package com.jasonlat.ai.cases.mcp.sse.message.node;

import com.jasonlat.ai.cases.mcp.sse.message.AbstractMcpSseMessageSupport;
import com.jasonlat.ai.cases.mcp.sse.message.facotry.DefaultMcpMessageFactory;
import com.jasonlat.ai.domain.session.adapter.port.ISessionPort;
import com.jasonlat.ai.domain.session.model.entity.HandleMessageCommandEntity;
import com.jasonlat.ai.domain.session.model.valobj.SessionConfigVO;
import com.jasonlat.ai.domain.session.model.valobj.SessionForwardRequestVO;
import com.jasonlat.ai.domain.session.model.valobj.SessionForwardResponseVO;
import com.jasonlat.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


/**
 * SSE 连接跨机器转发节点
 * <p>
 * 当 POST 落到非 SSE 连接持有者时，本节点通过 HTTP 回调到真正的 holder 实例。
 * <p>
 * 调用链路（在原消息处理链路中插入）:
 * <pre>
 *   RootNode
 *     → SessionNode         （查 session、识别本机是不是 holder）
 *       → HolderForwardNode（本节点：本机非 holder 时执行 HTTP 转发）
 *         → RateLimitNode   （本机是 holder 时继续走限流）
 *           → SseMessageHandleNode
 * </pre>
 *
 * @author jasonlat
 * 2026-07-22  20:30
 */
@Slf4j
@Service("mcpMessageHolderForwardNode")
public class HolderForwardNode extends AbstractMcpSseMessageSupport {

    @Resource
    private ISessionPort sessionPort;
    @Resource(name = "rateLimitNode")
    private RateLimitNode rateLimitNode;
    /**
     * 当前实例唯一标识（IP:Port），用于判断本机是否就是 holder
     */
    @Value("${mcp.instance.id}")
    private String instanceId;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Override
    protected ResponseEntity<Object> doApply(HandleMessageCommandEntity requestParameter,
                                             DefaultMcpMessageFactory.DynamicContext dynamicContext) throws Exception {
        SessionConfigVO sessionConfigVO = dynamicContext.getSessionConfigVO();
        if (sessionConfigVO == null) {
            log.warn("HolderForwardNode 缺失 sessionConfigVO sessionId:{}", requestParameter.getSessionId());
            return ResponseEntity.notFound().build();
        }
        String holderInstanceId = sessionConfigVO.getHolderInstanceId();
        // 防御性检查：本机就是 holder 时跳过转发
        if (StringUtils.isBlank(holderInstanceId) || holderInstanceId.equals(instanceId)) {
            log.debug("HolderForwardNode 本机就是 holder，跳过转发 sessionId:{}", requestParameter.getSessionId());
            return router(requestParameter, dynamicContext);
        }

        // 跨机器转发
        log.info("SSE 连接跨机器转发 sessionId:{} holderInstanceId:{} -> {}", requestParameter.getSessionId(), instanceId, holderInstanceId);
        // 工厂方法一行搞定参数封装
        SessionForwardRequestVO forwardRequest = SessionForwardRequestVO.of(
                holderInstanceId, contextPath, instanceId, requestParameter
        );
        SessionForwardResponseVO forwardResponse = sessionPort.forwardToHolder(forwardRequest);
        if (forwardResponse == null) {
            log.error("SSE 转发返回为空 sessionId:{} holder:{}",
                    requestParameter.getSessionId(), holderInstanceId);
            return ResponseEntity.internalServerError().build();
        }
        if (forwardResponse.isSuccess()) {
            log.debug("SSE 转发成功 sessionId:{} holder:{} status:{}", requestParameter.getSessionId(), holderInstanceId, forwardResponse.getStatusCode());
            return ResponseEntity.accepted().build();
        }
        log.warn("SSE 转发失败 sessionId:{} holder:{} status:{} error:{}", requestParameter.getSessionId(), holderInstanceId, forwardResponse.getStatusCode(), forwardResponse.getErrorMessage());
        HttpStatus status;
        try {
            status = HttpStatus.valueOf(forwardResponse.getStatusCode());
        } catch (Exception e) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return ResponseEntity.status(status).build();
    }
    @Override
    public StrategyHandler<HandleMessageCommandEntity, DefaultMcpMessageFactory.DynamicContext, ResponseEntity<Object>> get(HandleMessageCommandEntity requestParameter, DefaultMcpMessageFactory.DynamicContext dynamicContext) throws Exception {
        return rateLimitNode;
    }
}