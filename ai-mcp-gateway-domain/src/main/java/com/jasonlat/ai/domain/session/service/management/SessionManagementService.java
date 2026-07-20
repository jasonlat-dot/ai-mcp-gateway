package com.jasonlat.ai.domain.session.service.management;

import com.jasonlat.ai.domain.session.model.valobj.SessionConfigVO;
import com.jasonlat.ai.domain.session.model.valobj.enums.SessionTransportTypeEnumVO;
import com.jasonlat.ai.domain.session.service.ISessionManagementService;
import com.jasonlat.ai.types.enums.McpErrorCodes;
import com.jasonlat.ai.types.exception.AppException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * @author jasonlat
 * 2026-04-20  22:28
 */
@Slf4j
@Service
public class SessionManagementService implements ISessionManagementService {

    // 循环重试关闭 SSE sink 的操作，最多持续等待 200ms，超时直接放弃，防止线程死循环卡死。
    private static final long EMIT_RETRY_TIMEOUT_NANOS = Duration.ofMillis(200).toNanos();
    // 循环重试关闭 SSE sink 的操作，每次暂停 1ms
    private static final long EMIT_RETRY_PAUSE_NANOS = TimeUnit.MICROSECONDS.toNanos(1000);

    private final String messageEndpointPrefix;
    private final ScheduledExecutorService cleanupScheduler;
    private final long sessionTimeoutMinutes;
    private final long cleanupIntervalMillis;
    private final int eventBufferCapacity;
    // 每次清理无效会话，最多处理 x 个
    private final int maxCleanupCount;

    private final Map<String, SessionConfigVO> activeSessions = new ConcurrentHashMap<>(8);
    private final Object lifecycleMonitor = new Object();
    private volatile ScheduledFuture<?> cleanupTask;
    private volatile boolean shuttingDown;

    public SessionManagementService(
            @Qualifier("sessionCleanupScheduler") ScheduledExecutorService cleanupScheduler,
            @Value("${server.servlet.context-path:/api-gateway}") String messageEndpointPrefix,
            @Value("${mcp.session.idle-timeout-minutes:10}") long sessionTimeoutMinutes,
            @Value("${mcp.session.cleanup-interval-ms:300000}") long cleanupIntervalMillis,
            @Value("${mcp.session.event-buffer-capacity:256}") int eventBufferCapacity,
            @Value("${mcp.session.max-cleanup-count:100}") int maxCleanupCount) {
        this.cleanupScheduler = cleanupScheduler;
        this.messageEndpointPrefix = messageEndpointPrefix;
        this.sessionTimeoutMinutes = requirePositive(
                sessionTimeoutMinutes, "mcp.session.idle-timeout-minutes");
        this.cleanupIntervalMillis = requirePositive(
                cleanupIntervalMillis, "mcp.session.cleanup-interval-ms");
        this.eventBufferCapacity = requirePositive(
                eventBufferCapacity, "mcp.session.event-buffer-capacity");
        this.maxCleanupCount = requirePositive(
                maxCleanupCount, "mcp.session.max-cleanup-count");
    }

    @PostConstruct
    public void startCleanupTask() {
        cleanupTask = cleanupScheduler.scheduleWithFixedDelay(
                this::safeClearInactiveSessions,
                cleanupIntervalMillis,
                cleanupIntervalMillis,
                TimeUnit.MILLISECONDS);
        log.info("会话管理服务已启动，会话超时:{}分钟 清理周期:{}毫秒 session会话事件缓冲:{}",
                sessionTimeoutMinutes, cleanupIntervalMillis, eventBufferCapacity);
    }

    /**
     * 创建会话
     *
     * @param gatewayId 网关ID
     * @return 会话配置
     */
    @Override
    public SessionConfigVO createSession(String gatewayId, String apiKey) {

        return createSession(gatewayId, apiKey, SessionTransportTypeEnumVO.SSE);
    }

    @Override
    public SessionConfigVO createSession(String gatewayId, String apiKey, SessionTransportTypeEnumVO transportType) {
        SessionTransportTypeEnumVO sessionTransportType = transportType == null ? SessionTransportTypeEnumVO.SSE : transportType;
        log.info("创建会话 gatewayId:{} transportType:{}", gatewayId, sessionTransportType.getCode());

        String sessionId = "s-" + UUID.randomUUID();

        // 一个 session 只对应一条 SSE 响应流；有界队列避免慢客户端造成无限内存增长。
        Sinks.Many<ServerSentEvent<String>> sink = Sinks.many()
                .unicast()
                .onBackpressureBuffer(new ArrayBlockingQueue<>(eventBufferCapacity));

        // SSE 协议需要发送 endpoint 事件，Streamable HTTP 协议通过 Mcp-Session-Id 响应头返回会话，不发送 endpoint，避免破坏协议语义。
        if (sessionTransportType == SessionTransportTypeEnumVO.SSE) {
            UriComponentsBuilder endpointBuilder = UriComponentsBuilder
                    .fromPath(messageEndpointPrefix)
                    .pathSegment(gatewayId, "mcp", "sse")
                    .queryParam("sessionId", sessionId);
            if (StringUtils.isNotBlank(apiKey)) {
                endpointBuilder.queryParam("api_key", apiKey);
            }
            String messageEndpoint = endpointBuilder.build().encode().toUriString();

            Sinks.EmitResult emitResult = sink.tryEmitNext(ServerSentEvent.<String>builder()
                    .event("endpoint")
                    .data(messageEndpoint)
                    .build());
            if (emitResult.isFailure()) {
                sink.tryEmitComplete();
                throw new AppException(
                        "初始化 MCP SSE 会话失败 sessionId:" + sessionId + " result:" + emitResult);
            }
        }

        SessionConfigVO sessionConfigVO = new SessionConfigVO(sessionId, sink);
        synchronized (lifecycleMonitor) {
            if (shuttingDown) {
                sink.tryEmitComplete();
                throw new AppException(McpErrorCodes.SERVER_SHUTTING_DOWN, "server shutting down.");
            }
            activeSessions.put(sessionId, sessionConfigVO);
        }

        return sessionConfigVO;
    }

