package com.jasonlat.ai.domain.session.service;



import com.jasonlat.ai.domain.session.model.valobj.SessionConfigVO;
import com.jasonlat.ai.domain.session.model.valobj.SessionSyncEventVO;
import com.jasonlat.ai.domain.session.model.valobj.SessionSyncInfoVO;
import com.jasonlat.ai.domain.session.model.valobj.enums.SessionTransportTypeEnumVO;

import java.util.List;
import java.util.function.Consumer;

/**
 * 分布式会话管理服务接口
 */
public interface ISessionDistributedService {

    /**
     * 构建会话同步信息
     */
    SessionSyncInfoVO buildSessionSyncInfo(String sessionId, String gatewayId, String apiKey, SessionTransportTypeEnumVO transportType);

    /**
     * 构建会话同步信息（多实例版，携带 holderInstanceId）
     * <p>
     * 用于解决 SSE 长连接跨机器路由问题：记录谁是连接持有者，让 POST 能找到它。
     */
    SessionSyncInfoVO buildSessionSyncInfo(String sessionId, String gatewayId, String apiKey, SessionTransportTypeEnumVO transportType, String holderInstanceId);


    /**
     * 从同步信息重建本地会话
     */
    SessionConfigVO rebuildLocalSession(SessionSyncInfoVO sessionSyncInfoVO);

    /**
     * 保存会话同步信息到 Redis
     */
    void saveSession(SessionSyncInfoVO sessionSyncInfoVO);

    /**
     * 从 Redis 删除会话同步信息
     */
    void removeSession(String sessionId);

    /**
     * 从 Redis 加载当前有效的活跃会话
     */
    List<SessionSyncInfoVO> loadActiveSessions();

    SessionSyncInfoVO getSession(String sessionId);
}
