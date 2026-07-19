package com.jasonlat.ai.cases.mcp.streamable.session.node;


import com.jasonlat.ai.cases.mcp.streamable.session.AbstractMcpStreamableSessionSupport;
import com.jasonlat.ai.cases.mcp.streamable.session.factory.DefaultMcpStreamableSessionFactory;
import com.jasonlat.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;


/**
 * 根节点
 */
@Slf4j
@Service("mcpStreamableSessionRootNode")
public class RootNode extends AbstractMcpStreamableSessionSupport {

    @Resource(name = "mcpStreamableSessionVerifyNode")
    private VerifyNode verifyNode;

    @Override
    protected Flux<ServerSentEvent<String>> doApply(String requestParameter, DefaultMcpStreamableSessionFactory.DynamicContext dynamicContext) throws Exception {
        try {
            log.info("创建会话 mcp streamable session RootNode:{}", requestParameter);

            return router(requestParameter, dynamicContext);
        } catch (Exception e) {
            log.error("创建会话 mcp streamable session RootNode 异常:{}", requestParameter, e);
            throw e;
        }
    }

    @Override
    public StrategyHandler<String, DefaultMcpStreamableSessionFactory.DynamicContext, Flux<ServerSentEvent<String>>> get(String requestParameter, DefaultMcpStreamableSessionFactory.DynamicContext dynamicContext) throws Exception {
        return verifyNode;
    }

}
