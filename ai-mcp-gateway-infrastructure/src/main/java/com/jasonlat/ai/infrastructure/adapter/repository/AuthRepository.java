package com.jasonlat.ai.infrastructure.adapter.repository;

import com.jasonlat.ai.domain.auth.adapter.repository.IAuthRepository;
import com.jasonlat.ai.domain.auth.model.entity.LicenseCommandEntity;
import com.jasonlat.ai.domain.auth.model.valobj.McpGatewayAuthVO;
import com.jasonlat.ai.domain.auth.model.valobj.enums.AuthStatusEnum;
import com.jasonlat.ai.infrastructure.dao.IMcpGatewayAuthDao;
import com.jasonlat.ai.infrastructure.dao.IMcpGatewayDao;
import com.jasonlat.ai.infrastructure.dao.po.McpGatewayAuthPO;
import com.jasonlat.ai.infrastructure.dao.po.McpGatewayPO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

/**
 * 鉴权仓储
 * @author jasonlat
 * 2026-06-29  20:40
 */
@Repository
public class AuthRepository implements IAuthRepository {

    @Resource
    private IMcpGatewayAuthDao mcpGatewayAuthDao;

    @Resource
    private IMcpGatewayDao mcpGatewayDao;

    /**
     * 查询有效的网关授权信息
     *
     * @param commandEntity 查询授权信息命令
     * @return 网关授权信息
     */
    @Override
    public McpGatewayAuthVO queryEffectiveGatewayAuthInfo(LicenseCommandEntity commandEntity) {
        McpGatewayAuthPO poReq = new McpGatewayAuthPO();
        poReq.setGatewayId(commandEntity.getGatewayId());
        poReq.setApiKey(commandEntity.getApiKey());

        McpGatewayAuthPO mcpGatewayAuthPO = mcpGatewayAuthDao.queryMcpGatewayAuthPO(poReq);
        if (null == mcpGatewayAuthPO) return null;

        return McpGatewayAuthVO.builder()
                .gatewayId(mcpGatewayAuthPO.getGatewayId())
                .apiKey(mcpGatewayAuthPO.getApiKey())
                .rateLimit(mcpGatewayAuthPO.getRateLimit())
                .expireTime(mcpGatewayAuthPO.getExpireTime())
                .status(AuthStatusEnum.AuthConfig.get(mcpGatewayAuthPO.getStatus()))
                .build();
    }

    /**
     * 查询网关授权状态
     *
     * @param gatewayId 网关Id
     * @return 网关授权状态
     */
    @Override
    public AuthStatusEnum.GatewayConfig queryGatewayAuthStatus(String gatewayId) {
        McpGatewayPO mcpGatewayPO = mcpGatewayDao.queryMcpGatewayByGatewayId(gatewayId);
        return AuthStatusEnum.GatewayConfig.get(mcpGatewayPO.getAuth());
    }

    /**
     * 插入一条网关授权信息
     *
     * @param mcpGatewayAuthVO 网关授权信息
     */
    @Override
    public void insertOne(McpGatewayAuthVO mcpGatewayAuthVO) {
        McpGatewayAuthPO mcpGatewayAuthPO = McpGatewayAuthPO.builder()
                .gatewayId(mcpGatewayAuthVO.getGatewayId())
                .apiKey(mcpGatewayAuthVO.getApiKey())
                .rateLimit(mcpGatewayAuthVO.getRateLimit())
                .expireTime(mcpGatewayAuthVO.getExpireTime())
                .status(mcpGatewayAuthVO.getStatus().getCode())
                .build();
        mcpGatewayAuthDao.insert(mcpGatewayAuthPO);
    }
}
