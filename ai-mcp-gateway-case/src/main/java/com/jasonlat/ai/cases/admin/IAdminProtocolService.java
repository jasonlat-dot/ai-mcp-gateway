package com.jasonlat.ai.cases.admin;


import com.jasonlat.ai.domain.protocol.model.entity.AnalysisCommandEntity;
import com.jasonlat.ai.domain.protocol.model.entity.StorageCommandEntity;
import com.jasonlat.ai.domain.protocol.model.valobj.dubbo.DubboProtocolVO;
import com.jasonlat.ai.domain.protocol.model.valobj.http.HTTPProtocolVO;

import java.util.List;

/**
 * 协议配置管理
 */
public interface IAdminProtocolService {


    void saveGatewayProtocol(StorageCommandEntity commandEntity);

    void updateGatewayProtocol(StorageCommandEntity commandEntity);

    void deleteGatewayProtocol(Long protocolId);

    void importGatewayProtocol(AnalysisCommandEntity commandEntity);

    List<HTTPProtocolVO> analysisProtocol(AnalysisCommandEntity commandEntity);

    /**
     * 导入 Dubbo 协议(dubbo-api-docs JSON → 落库)。
     * <p>
     * 走 IDubboApiDocsAnalysisService 而不是 SPI strategy。
     */
    void importDubboGatewayProtocol(AnalysisCommandEntity commandEntity);

    /**
     * 解析 Dubbo 协议,仅产 VO 不落库,供前端预览。
     */
    List<DubboProtocolVO> analysisDubboProtocol(AnalysisCommandEntity commandEntity);


}