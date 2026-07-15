package com.jasonlat.ai.domain.llm.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * mcp 配置值对象
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class McpConfigVO {

    private String baseUri;
    private String sseEndpoint;
    private String authApiKey;
    private Integer timeout;

}
