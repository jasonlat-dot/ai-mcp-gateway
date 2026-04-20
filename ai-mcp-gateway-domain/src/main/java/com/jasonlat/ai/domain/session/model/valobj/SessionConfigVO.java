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

    // 不可变、线程安全 精确到纳秒的 时间
    private Instant createTime;

    // 最后访问时间
    private volatile Instant lastAccessedTime;

    // 是否活跃
    private volatile boolean active;

    public SessionConfigVO(String sessionId, Sinks.Many<ServerSentEvent<String>> sink) {
        this.sessionId = sessionId;
        this.sink = sink;
        this.createTime = Instant.now();
        this.lastAccessedTime = Instant.now();
        this.active = true;
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
        // 上次访问时间 早于 当前时间往前推 timeoutMinutes 分钟 → 已过期
        return lastAccessedTime.isBefore(Instant.now().minus(timeoutMinutes, ChronoUnit.MINUTES));
    }
}
