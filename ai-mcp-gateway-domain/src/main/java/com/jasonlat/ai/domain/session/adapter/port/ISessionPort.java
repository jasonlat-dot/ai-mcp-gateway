package com.jasonlat.ai.domain.session.adapter.port;

import com.jasonlat.ai.domain.session.model.valobj.*;
import com.jasonlat.ai.domain.session.model.valobj.gateway.McpToolProtocolConfigVO;


import java.util.List;
import java.util.function.Consumer;

public interface ISessionPort {

    /**
     * 统一工具调用入口:按 protocolType 分发 HTTP / DUBBO
     * @param protocolConfig 完整协议配置(含 protocolType + 对应协议子配置)
     * @param params         MCP 客户端传入的参数 Map {city:"beijing", ...}
     */
    Object toolCall(McpToolProtocolConfigVO protocolConfig, Object params) throws Exception;

    void saveSessionSyncInfo(SessionSyncInfoVO sessionSyncInfoVO);

    void removeSessionSyncInfo(String sessionId);

    List<SessionSyncInfoVO> loadActiveSessions();

    void publishSessionSyncEvent(SessionSyncEventVO event);

    void subscribeSessionSyncEvent(Consumer<SessionSyncEventVO> consumer);

    SessionSyncInfoVO getSession(String sessionId);

    /**
     * SSE 连接跨机器转发
     * <p>
     * 当 POST 落到非 SSE 连接持有者时，把请求回调到真正的 holder 实例。
     * 入参和出参都用 VO 包装，避免长参数列表。
     *
     * @param request 转发请求 VO（含 holder 信息和原始消息体）
     * @return holder 的响应封装
     */
    SessionForwardResponseVO forwardToHolder(SessionForwardRequestVO request);
}
