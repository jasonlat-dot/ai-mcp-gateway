package com.jasonlat.ai.domain.session.service.message;

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
    public McpSchemaVO.JsonRpcResponse processHandleMessage(McpSchemaVO.JsonRpcRequest request) {
        log.info("处理消息：{}", request);
        // 获取处理方法
        String method = request.method();
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
        return requestHandler.handleMessage(request);
    }
}
