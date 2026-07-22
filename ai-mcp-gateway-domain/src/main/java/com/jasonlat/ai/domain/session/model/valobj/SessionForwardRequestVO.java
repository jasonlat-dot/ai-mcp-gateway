package com.jasonlat.ai.domain.session.model.valobj;

import com.jasonlat.ai.domain.session.model.entity.HandleMessageCommandEntity;
import lombok.*;

import java.io.IOException;

/**
 * SSE 跨机器转发的请求 VO
 * <p>
 * 把"转发到 holder"所需的所有参数聚合成一个对象，避免 ISessionPort.forwardToHolder 签名过长。
 * <p>
 * 设计原则：
 * - 复用 HandleMessageCommandEntity 的核心字段（gatewayId / apiKey / sessionId / messageBody）
 * - 仅新增"跨机器转发"相关的 4 个字段（holderInstanceId / contextPath / fromInstanceId）
 *
 * @author jasonlat
 * 2026-07-22  20:45
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SessionForwardRequestVO {

    /**
     * 连接持有者实例标识（格式：host:port，不含 protocol）
     */
    private String holderInstanceId;

    /**
     * 应用的 context-path（如 /api-gateway）
     */
    private String contextPath;

    /**
     * 网关ID
     */
    private String gatewayId;

    /**
     * API Key（可空）
     */
    private String apiKey;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 原始 JSON-RPC 消息体
     */
    private String messageBody;

    /**
     * 发起转发的实例 ID（X-Forwarded-Instance 头，便于 holder 排错）
     */
    private String fromInstanceId;

    /**
     * 工厂方法：从 HandleMessageCommandEntity 构造
     * <p>
     * 让 case 层调用方一行代码搞定参数封装。
     *
     * @param holderInstanceId  连接持有者实例标识
     * @param contextPath       应用 context-path
     * @param fromInstanceId    当前实例 ID
     * @param entity            原始消息处理实体
     * @return 转发请求 VO
     */
    public static SessionForwardRequestVO of(String holderInstanceId,
                                             String contextPath,
                                             String fromInstanceId,
                                             HandleMessageCommandEntity entity) {
        return SessionForwardRequestVO.builder()
                .holderInstanceId(holderInstanceId)
                .contextPath(contextPath)
                .gatewayId(entity.getGatewayId())
                .apiKey(entity.getApiKey())
                .sessionId(entity.getSessionId())
                .messageBody(serializeJsonRpc(entity))
                .fromInstanceId(fromInstanceId)
                .build();
    }

    /**
     * 从 entity 中提取 JSON-RPC 消息体
     * <p>
     * 优先用 messageBody 字段（如果调用方在 controller 层已经传入 raw body），
     * 否则把 jsonRpcMessage 序列化为 JSON 字符串。
     */
    private static String serializeJsonRpc(HandleMessageCommandEntity entity) {
        if (entity == null) return null;
        // 这里直接用 entity 的 jsonRpcMessage 转 JSON
        // 注意：不要在这里做序列化失败抛异常，由调用方决定如何处理
        try {
            return McpSchemaVO.serializeJsonRpcMessage(entity.getJsonRpcMessage());
        } catch (Exception e) {
            return null;
        }
    }
}