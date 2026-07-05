package com.jasonlat.ai.cases.admin;


import com.jasonlat.ai.domain.auth.model.entity.RegisterCommandEntity;

/**
 * 认证配置管理
 */
public interface IAdminGatewayAuthService {

    void saveGatewayAuth(RegisterCommandEntity commandEntity);

}