    /**
     * 移除会话
     *
     * @param sessionId 会话ID
     */
    @Override
    public void removeSession(String sessionId) {
        if (StringUtils.isBlank(sessionId)) return;

        SessionConfigVO sessionConfigVO = activeSessions.remove(sessionId);
        if (sessionConfigVO == null) {
            log.debug("会话已移除或不存在 sessionId:{}", sessionId);
            return;
        }

        sessionConfigVO.markInactive();

        completeSink(sessionId, sessionConfigVO.getSink());

        log.info("移除session会话,sessionId:{},", sessionId);
    }

    /**
     * 获取会话
     *
     * @param sessionId 会话ID
     * @return 会话配置
     */
    @Override
    public SessionConfigVO getSession(String sessionId) {
        if (StringUtils.isBlank(sessionId)) return null;

        SessionConfigVO sessionConfigVO = activeSessions.get(sessionId);
        if (sessionConfigVO != null && sessionConfigVO.isActive()) {
            sessionConfigVO.updateLastAccessed();
            return sessionConfigVO;
        }

        return null;
    }

    /**
     * 清理无效会话(过期的会话)
     */
    @Override
    public void clearInactiveSessions() {
        int beforeCleanup = activeSessions.size();
        log.debug("开始清理无效会话,当前活跃会话数:{}", beforeCleanup);
        if (beforeCleanup == 0) return;
        activeSessions.entrySet().stream()
                .filter(entry -> {
                    SessionConfigVO vo = entry.getValue();
                    return !vo.isActive() || vo.isExpired(sessionTimeoutMinutes);
                })
                .limit(maxCleanupCount)
                .forEach(entry -> removeSession(entry.getKey()));

        int afterCleanup = activeSessions.size();
        if (afterCleanup < beforeCleanup) {
            log.debug("清理无效会话完成,清理数量:{},当前活跃会话数:{}", beforeCleanup - afterCleanup, afterCleanup);
        } else {
            log.debug("无过期会话,当前活跃会话数:{}", afterCleanup);
        }
    }

    /**
     * 停止服务
     */
    @Override
    @PreDestroy
    public void shutdown() {
        synchronized (lifecycleMonitor) {
            if (shuttingDown) return;
            shuttingDown = true;
        }

        log.info("关闭会话管理服务...");
        ScheduledFuture<?> task = cleanupTask;
        if (task != null) {
            task.cancel(false);
        }

        activeSessions.keySet().forEach(this::removeSession);

        // 调度器由 Spring Bean 的 destroyMethod 统一关闭，这里只负责取消本服务的任务。
        log.info("关闭会话管理服务完成");
    }

    private void safeClearInactiveSessions() {
        try {
            clearInactiveSessions();
        } catch (Exception e) {
            // ScheduledExecutorService 的周期任务若异常逃逸，后续调度会被永久抑制。
            log.error("清理无效会话失败，下一周期将继续重试", e);
        }
    }

    private void completeSink(String sessionId, Sinks.Many<ServerSentEvent<String>> sink) {
        long deadline = System.nanoTime() + EMIT_RETRY_TIMEOUT_NANOS;
        Sinks.EmitResult emitResult;
        do {
            emitResult = sink.tryEmitComplete();
            if (emitResult != Sinks.EmitResult.FAIL_NON_SERIALIZED) break;
            LockSupport.parkNanos(EMIT_RETRY_PAUSE_NANOS);
        } while (System.nanoTime() < deadline);

        if (emitResult.isFailure()
                && emitResult != Sinks.EmitResult.FAIL_TERMINATED
                && emitResult != Sinks.EmitResult.FAIL_CANCELLED) {
            log.warn("结束会话 Sink 失败 sessionId:{} result:{}", sessionId, emitResult);
        }
    }

    private static long requirePositive(long value, String propertyName) {
        if (value <= 0) {
            throw new IllegalArgumentException(propertyName + " 必须大于 0，当前值:" + value);
        }
        return value;
    }

    private static int requirePositive(int value, String propertyName) {
        if (value <= 0) {
            throw new IllegalArgumentException(propertyName + " 必须大于 0，当前值:" + value);
        }
        return value;
    }
}
