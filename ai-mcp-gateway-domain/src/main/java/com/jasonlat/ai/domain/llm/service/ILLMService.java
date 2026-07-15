package com.jasonlat.ai.domain.llm.service;

import com.jasonlat.ai.domain.llm.model.entity.BuildChatModelCommandEntity;
import org.springframework.ai.chat.model.ChatModel;


/**
 * 大模型服务接口；用于网关服务测试
 */
public interface ILLMService {

    /**
     * 构建对话模型
     */
    void buildChatModel(BuildChatModelCommandEntity commandEntity);

    /**
     * 根据名称获取对话模型
     */
    ChatModel getChatModel(String gatewayId);

}
