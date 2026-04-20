package com.jasonlat.ai.domain.session.service;

import com.jasonlat.ai.domain.session.model.valobj.SessionConfigVO;

/**
 * @author jasonlat
 * 2026-04-20  22:26
 */
public interface ISessionManagementService {

    /**
     * 创建会话
     * @param gatewayId 网关ID
     * @return  会话配置
     */
    SessionConfigVO createSession(String gatewayId);

    /**
     * 移除会话
     * @param sessionId 会话ID
     */
    void removeSession(String sessionId);

    /**
     * 获取会话
     * @param sessionId 会话ID
     * @return  会话配置
     */
    SessionConfigVO getSession(String sessionId);

    /**
     * 清理无效会话(过期的会话)
     */
    void clearInactiveSessions();

    /**
     * 停止服务
     */
    void shutdown();

}
