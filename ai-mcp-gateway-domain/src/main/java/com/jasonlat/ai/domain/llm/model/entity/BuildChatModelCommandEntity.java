package com.jasonlat.ai.domain.llm.model.entity;

import com.jasonlat.ai.domain.llm.model.valobj.McpConfigVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 构建对话模型命令
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuildChatModelCommandEntity {

    private String gatewayId;

    private McpConfigVO mcpConfigVO;

}
