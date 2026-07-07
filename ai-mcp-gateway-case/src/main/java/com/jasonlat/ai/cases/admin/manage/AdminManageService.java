package com.jasonlat.ai.cases.admin.manage;

import com.jasonlat.ai.cases.admin.IAdminManageService;
import com.jasonlat.ai.domain.admin.model.entity.*;
import com.jasonlat.ai.domain.admin.service.IAdminService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 运营管理实现
 */
@Slf4j
@Service
public class AdminManageService implements IAdminManageService {

    @Resource
    private IAdminService adminService;

    @Override
    public List<GatewayConfigEntity> queryGatewayConfigList() {
        return adminService.queryGatewayConfigList();
    }

    @Override
    public GatewayConfigPageEntity queryGatewayConfigPage(GatewayConfigQueryEntity queryEntity) {
        return adminService.queryGatewayConfigPage(queryEntity);
    }

    @Override
    public List<GatewayToolConfigEntity> queryGatewayToolList() {
        return adminService.queryGatewayToolList();
    }

    @Override
    public GatewayToolPageEntity queryGatewayToolPage(GatewayToolQueryEntity queryEntity) {
        return adminService.queryGatewayToolPage(queryEntity);
    }

    @Override
    public List<GatewayToolConfigEntity> queryGatewayToolListByGatewayId(String gatewayId) {
        return adminService.queryGatewayToolListByGatewayId(gatewayId);
    }

    @Override
    public List<GatewayProtocolConfigEntity> queryGatewayProtocolList() {
        return adminService.queryGatewayProtocolList();
    }

    @Override
    public GatewayProtocolPageEntity queryGatewayProtocolPage(GatewayProtocolQueryEntity queryEntity) {
        return adminService.queryGatewayProtocolPage(queryEntity);
    }

    @Override
    public List<GatewayProtocolConfigEntity> queryGatewayProtocolListByGatewayId(String gatewayId) {
        return adminService.queryGatewayProtocolListByGatewayId(gatewayId);
    }

    @Override
    public List<GatewayAuthConfigEntity> queryGatewayAuthList() {
        return adminService.queryGatewayAuthList();
    }

    @Override
    public GatewayAuthPageEntity queryGatewayAuthPage(GatewayAuthQueryEntity queryEntity) {
        return adminService.queryGatewayAuthPage(queryEntity);
    }

}