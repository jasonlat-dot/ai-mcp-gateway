package com.jasonlat.ai.infrastructure.adapter.repository;


import com.jasonlat.ai.domain.gateway.adapter.repository.IGatewayRepository;
import com.jasonlat.ai.domain.gateway.model.entity.GatewayConfigCommandEntity;
import com.jasonlat.ai.domain.gateway.model.entity.GatewayToolConfigCommandEntity;
import com.jasonlat.ai.domain.gateway.model.valobj.GatewayConfigVO;
import com.jasonlat.ai.domain.gateway.model.valobj.GatewayToolConfigVO;
import com.jasonlat.ai.infrastructure.dao.IMcpGatewayDao;
import com.jasonlat.ai.infrastructure.dao.IMcpGatewayToolDao;
import com.jasonlat.ai.infrastructure.dao.po.McpGatewayPO;
import com.jasonlat.ai.infrastructure.dao.po.McpGatewayToolPO;
import com.jasonlat.ai.types.enums.GatewayEnum;
import com.jasonlat.ai.types.exception.AppException;
import com.jasonlat.ai.types.snow.SnowflakeIdGenerator;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static com.jasonlat.ai.types.enums.ResponseCode.DB_UPDATE_FAIL;

/**
 * 网关仓储服务实现
 */
@Slf4j
@Repository
public class GatewayRepository implements IGatewayRepository {

    @Resource
    private IMcpGatewayDao mcpGatewayDao;

    @Resource
    private IMcpGatewayToolDao mcpGatewayToolDao;

    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    public void saveGatewayConfig(GatewayConfigCommandEntity commandEntity) {
        GatewayConfigVO gatewayConfigVO = commandEntity.getGatewayConfigVO();

        McpGatewayPO mcpGatewayPO = new McpGatewayPO();
        mcpGatewayPO.setGatewayId(gatewayConfigVO.getGatewayId());
        mcpGatewayPO.setGatewayName(gatewayConfigVO.getGatewayName());
        mcpGatewayPO.setGatewayDesc(gatewayConfigVO.getGatewayDesc());
        mcpGatewayPO.setVersion(gatewayConfigVO.getVersion());
        mcpGatewayPO.setAuth(null != gatewayConfigVO.getAuth() ? gatewayConfigVO.getAuth().getCode() : GatewayEnum.GatewayAuthStatusEnum.NOT_VERIFIED.getCode());
        mcpGatewayPO.setStatus(null != gatewayConfigVO.getStatus() ? gatewayConfigVO.getStatus().getCode() : GatewayEnum.GatewayStatus.ENABLE.getCode());
        mcpGatewayDao.insert(mcpGatewayPO);
    }

    @Override
    public void updateGatewayConfig(GatewayConfigCommandEntity commandEntity) {
        GatewayConfigVO gatewayConfigVO = commandEntity.getGatewayConfigVO();

        McpGatewayPO mcpGatewayPO = new McpGatewayPO();
        mcpGatewayPO.setGatewayId(gatewayConfigVO.getGatewayId());
        mcpGatewayPO.setGatewayName(gatewayConfigVO.getGatewayName());
        mcpGatewayPO.setGatewayDesc(gatewayConfigVO.getGatewayDesc());
        mcpGatewayPO.setVersion(gatewayConfigVO.getVersion());
        mcpGatewayPO.setAuth(null != gatewayConfigVO.getAuth() ? gatewayConfigVO.getAuth().getCode() : null);
        mcpGatewayPO.setStatus(null != gatewayConfigVO.getStatus() ? gatewayConfigVO.getStatus().getCode() : null);
        mcpGatewayPO.setUpdateTime(new Date());

        int count = mcpGatewayDao.updateByGatewayId(mcpGatewayPO);
        if (1 != count) {
            throw new AppException(DB_UPDATE_FAIL.getCode(), DB_UPDATE_FAIL.getInfo());
        }
    }

