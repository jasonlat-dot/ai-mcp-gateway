package com.jasonlat.ai.cases.admin.protocol;


import com.jasonlat.ai.cases.admin.IAdminProtocolService;
import com.jasonlat.ai.domain.protocol.model.entity.StorageCommandEntity;
import com.jasonlat.ai.domain.protocol.service.IProtocolStorageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 协议配置管理
 */
@Slf4j
@Service
public class AdminProtocolService implements IAdminProtocolService {

    @Resource
    private IProtocolStorageService protocolStorage;

    @Override
    public void saveGatewayProtocol(StorageCommandEntity commandEntity) {
        protocolStorage.storage(commandEntity);
    }

}
