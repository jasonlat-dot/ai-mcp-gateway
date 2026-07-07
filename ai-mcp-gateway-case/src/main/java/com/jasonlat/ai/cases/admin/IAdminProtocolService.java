package com.jasonlat.ai.cases.admin;


import com.jasonlat.ai.domain.protocol.model.entity.AnalysisCommandEntity;
import com.jasonlat.ai.domain.protocol.model.entity.StorageCommandEntity;
import com.jasonlat.ai.domain.protocol.model.valobj.http.HTTPProtocolVO;

/**
 * 协议配置管理
 */
public interface IAdminProtocolService {


    void saveGatewayProtocol(StorageCommandEntity commandEntity);

    void updateGatewayProtocol(StorageCommandEntity commandEntity);

    void deleteGatewayProtocol(Long protocolId);

    void importGatewayProtocol(AnalysisCommandEntity commandEntity);

    java.util.List<HTTPProtocolVO> analysisProtocol(AnalysisCommandEntity commandEntity);


}
