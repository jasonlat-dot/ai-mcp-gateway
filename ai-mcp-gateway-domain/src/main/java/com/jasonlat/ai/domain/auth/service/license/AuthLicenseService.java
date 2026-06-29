package com.jasonlat.ai.domain.auth.service.license;

import com.jasonlat.ai.domain.auth.adapter.repository.IAuthRepository;
import com.jasonlat.ai.domain.auth.model.entity.LicenseCommandEntity;
import com.jasonlat.ai.domain.auth.model.valobj.McpGatewayAuthVO;
import com.jasonlat.ai.domain.auth.model.valobj.enums.AuthStatusEnum;
import com.jasonlat.ai.domain.auth.service.IAuthLicenseService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 证书授权服务
 * @author jasonlat
 * 2026-06-29  20:31
 */
@Slf4j
@Service
public class AuthLicenseService implements IAuthLicenseService {

    @Resource
    private IAuthRepository repository;

    /**
     * 验证授权
     *
     * @param commandEntity 入参信息
     * @return true:授权成功 false:授权失败
     */
    @Override
    public boolean checkLicense(LicenseCommandEntity commandEntity) {
        AuthStatusEnum.GatewayConfig gatewayAuthStatus = repository.queryGatewayAuthStatus(commandEntity.getGatewayId());
        if (AuthStatusEnum.GatewayConfig.NOT_VERIFIED.equals(gatewayAuthStatus)) {
            log.info("网关Id: {} 未配置强校验，无须校验apiKey", commandEntity.getGatewayId());
            return true;
        }
        // 配置了强校验，校验apiKey信息
        McpGatewayAuthVO gatewayAuthVO = repository.queryEffectiveGatewayAuthInfo(commandEntity);
        if (gatewayAuthVO == null) {
            log.info("网关Id: {} apiKey: {} 无效", commandEntity.getGatewayId(), commandEntity.getApiKey());
            return false;
        }
        // 是否被禁用
        if (AuthStatusEnum.AuthConfig.DISABLE.equals(gatewayAuthVO.getStatus())) {
            log.info("网关Id: {} apiKey: {} 已禁用", commandEntity.getGatewayId(), commandEntity.getApiKey());
            return false;
        }
        // 是否过期
        if (gatewayAuthVO.getExpireTime() == null) {
            log.info("网关Id: {} apiKey: {} 无过期时间", commandEntity.getGatewayId(), commandEntity.getApiKey());
            return true;
        }
        if (gatewayAuthVO.getExpireTime().before(new Date())) {
            log.info("网关Id: {} apiKey: {} 已过期, 过期时间：{}", commandEntity.getGatewayId(), commandEntity.getApiKey(), gatewayAuthVO.getExpireTime());
            return false;
        }
        log.info("网关Id: {} apiKey: {} 验证成功", commandEntity.getGatewayId(), commandEntity.getApiKey());
        return true;
    }
}
