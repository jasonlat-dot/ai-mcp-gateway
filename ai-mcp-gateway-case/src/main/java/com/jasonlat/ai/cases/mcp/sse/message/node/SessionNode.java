package com.jasonlat.ai.cases.mcp.sse.message.node;

import com.jasonlat.ai.cases.mcp.sse.message.AbstractMcpSseMessageSupport;
import com.jasonlat.ai.cases.mcp.sse.message.facotry.DefaultMcpMessageFactory;
import com.jasonlat.ai.domain.session.model.entity.HandleMessageCommandEntity;
import com.jasonlat.ai.domain.session.model.valobj.SessionConfigVO;
import com.jasonlat.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * @author jasonlat
 * 2026-06-26  19:38
 */
@Slf4j
@Service("mcpMessageSessionNode")
public class SessionNode extends AbstractMcpSseMessageSupport {

    @Resource(name = "rateLimitNode")
    private RateLimitNode rateLimitNode;
    @Resource(name = "mcpMessageHolderForwardNode")
    private HolderForwardNode holderForwardNode;

    /**
     * 当前实例 ID
     */
    @Value("${mcp.instance.id}")
    private String instanceId;

    @Override
    protected ResponseEntity<Object> doApply(HandleMessageCommandEntity requestParameter, DefaultMcpMessageFactory.DynamicContext dynamicContext) throws Exception {
        log.info("SessionNode handle mcp message: {}", requestParameter.toString());
        SessionConfigVO sessionConfigVO = sessionManagementService.getSession(requestParameter.getSessionId());
        if (sessionConfigVO == null) {
            log.warn("会话不存在 gatewayId:{} sessionId:{}", requestParameter.getGatewayId(), requestParameter.getSessionId());
            return ResponseEntity.notFound().build();
        }
        // 设置会话信息
        dynamicContext.setSessionConfigVO(sessionConfigVO);

        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<HandleMessageCommandEntity, DefaultMcpMessageFactory.DynamicContext, ResponseEntity<Object>> get(HandleMessageCommandEntity requestParameter, DefaultMcpMessageFactory.DynamicContext dynamicContext) throws Exception {
        SessionConfigVO sessionConfigVO = dynamicContext.getSessionConfigVO();
        String holderInstanceId = sessionConfigVO.getHolderInstanceId();
        // ========== 关键路由逻辑 ==========
        // 如果 holderInstanceId 为空，向后兼容老逻辑（仍走本机）
        // 如果本机就是 holder，正常往下走
        // 如果本机不是 holder，转发到真正的 holder 机器
        if (StringUtils.isNotBlank(holderInstanceId) && !holderInstanceId.equals(instanceId)) {
            return holderForwardNode;
        }
        return rateLimitNode;
    }
}
