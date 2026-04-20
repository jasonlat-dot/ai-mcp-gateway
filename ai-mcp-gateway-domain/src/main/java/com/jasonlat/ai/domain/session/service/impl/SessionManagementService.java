package com.jasonlat.ai.domain.session.service.impl;

import com.jasonlat.ai.domain.session.model.valobj.SessionConfigVO;
import com.jasonlat.ai.domain.session.service.ISessionManagementService;
import com.jasonlat.ai.types.snow.SnowflakeIdGenerator;
import com.jasonlat.ai.types.utils.RandomCodeUtil;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author jasonlat
 * 2026-04-20  22:28
 */
@Slf4j
@Service
public class SessionManagementService implements ISessionManagementService {

    private static final long SESSION_TIMEOUT_MINUTES = 30;

    private final ScheduledExecutorService cleanupScheduler;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    private final Map<String, SessionConfigVO> activeSessions = new ConcurrentHashMap<>(8);

    public SessionManagementService(ScheduledExecutorService cleanupScheduler, SnowflakeIdGenerator snowflakeIdGenerator) {
        this.snowflakeIdGenerator = snowflakeIdGenerator;
        this.cleanupScheduler = cleanupScheduler;

        this.cleanupScheduler.scheduleAtFixedRate(
                this::clearInactiveSessions,// 要执行的任务
                5, // 第一次执行 延迟多久
                5, // 之后每隔多久执行一次
                TimeUnit.MINUTES);
        log.info("会话管理服务已启动，会话超时时间: {} 分钟", SESSION_TIMEOUT_MINUTES);
    }

    /**
     * 创建会话
     *
     * @param gatewayId 网关ID
     * @return 会话配置
     */
    @Override
    public SessionConfigVO createSession(String gatewayId) {
        log.info("创建会话 gatewayId:{}", gatewayId);
        String sessionId = RandomCodeUtil.generateRandomCode(6, true) + snowflakeIdGenerator.nextId();

        Sinks.Many<ServerSentEvent<String>> sink = Sinks.many().multicast().onBackpressureBuffer();

        String messageEndpoint = "/" + gatewayId + "/mcp/message?sessionId=" + sessionId;
        sink.tryEmitNext(ServerSentEvent.<String>builder()
                .event("endpoint")
                .data(messageEndpoint)
                .build());
        SessionConfigVO sessionConfigVO = new SessionConfigVO(sessionId, sink);
        activeSessions.put(sessionId, sessionConfigVO);
        log.info("创建会话 gatewayId:{} sessionId:{},当前活跃会话数:{}", gatewayId, sessionId, activeSessions.size());

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
        SessionConfigVO sessionConfigVO = activeSessions.remove(sessionId);
        if (null == sessionConfigVO) return;

        sessionConfigVO.markInactive();

        try {
            sessionConfigVO.getSink().tryEmitComplete();
        } catch (Exception e) {
            log.warn("关闭会话Sink时出错:{}", e.getMessage());
        }

        log.info("移除会话:{},剩余活跃会话数:{}", sessionId, activeSessions.size());
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

        return null;
    }

    /**
     * 清理无效会话(过期的会话)
     */
    @Override
    public void clearInactiveSessions() {
        log.info("开始清理无效会话");
        activeSessions.entrySet().stream()
                .filter(entry -> {
                    SessionConfigVO vo = entry.getValue();
                    return vo == null || !vo.isActive() || vo.isExpired(SESSION_TIMEOUT_MINUTES);
                })
                .forEach(entry -> removeSession(entry.getKey()));
//        for (Map.Entry<String, SessionConfigVO> entry : activeSessions.entrySet()) {
//            SessionConfigVO sessionConfigVO = entry.getValue();
//            if (!sessionConfigVO.isActive() || sessionConfigVO.isExpired(SESSION_TIMEOUT_MINUTES)) {
//                removeSession(sessionConfigVO.getSessionId());
//            }
//        }
        log.info("清理无效会话完成,当前活跃会话数:{}", activeSessions.size());
    }

    /**
     * 停止服务
     */
    @Override
    @PreDestroy
    public void shutdown() {
        log.info("关闭会话管理服务...");
        // 清理session
        activeSessions.keySet().forEach(this::removeSession);
        try {
            // 关闭清理调度器
            cleanupScheduler.shutdown();
            // 等待10秒让正在执行的任务完成
            if (!cleanupScheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                // 超时强制关闭
                cleanupScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            // 异常强制关闭
            cleanupScheduler.shutdown();
            Thread.currentThread().interrupt();
        }
        log.info("关闭会话管理服务完成");
    }
}
