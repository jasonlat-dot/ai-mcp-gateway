package com.jasonlat.ai.cases.admin.gateway;

import com.jasonlat.ai.cases.admin.IAdminGatewayService;
import com.jasonlat.ai.domain.gateway.model.entity.GatewayConfigCommandEntity;
import com.jasonlat.ai.domain.gateway.model.entity.GatewayToolConfigCommandEntity;
import com.jasonlat.ai.domain.gateway.service.IGatewayConfigService;
import com.jasonlat.ai.domain.gateway.service.IGatewayToolConfigService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 网关配置管理
 */
@Slf4j
@Service
public class AdminGatewayService implements IAdminGatewayService {

    @Resource
    private IGatewayConfigService gatewayConfigService;

    @Resource
    private IGatewayToolConfigService gatewayToolConfigService;

    @Override
    public void saveGatewayConfig(GatewayConfigCommandEntity commandEntity) {
        gatewayConfigService.saveGatewayConfig(commandEntity);
    }

    @Override
    public void updateGatewayConfig(GatewayConfigCommandEntity commandEntity) {
        gatewayConfigService.updateGatewayConfig(commandEntity);
    }

    @Override
    public void saveGatewayToolConfig(GatewayToolConfigCommandEntity commandEntity) {
        gatewayToolConfigService.saveGatewayToolConfig(commandEntity);
    }

    @Override
    public void updateGatewayToolConfig(GatewayToolConfigCommandEntity commandEntity) {
        gatewayToolConfigService.updateGatewayToolConfig(commandEntity);
    }

    @Override
    public void deleteGatewayToolConfig(Long toolId) {
        gatewayToolConfigService.deleteGatewayToolConfig(toolId);
    }

}
