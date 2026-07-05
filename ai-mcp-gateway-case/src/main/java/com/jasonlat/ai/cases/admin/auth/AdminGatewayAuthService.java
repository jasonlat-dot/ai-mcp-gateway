package com.jasonlat.ai.cases.admin.auth;

import com.jasonlat.ai.cases.admin.IAdminGatewayAuthService;
import com.jasonlat.ai.domain.auth.model.entity.RegisterCommandEntity;
import com.jasonlat.ai.domain.auth.service.IAuthRegisterService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 认证配置管理
 */
@Slf4j
@Service
public class AdminGatewayAuthService implements IAdminGatewayAuthService {

    @Resource
    private IAuthRegisterService authRegisterService;

    @Override
    public void saveGatewayAuth(RegisterCommandEntity commandEntity) {
        authRegisterService.register(commandEntity);
    }

}
