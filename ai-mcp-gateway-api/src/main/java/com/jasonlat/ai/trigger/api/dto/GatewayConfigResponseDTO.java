package com.jasonlat.ai.trigger.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 网关配置应答对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GatewayConfigResponseDTO {

    private Boolean success;

}
