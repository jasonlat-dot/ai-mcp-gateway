package com.jasonlat.ai.domain.session.model.entity;

import com.jasonlat.ai.domain.session.model.valobj.McpSchemaVO;
import lombok.*;

import java.io.IOException;

/**
 * @author jasonlat
 * 2026-06-26  19:31
 */
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class HandleMessageCommandEntity {

    /**
     * 网关ID
     */
    private String gatewayId;

    /**
     * apiKey
     */
    private String apiKey;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * JSON-RPC消息
     */
    private McpSchemaVO.JsonRpcMessage jsonRpcMessage;

    public  HandleMessageCommandEntity(String gatewayId, String sessionId, String messageBody) throws IOException {
        this.gatewayId = gatewayId;
        this.sessionId = sessionId;
        this.jsonRpcMessage = McpSchemaVO.deserializeJsonRpcMessage(messageBody);
    }


    public  HandleMessageCommandEntity(String gatewayId,String apiKey, String sessionId, String messageBody) throws IOException {
        this.gatewayId = gatewayId;
        this.apiKey = apiKey;
        this.sessionId = sessionId;
        this.jsonRpcMessage = McpSchemaVO.deserializeJsonRpcMessage(messageBody);
    }
}
