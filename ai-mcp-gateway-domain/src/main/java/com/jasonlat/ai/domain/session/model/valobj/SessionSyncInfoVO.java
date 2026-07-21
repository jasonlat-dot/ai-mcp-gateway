package com.jasonlat.ai.domain.session.model.valobj;

import com.jasonlat.ai.domain.session.model.valobj.enums.SessionTransportTypeEnumVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SessionSyncInfoVO {

    /**
     * 会话ID
     */
    private String sessionId;
    /**
     * 网关ID
     */
    private String gatewayId;

    /**
     * API Key
     */
    private String apiKey;

    /**
     * 传输协议
     */
    private SessionTransportTypeEnumVO transportType;

    /**
     * 创建时间（毫秒时间戳）
     */
    private volatile long createTime;

    /**
     * 最后访问时间（毫秒时间戳）
     */
    private volatile long lastAccessedTime;
    /**
     * 会话活跃状态标识
     */
    private volatile boolean active;
}
