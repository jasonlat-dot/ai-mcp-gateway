package com.jasonlat.ai.domain.gateway.service.tool;

import com.jasonlat.ai.domain.gateway.adapter.repository.IGatewayRepository;
import com.jasonlat.ai.domain.gateway.model.entity.GatewayToolConfigCommandEntity;
import com.jasonlat.ai.domain.gateway.model.valobj.GatewayToolConfigVO;
import com.jasonlat.ai.domain.gateway.service.IGatewayToolConfigService;
import com.jasonlat.ai.types.enums.ResponseCode;
import com.jasonlat.ai.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 网关工具配置服务实现
 */
@Slf4j
@Service
public class GatewayToolConfigService implements IGatewayToolConfigService {

    @Resource
    private IGatewayRepository repository;

    @Override
    public void saveGatewayToolConfig(GatewayToolConfigCommandEntity commandEntity) {
        GatewayToolConfigVO gatewayToolConfigVO = commandEntity.getGatewayToolConfigVO();
        if (StringUtils.isAnyBlank(gatewayToolConfigVO.getGatewayId(), gatewayToolConfigVO.getToolName(),
                gatewayToolConfigVO.getToolType(), gatewayToolConfigVO.getToolVersion(),
                gatewayToolConfigVO.getProtocolType(), gatewayToolConfigVO.getToolDescription())
                || gatewayToolConfigVO.getProtocolId() == null || gatewayToolConfigVO.getGatewayId() == null) {
            log.error("保存工具配置失败: gatewayId或toolName或toolType或toolVersion为空");
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER);
        }
        repository.saveGatewayToolConfig(commandEntity);
    }

    @Override
    public void updateGatewayToolConfig(GatewayToolConfigCommandEntity commandEntity) {
        GatewayToolConfigVO gatewayToolConfigVO = commandEntity.getGatewayToolConfigVO();
        if (gatewayToolConfigVO.getToolId() == null) {
            log.error("更新工具配置失败: gatewayId为空");
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER);
        }
        repository.updateGatewayToolConfig(commandEntity);
    }

    @Override
    public void updateGatewayToolProtocol(GatewayToolConfigCommandEntity commandEntity) {
        GatewayToolConfigVO gatewayToolConfigVO = commandEntity.getGatewayToolConfigVO();
        if (StringUtils.isBlank(gatewayToolConfigVO.getGatewayId()) || gatewayToolConfigVO.getProtocolId() == null) {
            log.error("更新工具协议失败: gatewayId或protocolId为空");
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER);
        }
        repository.updateGatewayToolProtocol(commandEntity);
    }

    @Override
    public void deleteGatewayToolConfig(Long toolId) {
        repository.deleteGatewayToolConfig(toolId);
    }
}
