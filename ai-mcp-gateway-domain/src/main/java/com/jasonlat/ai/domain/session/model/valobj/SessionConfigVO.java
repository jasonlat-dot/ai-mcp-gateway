package com.jasonlat.ai.domain.session.model.valobj;

import lombok.*;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Sinks;

import java.time.Instant;
import java.time.temporal.ChronoUnit;


/**
 * 会话配置
 * @author jasonlat
 * 2026-04-20  22:12
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SessionConfigVO {

    private String sessionId;

    // 响应式对象
    private Sinks.Many<ServerSentEvent<String>> sink;

    /**
     * 会话时间
     */
    private Instant createTime;

    /**
     * 最后访问时间戳，volatile 确保多线程下可见性
     */
    private volatile Instant lastAccessedTime;

    /**
     * 会话活跃状态标识
     */
    private volatile boolean active;

    /**
     * 持有 SSE 长连接的实例 ID（IP:Port 或 instanceId）
     * <p>
     * 关键字段：用于判断"本机是否就是连接持有者"。
     * 若本机不是 holder，POST 消息需要转发到 holder 处理。
     */
    private String holderInstanceId;

    public SessionConfigVO(String sessionId, Sinks.Many<ServerSentEvent<String>> sink) {
        this.sessionId = sessionId;
        this.sink = sink;
        this.createTime = Instant.now();
        this.lastAccessedTime = Instant.now();
        this.active = true;
    }

    public SessionConfigVO(String sessionId, Sinks.Many<ServerSentEvent<String>> sink, String holderInstanceId) {
        this.sessionId = sessionId;
        this.sink = sink;
        this.createTime = Instant.now();
        this.lastAccessedTime = Instant.now();
        this.active = true;
        this.holderInstanceId = holderInstanceId;
    }

    /**
     * 标记会话为非活跃状态
     */
    public void markInactive() {
        this.active = false;
    }

    /**
     * 更新最后访问时间
     */
    public void updateLastAccessed() {
        this.lastAccessedTime = Instant.now();
    }

    /**
     * 过期时间判断
     */
    public boolean isExpired(long timeoutMinutes) {
        return lastAccessedTime.isBefore(Instant.now().minus(timeoutMinutes, ChronoUnit.MINUTES));
    }
}
