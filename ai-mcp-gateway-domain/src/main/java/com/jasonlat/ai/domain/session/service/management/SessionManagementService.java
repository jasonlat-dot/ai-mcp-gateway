package com.jasonlat.ai.domain.session.service.management;

import com.jasonlat.ai.domain.session.adapter.port.ISessionPort;
import com.jasonlat.ai.domain.session.model.valobj.SessionConfigVO;
import com.jasonlat.ai.domain.session.model.valobj.SessionSyncInfoVO;
import com.jasonlat.ai.domain.session.model.valobj.enums.SessionTransportTypeEnumVO;
import com.jasonlat.ai.domain.session.service.ISessionDistributedService;
import com.jasonlat.ai.domain.session.service.ISessionManagementService;
import com.jasonlat.ai.types.exception.AppException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Sinks;


import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;


/**
 * @author jasonlat
 * 2026-04-20  22:28
 */
@Slf4j
@Service
public class SessionManagementService implements ISessionManagementService {

    @Resource
    private ISessionPort sessionPort;

    @Resource
    private ISessionDistributedService sessionDistributedService;



    /**
     * 活跃回话存储器，key->sessionId，ConcurrentHashMap 确保线程安全
     */
    private final Map<String, SessionConfigVO> activeSessions = new ConcurrentHashMap<>();

    /**
     * 会话超时时间（分钟）- 也可以把配置抽取到yml里
     */
    private final int sessionTimeoutMinutes;
    private final String messageEndpointPrefix;
    private final int eventBufferCapacity;


    public SessionManagementService(
            @Value("${server.servlet.context-path:/api-gateway}") String messageEndpointPrefix,
            @Value("${mcp.session.event-buffer-capacity:256}") int eventBufferCapacity,
            @Value("${mcp.session.session-timeout-minutes:30}") int sessionTimeoutMinutes
            ) {
        this.messageEndpointPrefix = messageEndpointPrefix;
        this.eventBufferCapacity = requirePositive(eventBufferCapacity, "mcp.session.event-buffer-capacity");
        this.sessionTimeoutMinutes = requirePositive(sessionTimeoutMinutes, "mcp.session.session-timeout-minutes");
        /*
         * 定时任务调度
         */
        ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor();
        cleanupScheduler.scheduleAtFixedRate(this::cleanupExpiredSessions, 5, 5, TimeUnit.MINUTES);
        log.info("会话管理服务已启动，会话超时时间: {} 分钟", sessionTimeoutMinutes);
    }

    @PostConstruct
    public void init() {
        initializeDistributedSessions();
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
        SessionConfigVO sessionConfigVO = createLocalSession(sessionId, gatewayId, apiKey, sessionTransportType);

        SessionSyncInfoVO sessionSyncInfoVO = sessionDistributedService.buildSessionSyncInfo(sessionId, gatewayId, apiKey, sessionTransportType);
        sessionDistributedService.saveSession(sessionSyncInfoVO);

        log.info("创建会话 gatewayId:{} sessionId:{} transportType:{},当前活跃会话数:{}", gatewayId, sessionId, sessionTransportType.getCode(), activeSessions.size());

        return sessionConfigVO;
    }

