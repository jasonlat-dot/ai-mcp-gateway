package com.jasonlat.ai.domain.gateway.service.gateway;

import com.jasonlat.ai.domain.gateway.adapter.repository.IGatewayRepository;
import com.jasonlat.ai.domain.gateway.model.entity.GatewayConfigCommandEntity;
import com.jasonlat.ai.domain.gateway.model.valobj.GatewayConfigVO;
import com.jasonlat.ai.domain.gateway.service.IGatewayConfigService;
import com.jasonlat.ai.types.enums.ResponseCode;
import com.jasonlat.ai.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 网关配置服务
 */
@Slf4j
@Service
public class GatewayConfigService implements IGatewayConfigService {

    @Resource
    private IGatewayRepository repository;

    @Override
    public void saveGatewayConfig(GatewayConfigCommandEntity commandEntity) {
        repository.saveGatewayConfig(commandEntity);
    }

    @Override
    public void updateGatewayConfig(GatewayConfigCommandEntity commandEntity) {
        GatewayConfigVO gatewayConfigVO = commandEntity.getGatewayConfigVO();
        if (StringUtils.isBlank(gatewayConfigVO.getGatewayId())) {
            log.error("更新网关配置失败: gatewayId为空");
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER);
        }
        repository.updateGatewayConfig(commandEntity);
    }

    @Override
    public void updateGatewayAuthStatus(GatewayConfigCommandEntity commandEntity) {
        GatewayConfigVO gatewayConfigVO = commandEntity.getGatewayConfigVO();
        if (gatewayConfigVO.getAuth() == null) {
            log.error("网关配置服务：网关授权状态不能为空");
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER);
        }
        if (StringUtils.isAnyBlank(gatewayConfigVO.getGatewayId(), gatewayConfigVO.getNewVersion(), gatewayConfigVO.getVersion())) {
            log.error("网关配置服务：网关ID、版本号、新版本号不能为空");
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER);
        }
        repository.updateGatewayAuthStatus(commandEntity);
    }

    @Override
    public boolean deleteGatewayConfig(String gatewayId) {
        if (StringUtils.isBlank(gatewayId)) {
            log.error("删除网关配置失败: gatewayId为空");
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER);
        }
        // 引用校验: 网关下若还挂有工具 / API Key, 不允许删除
        long authCount = repository.countAuthsByGatewayId(gatewayId);
        if (authCount > 0) {
            String detail = String.format(
                "网关 [%s] 还被引用:  API Key %d 个;请先到「工具管理」「认证管理」删除或转移以上子资源再删除。",
                gatewayId, authCount);
            log.warn("删除网关被拒绝 gatewayId: {}  authCount: {}", gatewayId, authCount);
            throw new AppException(ResponseCode.GATEWAY_IN_USE.getCode(), detail);
        }
        long toolCount = repository.countToolsByGatewayId(gatewayId);
        if (toolCount > 0) {
            String detail = String.format(
                "网关 [%s] 还被引用:  工具 %d 个;请先到「工具管理」删除或转移以上子资源再删除。",
                gatewayId, toolCount);
            log.warn("删除网关被拒绝 gatewayId: {}  toolCount: {}", gatewayId, toolCount);
            throw new AppException(ResponseCode.GATEWAY_IN_USE.getCode(), detail);
        }

        // 0 行(记录不存在)视为幂等成功,不抛异常
        int rows = repository.deleteGatewayConfigByGatewayId(gatewayId);
        return rows >= 0;
    }
}
