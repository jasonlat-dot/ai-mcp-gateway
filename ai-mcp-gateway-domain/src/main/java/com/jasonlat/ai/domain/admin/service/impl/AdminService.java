package com.jasonlat.ai.domain.admin.service.impl;

import com.jasonlat.ai.domain.admin.adapter.respository.IAdminRepository;
import com.jasonlat.ai.domain.admin.model.entity.GatewayConfigEntity;
import com.jasonlat.ai.domain.admin.service.IAdminService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService implements IAdminService {

    @Resource
    private IAdminRepository adminRepository;

    @Override
    public List<GatewayConfigEntity> queryGatewayConfigList() {
        return adminRepository.queryGatewayConfigList();
    }

}