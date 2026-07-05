package com.jasonlat.ai.domain.gateway.model.entity;

import com.jasonlat.ai.domain.gateway.model.valobj.GatewayConfigVO;
import com.jasonlat.ai.types.enums.GatewayEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 网关配置实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatewayConfigCommandEntity {

    private GatewayConfigVO gatewayConfigVO;

    public static GatewayConfigCommandEntity buildUpdateGatewayAuthStatusVO(String gatewayId, GatewayEnum.GatewayAuthStatusEnum auth, String  version) {
        return GatewayConfigCommandEntity.builder()
                .gatewayConfigVO(GatewayConfigVO.builder()
                        .gatewayId(gatewayId)
                        .auth(auth)
                        .version(version)
                        .build())
                .build();
    }

}
