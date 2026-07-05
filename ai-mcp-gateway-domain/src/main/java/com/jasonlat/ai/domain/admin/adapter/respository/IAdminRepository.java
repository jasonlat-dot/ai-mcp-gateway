package com.jasonlat.ai.domain.admin.adapter.respository;

import com.jasonlat.ai.domain.admin.model.entity.GatewayConfigEntity;

import java.util.List;

public interface IAdminRepository {

    List<GatewayConfigEntity> queryGatewayConfigList();

}