    private SessionConfigVO createLocalSession(String sessionId, String gatewayId, String apiKey, SessionTransportTypeEnumVO transportType) {

        // 一个 session 只对应一条 SSE 响应流；有界队列避免慢客户端造成无限内存增长。
        Sinks.Many<ServerSentEvent<String>> sink = Sinks.many()
                .unicast()
                .onBackpressureBuffer(new ArrayBlockingQueue<>(eventBufferCapacity));

        // SSE 协议需要发送 endpoint 事件，Streamable HTTP 协议通过 Mcp-Session-Id 响应头返回会话，不发送 endpoint，避免破坏协议语义。
        if (transportType == SessionTransportTypeEnumVO.SSE) {
            UriComponentsBuilder endpointBuilder = UriComponentsBuilder
                    .fromPath(messageEndpointPrefix)
                    .pathSegment(gatewayId, "mcp", "sse")
                    .queryParam("sessionId", sessionId);
            if (StringUtils.isNotBlank(apiKey)) {
                endpointBuilder.queryParam("api_key", apiKey);
            }
            String messageEndpoint = endpointBuilder.build().encode().toUriString();
            log.debug("mcp messageEndpoint: {}", messageEndpoint);
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
        activeSessions.put(sessionId, sessionConfigVO);

        return sessionConfigVO;
    }

    /**
     * 移除会话
     *
     * @param sessionId 会话ID
     */
    @Override
    public void removeSession(String sessionId) {
        log.info("删除会话配置 sessionId:{}", sessionId);
        removeLocalSession(sessionId);
        sessionDistributedService.removeSession(sessionId);
    }

    @Override
    public void removeLocalSession(String sessionId) {
        SessionConfigVO sessionConfigVO = activeSessions.remove(sessionId);

        if (null == sessionConfigVO) {
            log.info("会话{}已不存在于本地实例", sessionId);
            return;
        }
        sessionConfigVO.markInactive();

        try {
            sessionConfigVO.getSink().tryEmitComplete();
        } catch (Exception e) {
            log.warn("关闭会话Sink时出错:{}", e.getMessage());
        }

        log.info("移除本地会话:{},剩余活跃会话数:{}", sessionId, activeSessions.size());
    }

    /**
     * 获取会话
     *
     * @param sessionId 会话ID
     * @return 会话配置
     */
    @Override
    public SessionConfigVO getSession(String sessionId) {
        if (null == sessionId || sessionId.isEmpty()) {
            return null;
        }

        SessionConfigVO sessionConfigVO = activeSessions.get(sessionId);
        if (null != sessionConfigVO && sessionConfigVO.isActive()) {
            sessionConfigVO.updateLastAccessed();
            return sessionConfigVO;
        }
        // 兜底处理 从分布式缓存获取session信息
        SessionSyncInfoVO sessionSyncInfoVO = sessionDistributedService.getSession(sessionId);
        if (sessionSyncInfoVO == null) return null;
        return sessionDistributedService.rebuildLocalSession(sessionSyncInfoVO);
    }

    @Override
    public void syncSession(SessionSyncInfoVO sessionSyncInfoVO) {
        if (sessionSyncInfoVO == null || StringUtils.isBlank(sessionSyncInfoVO.getSessionId()) || !sessionSyncInfoVO.isActive()) {
            return;
        }

        activeSessions.computeIfAbsent(sessionSyncInfoVO.getSessionId(), key -> {
            log.info("同步远端会话到本地实例 sessionId:{} transportType:{}", sessionSyncInfoVO.getSessionId(),
                    sessionSyncInfoVO.getTransportType() == null ? "unknown" : sessionSyncInfoVO.getTransportType().getCode());
            return sessionDistributedService.rebuildLocalSession(sessionSyncInfoVO);
        });
    }

    @Override
    public boolean hasSession(String sessionId) {
        return StringUtils.isNotBlank(sessionId) && activeSessions.containsKey(sessionId);
    }


    private static int requirePositive(int value, String propertyName) {
        if (value <= 0) {
            throw new IllegalArgumentException(propertyName + " 必须大于 0，当前值:" + value);
        }
        return value;
    }

    public void cleanupExpiredSessions() {
        int cleanedCount = 0;

        for (Map.Entry<String, SessionConfigVO> entry : activeSessions.entrySet()) {
            SessionConfigVO sessionConfigVO = entry.getValue();

            if (!sessionConfigVO.isActive() || sessionConfigVO.isExpired(sessionTimeoutMinutes)) {
                removeSession(sessionConfigVO.getSessionId());
                cleanedCount++;
            }

        }

        if (cleanedCount > 0) {
            log.info("清理了 {} 个过期会话，剩余活跃会话数: {}", cleanedCount, activeSessions.size());
        }
    }

    @Override
    public void initializeDistributedSessions() {
        int loadCount = 0;
        for (SessionSyncInfoVO sessionSyncInfoVO : sessionDistributedService.loadActiveSessions()) {
            if (sessionSyncInfoVO == null || StringUtils.isBlank(sessionSyncInfoVO.getSessionId()) || !sessionSyncInfoVO.isActive()) {
                continue;
            }

            if (activeSessions.containsKey(sessionSyncInfoVO.getSessionId())) {
                continue;
            }

            activeSessions.put(sessionSyncInfoVO.getSessionId(), sessionDistributedService.rebuildLocalSession(sessionSyncInfoVO));
            loadCount++;
        }

        if (loadCount > 0) {
            log.info("应用启动完成分布式会话初始化，同步会话数:{} 当前本地活跃会话数:{}", loadCount, activeSessions.size());
        } else {
            log.info("应用启动完成分布式会话初始化，未发现可恢复会话");
        }
    }
}
