package com.jasonlat.ai.cases.admin;


import com.jasonlat.ai.domain.protocol.model.entity.StorageCommandEntity;

/**
 * 协议配置管理
 */
public interface IAdminProtocolService {

    void saveGatewayProtocol(StorageCommandEntity commandEntity);

}
