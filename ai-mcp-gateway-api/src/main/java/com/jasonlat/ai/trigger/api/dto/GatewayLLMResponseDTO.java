package com.jasonlat.ai.trigger.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 大模型应答结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatewayLLMResponseDTO {

    private String content;

}
