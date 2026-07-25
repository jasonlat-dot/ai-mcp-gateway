package com.jasonlat.ai.domain.admin.adapter.respository;

import com.jasonlat.ai.domain.admin.model.entity.*;

import java.util.List;

public interface IAdminRepository {

    List<GatewayConfigEntity> queryGatewayConfigList();

    GatewayConfigPageEntity queryGatewayConfigPage(GatewayConfigQueryEntity queryEntity);

    List<GatewayToolConfigEntity> queryGatewayToolList();

    GatewayToolPageEntity queryGatewayToolPage(GatewayToolQueryEntity queryEntity);

    List<GatewayToolConfigEntity> queryGatewayToolListByGatewayId(String gatewayId);

    List<GatewayProtocolConfigEntity> queryGatewayProtocolList();

    GatewayProtocolPageEntity queryGatewayProtocolPage(GatewayProtocolQueryEntity queryEntity);

    List<GatewayProtocolConfigEntity> queryGatewayProtocolListByProtocolIds(List<Long> protocolIds);

    List<DubboProtocolConfigEntity> queryDubboProtocolList();

    DubboProtocolPageEntity queryDubboProtocolPage(DubboProtocolQueryEntity queryEntity);

    List<DubboProtocolConfigEntity> queryDubboProtocolListByProtocolIds(List<Long> protocolIds);

    List<GatewayAuthConfigEntity> queryGatewayAuthList();

    GatewayAuthPageEntity queryGatewayAuthPage(GatewayAuthQueryEntity queryEntity);

}
