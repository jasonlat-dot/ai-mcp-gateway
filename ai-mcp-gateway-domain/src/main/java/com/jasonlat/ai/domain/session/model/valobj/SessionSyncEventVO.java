package com.jasonlat.ai.domain.session.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SessionSyncEventVO {

    public enum EventType {
        CREATE,
        REMOVE
    }

    /**
     * 事件类型
     */
    private EventType eventType;

    /**
     * 会话同步信息
     */
    private SessionSyncInfoVO sessionSyncInfo;

}