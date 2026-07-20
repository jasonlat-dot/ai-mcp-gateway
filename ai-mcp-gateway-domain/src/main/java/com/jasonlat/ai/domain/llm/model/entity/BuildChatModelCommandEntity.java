package com.jasonlat.ai.domain.llm.model.entity;

import com.jasonlat.ai.domain.llm.model.valobj.McpConfigVO;
import com.jasonlat.ai.domain.llm.model.valobj.McpTypeEnumVO;
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

    /**
     * mcp 类型。见 McpTypeEnum，支持 SSE（默认）、STREAMABLE
     * 若为 null 或未指定，默认使用 SSE。
     */
    private McpTypeEnumVO mcpType;

}
