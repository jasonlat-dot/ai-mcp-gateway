package com.jasonlat.ai.cases.admin;

import com.jasonlat.ai.domain.admin.model.entity.*;

import java.util.List;

/**
 * 运营管理
 */
public interface IAdminManageService {

    List<GatewayConfigEntity> queryGatewayConfigList();

    GatewayConfigPageEntity queryGatewayConfigPage(GatewayConfigQueryEntity queryEntity);

    List<GatewayToolConfigEntity> queryGatewayToolList();

    GatewayToolPageEntity queryGatewayToolPage(GatewayToolQueryEntity queryEntity);

    List<GatewayToolConfigEntity> queryGatewayToolListByGatewayId(String gatewayId);

    List<GatewayProtocolConfigEntity> queryGatewayProtocolList();

    GatewayProtocolPageEntity queryGatewayProtocolPage(GatewayProtocolQueryEntity queryEntity);

    List<GatewayProtocolConfigEntity> queryGatewayProtocolListByGatewayId(String gatewayId);

    List<DubboProtocolConfigEntity> queryDubboProtocolList();

    DubboProtocolPageEntity queryDubboProtocolPage(DubboProtocolQueryEntity queryEntity);

    List<DubboProtocolConfigEntity> queryDubboProtocolListByGatewayId(String gatewayId);

    List<GatewayAuthConfigEntity> queryGatewayAuthList();

    GatewayAuthPageEntity queryGatewayAuthPage(GatewayAuthQueryEntity queryEntity);


}