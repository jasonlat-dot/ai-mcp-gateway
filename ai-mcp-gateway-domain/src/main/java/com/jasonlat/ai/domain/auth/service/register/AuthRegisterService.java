package com.jasonlat.ai.domain.auth.service.register;

import com.jasonlat.ai.domain.auth.adapter.repository.IAuthRepository;
import com.jasonlat.ai.domain.auth.model.entity.RegisterCommandEntity;
import com.jasonlat.ai.domain.auth.model.valobj.McpGatewayAuthVO;
import com.jasonlat.ai.domain.auth.model.valobj.enums.AuthStatusEnum;
import com.jasonlat.ai.domain.auth.service.IAuthRegisterService;
import com.jasonlat.ai.types.enums.ResponseCode;
import com.jasonlat.ai.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 注册服务
 * @author jasonlat
 * 2026-06-29  20:30
 */
@Slf4j
@Service
public class AuthRegisterService implements IAuthRegisterService {

    @Resource
    private IAuthRepository repository;

    /**
     * 注册
     *
     * @param commandEntity 注册命令入参
     * @return 注册结果 apiKey
     */
    @Override
    public String register(RegisterCommandEntity commandEntity) {
        // 1. 生成 API Key | gw 网关缩写，方便区分
        String apiKey = "gw-" + RandomStringUtils.randomAlphanumeric(48);

        // 2. 构建聚合对象
        McpGatewayAuthVO mcpGatewayAuthVO = McpGatewayAuthVO.builder()
                .gatewayId(commandEntity.getGatewayId())
                .apiKey(apiKey)
                .rateLimit(commandEntity.getRateLimit())
                .expireTime(commandEntity.getExpireTime())
                .status(AuthStatusEnum.AuthConfig.ENABLE)
                .build();

        // 3. 保存数据
        repository.insertOne(mcpGatewayAuthVO);

        // 4. 返回结果
        return apiKey;
    }

    @Override
    public boolean updateGatewayAuth(RegisterCommandEntity commandEntity) {
        if (StringUtils.isBlank(commandEntity.getGatewayId())) {
            log.error("更新网关认证配置失败: gatewayId为空");
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER);
        }
        McpGatewayAuthVO mcpGatewayAuthVO = McpGatewayAuthVO.builder()
                .gatewayId(commandEntity.getGatewayId())
                .rateLimit(commandEntity.getRateLimit())
                .expireTime(commandEntity.getExpireTime())
                .build();
        return repository.updateGatewayAuth(mcpGatewayAuthVO);
    }

    @Override
    public void deleteGatewayAuth(String gatewayId) {
        repository.deleteGatewayAuth(gatewayId);
    }
}
