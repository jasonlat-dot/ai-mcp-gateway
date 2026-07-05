package com.jasonlat.ai.domain.admin.service;

import com.jasonlat.ai.domain.admin.model.entity.GatewayConfigEntity;

import java.util.List;


public interface IAdminService {

    List<GatewayConfigEntity> queryGatewayConfigList();

}
