package com.jasonlat.ai.cases.admin;

import com.jasonlat.ai.domain.admin.model.entity.GatewayConfigEntity;

import java.util.List;

/**
 * 运营管理
 */
public interface IAdminManageService {

    List<GatewayConfigEntity> queryGatewayConfigList();

}