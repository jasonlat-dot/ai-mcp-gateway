package com.jasonlat.ai.cases.admin.manage;

import com.jasonlat.ai.cases.admin.IAdminManageService;
import com.jasonlat.ai.domain.admin.model.entity.GatewayConfigEntity;
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

}