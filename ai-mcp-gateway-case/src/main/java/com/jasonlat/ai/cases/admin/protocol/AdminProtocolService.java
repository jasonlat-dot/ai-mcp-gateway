package com.jasonlat.ai.cases.admin.protocol;


import com.jasonlat.ai.cases.admin.IAdminProtocolService;
import com.jasonlat.ai.domain.protocol.model.entity.AnalysisCommandEntity;
import com.jasonlat.ai.domain.protocol.model.entity.StorageCommandEntity;
import com.jasonlat.ai.domain.protocol.model.valobj.dubbo.DubboProtocolVO;
import com.jasonlat.ai.domain.protocol.model.valobj.http.HTTPProtocolVO;
import com.jasonlat.ai.domain.protocol.service.IProtocolAnalysisService;
import com.jasonlat.ai.domain.protocol.service.IProtocolStorageService;
import com.jasonlat.ai.domain.protocol.service.dubbo.IDubboApiDocsAnalysisService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 协议配置管理
 */
@Slf4j
@Service
public class AdminProtocolService implements IAdminProtocolService {

    @Resource
    private IProtocolStorageService protocolStorage;

    @Resource
    private IProtocolAnalysisService protocolAnalysis;

    @Resource
    private IDubboApiDocsAnalysisService dubboAnalysis;

    @Override
    public void saveGatewayProtocol(StorageCommandEntity commandEntity) {
        protocolStorage.storage(commandEntity);
    }

    @Override
    public void updateGatewayProtocol(StorageCommandEntity commandEntity) {
        protocolStorage.updateGatewayProtocol(commandEntity);
    }

    @Override
    public void deleteGatewayProtocol(Long protocolId) {
        protocolStorage.deleteGatewayProtocol(protocolId);
    }

    @Override
    public void importGatewayProtocol(AnalysisCommandEntity commandEntity) {
        // 1. 协议解析
        List<HTTPProtocolVO> httpProtocolVOS = protocolAnalysis.analysis(commandEntity);

        // 2. 协议存储
        protocolStorage.storage(StorageCommandEntity.builder().httpProtocolVOS(httpProtocolVOS).build());
    }

    @Override
    public List<HTTPProtocolVO> analysisProtocol(AnalysisCommandEntity commandEntity) {
        return protocolAnalysis.analysis(commandEntity);
    }

    @Override
    public void importDubboGatewayProtocol(AnalysisCommandEntity commandEntity) {
        // 1. 解析
        List<DubboProtocolVO> dubboProtocolVOS = dubboAnalysis.analysis(commandEntity.getOpenApiJson());

        // 2. 落库
        protocolStorage.storage(StorageCommandEntity.builder().dubboProtocolVOS(dubboProtocolVOS).build());
    }

    @Override
    public List<DubboProtocolVO> analysisDubboProtocol(AnalysisCommandEntity commandEntity) {
        return dubboAnalysis.analysis(commandEntity.getOpenApiJson());
    }


}