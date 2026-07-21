package com.jasonlat.ai.domain.session.adapter.port;

import com.jasonlat.ai.domain.session.model.valobj.SessionSyncEventVO;
import com.jasonlat.ai.domain.session.model.valobj.SessionSyncInfoVO;
import com.jasonlat.ai.domain.session.model.valobj.gateway.McpToolProtocolConfigVO;


import java.util.List;
import java.util.function.Consumer;

public interface ISessionPort {


    Object toolCall(McpToolProtocolConfigVO.HTTPConfig httpConfig, Object params) throws Exception;

    void saveSessionSyncInfo(SessionSyncInfoVO sessionSyncInfoVO);

    void removeSessionSyncInfo(String sessionId);

    List<SessionSyncInfoVO> loadActiveSessions();

    void publishSessionSyncEvent(SessionSyncEventVO event);

    void subscribeSessionSyncEvent(Consumer<SessionSyncEventVO> consumer);
}