    @Override
    public void updateGatewayAuthStatus(GatewayConfigCommandEntity commandEntity) {
        GatewayConfigVO gatewayConfigVO = commandEntity.getGatewayConfigVO();
        if (null == gatewayConfigVO.getAuth()) {
            return;
        }

        McpGatewayPO mcpGatewayPO = new McpGatewayPO();
        mcpGatewayPO.setGatewayId(gatewayConfigVO.getGatewayId());
        mcpGatewayPO.setAuth(null != gatewayConfigVO.getAuth() ? gatewayConfigVO.getAuth().getCode() : null);
        mcpGatewayPO.setUpdateTime(new Date());
        mcpGatewayPO.setVersion(gatewayConfigVO.getNewVersion());
        int count = mcpGatewayDao.updateAuthStatusByGatewayId(mcpGatewayPO);
        if (1 != count) {
            throw new AppException(DB_UPDATE_FAIL.getCode(), DB_UPDATE_FAIL.getInfo());
        }
    }

    @Override
    public void saveGatewayToolConfig(GatewayToolConfigCommandEntity commandEntity) {
        GatewayToolConfigVO gatewayToolConfigVO = commandEntity.getGatewayToolConfigVO();

        McpGatewayToolPO mcpGatewayToolPO = new McpGatewayToolPO();
        mcpGatewayToolPO.setId(snowflakeIdGenerator.nextId());
        mcpGatewayToolPO.setGatewayId(gatewayToolConfigVO.getGatewayId());
        mcpGatewayToolPO.setToolId(snowflakeIdGenerator.nextId());
        mcpGatewayToolPO.setToolName(gatewayToolConfigVO.getToolName());
        mcpGatewayToolPO.setToolType(gatewayToolConfigVO.getToolType());
        mcpGatewayToolPO.setToolDescription(gatewayToolConfigVO.getToolDescription());
        mcpGatewayToolPO.setToolVersion(gatewayToolConfigVO.getToolVersion());
        mcpGatewayToolPO.setProtocolId(gatewayToolConfigVO.getProtocolId());
        mcpGatewayToolPO.setProtocolType(gatewayToolConfigVO.getProtocolType());
        mcpGatewayToolDao.insert(mcpGatewayToolPO);
    }

    @Override
    public void updateGatewayToolConfig(GatewayToolConfigCommandEntity commandEntity) {
        GatewayToolConfigVO gatewayToolConfigVO = commandEntity.getGatewayToolConfigVO();

        McpGatewayToolPO mcpGatewayToolPO = new McpGatewayToolPO();
        mcpGatewayToolPO.setGatewayId(gatewayToolConfigVO.getGatewayId());
        mcpGatewayToolPO.setToolId(gatewayToolConfigVO.getToolId());
        mcpGatewayToolPO.setToolName(gatewayToolConfigVO.getToolName());
        mcpGatewayToolPO.setToolType(gatewayToolConfigVO.getToolType());
        mcpGatewayToolPO.setToolDescription(gatewayToolConfigVO.getToolDescription());
        mcpGatewayToolPO.setToolVersion(gatewayToolConfigVO.getToolVersion());
        mcpGatewayToolPO.setProtocolId(gatewayToolConfigVO.getProtocolId());
        mcpGatewayToolPO.setProtocolType(gatewayToolConfigVO.getProtocolType());
        mcpGatewayToolPO.setUpdateTime(new Date());

        int count = mcpGatewayToolDao.updateToolConfigByToolId(mcpGatewayToolPO);
        if (1 != count) {
            throw new AppException(DB_UPDATE_FAIL.getCode(), DB_UPDATE_FAIL.getInfo());
        }
    }

    @Override
    public void updateGatewayToolProtocol(GatewayToolConfigCommandEntity commandEntity) {
        GatewayToolConfigVO gatewayToolConfigVO = commandEntity.getGatewayToolConfigVO();

        McpGatewayToolPO mcpGatewayToolPO = new McpGatewayToolPO();
        mcpGatewayToolPO.setGatewayId(gatewayToolConfigVO.getGatewayId());
        mcpGatewayToolPO.setProtocolId(gatewayToolConfigVO.getProtocolId());
        mcpGatewayToolPO.setProtocolType(gatewayToolConfigVO.getProtocolType());

        int count = mcpGatewayToolDao.updateProtocolByGatewayId(mcpGatewayToolPO);
        if (1 != count) {
            throw new AppException(DB_UPDATE_FAIL.getCode(), DB_UPDATE_FAIL.getInfo());
        }
    }

    @Override
    public void deleteGatewayToolConfig(Long toolId) {
        mcpGatewayToolDao.deleteByToolId(toolId);
    }

}
