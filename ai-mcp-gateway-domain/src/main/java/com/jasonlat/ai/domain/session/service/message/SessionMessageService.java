package com.jasonlat.ai.domain.session.service.message;

import com.alibaba.fastjson2.JSON;
import com.jasonlat.ai.domain.session.model.valobj.McpSchemaVO;
import com.jasonlat.ai.domain.session.model.valobj.enums.SessionMessageHandlerMethodEnum;
import com.jasonlat.ai.domain.session.service.ISessionMessageService;
import com.jasonlat.ai.domain.session.service.message.handle.IRequestHandler;
import com.jasonlat.ai.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.jasonlat.ai.types.enums.ResponseCode.METHOD_HANDLER_SERVICE_NOT_FOUND;
import static com.jasonlat.ai.types.enums.ResponseCode.METHOD_NOT_FOUND;

/**
 * @author jasonlat
 * 2026-05-12  22:20
 */
@Slf4j
@Service
public class SessionMessageService implements ISessionMessageService {

    @Resource
    private Map<String, IRequestHandler> requestHandlerMap;

    @Override
    public McpSchemaVO.JsonRpcResponse processHandleMessage(String gatewayId, McpSchemaVO.JsonRpcMessage messageRequest) {

        if (messageRequest instanceof McpSchemaVO.JsonRpcResponse response) {
            // todo
            log.info("收到 MCP SSE 结果消息: {}", JSON.toJSONString(response));
        }

        if (messageRequest instanceof McpSchemaVO.JsonRpcRequest request) {
            // 获取处理方法
            String method = request.method();
            log.info("开始处理请求，方法：{}", method);
            // 获取处理方法枚举
            SessionMessageHandlerMethodEnum handlerEnum = SessionMessageHandlerMethodEnum.getByMethod(method);
            if (handlerEnum == null) {
                throw new AppException(METHOD_NOT_FOUND);
            }
            // 获取处理方法实现
            String handlerName = handlerEnum.getHandlerName();
            IRequestHandler requestHandler = requestHandlerMap.get(handlerName);
            if (requestHandler == null) {
                throw new AppException(METHOD_HANDLER_SERVICE_NOT_FOUND);
            }
            // 处理消息
            return requestHandler.handleMessage(gatewayId, request);
        }

        if (messageRequest instanceof McpSchemaVO.JsonRpcNotification notification) {
            // todo
            log.info("收到 MCP SSE 通知消息: {}", JSON.toJSONString(notification));
        }

        return null;
    }
}
