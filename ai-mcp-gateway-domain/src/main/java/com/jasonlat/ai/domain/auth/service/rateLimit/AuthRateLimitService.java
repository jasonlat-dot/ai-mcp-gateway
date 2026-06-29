package com.jasonlat.ai.domain.auth.service.rateLimit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jasonlat.ai.domain.auth.adapter.repository.IAuthRepository;
import com.jasonlat.ai.domain.auth.model.entity.LicenseCommandEntity;
import com.jasonlat.ai.domain.auth.model.entity.RateLimitCommandEntity;
import com.jasonlat.ai.domain.auth.model.valobj.McpGatewayAuthVO;
import com.jasonlat.ai.domain.auth.service.IAuthRateLimitService;
import com.jasonlat.ai.domain.auth.service.rateLimit.patch.TokenBucketRateLimiter;
import com.jasonlat.ai.types.enums.ResponseCode;
import com.jasonlat.ai.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 鉴权限流服务
 * @author jasonlat
 * 2026-06-29  20:29
 */
@Slf4j
@Service
public class AuthRateLimitService implements IAuthRateLimitService {

    @Resource
    private IAuthRepository repository;


    private final Cache<String, TokenBucketRateLimiter> rateLimiterCache = CacheBuilder.newBuilder()
            // 写后过期，配置变更最多 N 分钟生效
            .expireAfterWrite(5, TimeUnit.MINUTES)
            // 兜底：即使没有被 access，1 小时后也清理（防止内存泄漏）
            .expireAfterAccess(1, TimeUnit.HOURS)
            .maximumSize(10_000)
            .removalListener((notification) -> {
                log.debug("限流缓存被清理 key={} reason={}",
                        notification.getKey(), notification.getCause());
            })
            .build();


    /**
     * 鉴权限流
     *
     * @param commandEntity 鉴权限流命令
     * @return true: 限流; false: 不限流
     */
    @Override
    public boolean rateLimit(RateLimitCommandEntity commandEntity) {
        String gatewayId = commandEntity.getGatewayId();
        String apiKey = commandEntity.getApiKey();
        // 参数校验
        if (StringUtils.isAnyBlank(gatewayId, apiKey)) {
            log.info("网关Id: {} apiKey: {} 参数错误", gatewayId, apiKey);
            return false;
        }
        String key = gatewayId + "_" + apiKey;
        try {
            TokenBucketRateLimiter rateLimiter = rateLimiterCache.get(key, () -> {
                McpGatewayAuthVO gatewayAuthVO = repository.queryEffectiveGatewayAuthInfo(new LicenseCommandEntity(gatewayId, apiKey));
                if (null == gatewayAuthVO) {
                    // 无效的apikey
                    throw new AppException(ResponseCode.GATEWAY_APIKEY_ILLEGAL);
                }
                if (null == gatewayAuthVO.getRateLimit()) {
                    throw new AppException(ResponseCode.LIMIT_NOT_CONFIG);
                }
                // 速率限制（次/小时）转换为（次/秒）
                double permitsPerSecond = (double) gatewayAuthVO.getRateLimit() / 3600;
                if (permitsPerSecond <= 0) {
                    throw new AppException(ResponseCode.LIMIT_VALUE_ERROR);
                }
                log.info("网关Id: {} apiKey: {} 配置限流: {} 次/秒", gatewayId, apiKey, permitsPerSecond);
                return new TokenBucketRateLimiter(permitsPerSecond);
            });
            // 尝试获取令牌 true: 限流; false: 不限流
            return !rateLimiter.tryAcquire();
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause instanceof AppException) {
                if (((AppException) cause).getCode().equals(ResponseCode.GATEWAY_APIKEY_ILLEGAL.getCode())) {
                    // 如果是无效的apikey，返回 true (限流/禁止)
                    log.info("网关Id: {} apiKey: {} 无效的apikey --> 限流/禁止", gatewayId, apiKey);
                    return true;
                } else if (((AppException) cause).getCode().equals(ResponseCode.LIMIT_NOT_CONFIG.getCode())) {
                    // 如果是无限流配置，按原逻辑返回 false (不限流)
                    log.info("网关Id: {} apiKey: {} 无限流配置 --> 不限流", gatewayId, apiKey);
                    return false;
                } else if (((AppException) cause).getCode().equals(ResponseCode.LIMIT_VALUE_ERROR.getCode())) {
                    // 如果是配置为 0/负数，按原逻辑返回 true (限流/禁止)
                    log.info("网关Id: {} apiKey: {} 配置限流: 0/负数 --> 限流/禁止", gatewayId, apiKey);
                    return true;
                }
            }

            // 其他异常（如数据库错误），记录日志并放行
            log.error("限流校验失败 gatewayId:{} apiKey:{}", gatewayId, apiKey, e);
            return false;
        }
    }
}
