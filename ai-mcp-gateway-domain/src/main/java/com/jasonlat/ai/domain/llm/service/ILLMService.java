package com.jasonlat.ai.domain.llm.service;

import com.jasonlat.ai.domain.llm.model.entity.BuildChatModelCommandEntity;


/**
 * 大模型服务接口；用于网关服务测试
 */
public interface ILLMService {

    /**
     * 创建一次性的 MCP 客户端并执行测试调用；方法返回前关闭客户端连接。
     */
    String callGateway(BuildChatModelCommandEntity commandEntity, String message);

}
