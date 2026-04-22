package com.jasonlat.ai.cases.mcp.session;

import com.jasonlat.ai.cases.mcp.session.factory.DefaultMcpSessionFactory;
import com.jasonlat.ai.domain.session.service.ISessionManagementService;
import com.jasonlat.design.framework.tree.AbstractMultiThreadStrategyRouter;
import jakarta.annotation.Resource;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @author jasonlat
 * 2026-04-22  20:24
 */
public abstract class AbstractMcpMessageSupport extends AbstractMultiThreadStrategyRouter<String, DefaultMcpSessionFactory.DynamicContext, Flux<ServerSentEvent<String>>> {

    @Resource
    protected ISessionManagementService sessionManagementService;

    /**
     * 多线程异步数据加载方法
     * <p>
     * 子类需要实现此方法来定义具体的异步数据加载逻辑。
     * 该方法在业务流程处理之前执行，用于预加载必要的数据。
     * </p>
     *
     * @param requestParameter 请求参数
     * @param dynamicContext   动态上下文
     * @throws ExecutionException   执行异常
     * @throws InterruptedException 中断异常
     * @throws TimeoutException     超时异常
     */
    @Override
    protected void multiThread(String requestParameter, DefaultMcpSessionFactory.DynamicContext dynamicContext) throws ExecutionException, InterruptedException, TimeoutException {

    }

}
