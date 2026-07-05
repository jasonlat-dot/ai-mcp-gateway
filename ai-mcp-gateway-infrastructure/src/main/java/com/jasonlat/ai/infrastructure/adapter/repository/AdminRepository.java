package com.jasonlat.ai.infrastructure.adapter.repository;

import com.jasonlat.ai.domain.admin.adapter.respository.IAdminRepository;
import com.jasonlat.ai.domain.admin.model.entity.GatewayConfigEntity;
import com.jasonlat.ai.infrastructure.dao.*;
import com.jasonlat.ai.infrastructure.dao.po.McpGatewayPO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class AdminRepository implements IAdminRepository {

    @Resource
    private IMcpGatewayAuthDao mcpGatewayAuthDao;

    @Resource
    private IMcpGatewayDao mcpGatewayDao;

    @Resource
    private IMcpGatewayToolDao mcpGatewayToolDao;

    @Resource
    private IMcpProtocolHttpDao protocolHttpDao;

    @Resource
    private IMcpProtocolMappingDao protocolMappingDao;

    @Override
    public List<GatewayConfigEntity> queryGatewayConfigList() {
        List<McpGatewayPO> mcpGatewayPOS = mcpGatewayDao.queryAll();
        return mcpGatewayPOS.stream().map(po -> GatewayConfigEntity.builder()
                .gatewayId(po.getGatewayId())
                .gatewayName(po.getGatewayName())
                .gatewayDesc(po.getGatewayDesc())
                .version(po.getVersion())
                .auth(po.getAuth())
                .status(po.getStatus())
                .build()).collect(Collectors.toList());
    }

}
