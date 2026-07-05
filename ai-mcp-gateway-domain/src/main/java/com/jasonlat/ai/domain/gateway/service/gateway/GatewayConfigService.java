package com.jasonlat.ai.domain.gateway.service.gateway;

import com.jasonlat.ai.domain.gateway.adapter.repository.IGatewayRepository;
import com.jasonlat.ai.domain.gateway.model.entity.GatewayConfigCommandEntity;
import com.jasonlat.ai.domain.gateway.model.valobj.GatewayConfigVO;
import com.jasonlat.ai.domain.gateway.service.IGatewayConfigService;
import com.jasonlat.ai.types.enums.ResponseCode;
import com.jasonlat.ai.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 网关配置服务
 */
@Slf4j
@Service
public class GatewayConfigService implements IGatewayConfigService {

    @Resource
    private IGatewayRepository repository;

    @Override
    public void saveGatewayConfig(GatewayConfigCommandEntity commandEntity) {
        repository.saveGatewayConfig(commandEntity);
    }

    @Override
    public void updateGatewayAuthStatus(GatewayConfigCommandEntity commandEntity) {
        GatewayConfigVO gatewayConfigVO = commandEntity.getGatewayConfigVO();
        if (gatewayConfigVO.getAuth() == null) {
            log.error("网关配置服务：网关授权状态不能为空");
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER);
        }
        if (StringUtils.isAnyBlank(gatewayConfigVO.getGatewayId(), gatewayConfigVO.getNewVersion(), gatewayConfigVO.getVersion())) {
            log.error("网关配置服务：网关ID、版本号、新版本号不能为空");
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER);
        }
        repository.updateGatewayAuthStatus(commandEntity);
    }
}
