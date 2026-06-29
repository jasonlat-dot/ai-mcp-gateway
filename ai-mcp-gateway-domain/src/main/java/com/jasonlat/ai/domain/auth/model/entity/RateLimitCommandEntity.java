package com.jasonlat.ai.domain.auth.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RateLimitCommandEntity {

    /**
     * 网关ID
     */
    private String gatewayId;

    /**
     * API密钥
     */
    private String apiKey;

}
