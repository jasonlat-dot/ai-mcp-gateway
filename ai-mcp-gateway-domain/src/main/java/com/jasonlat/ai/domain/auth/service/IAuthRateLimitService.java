package com.jasonlat.ai.domain.auth.service;


import com.jasonlat.ai.domain.auth.model.entity.RateLimitCommandEntity;

public interface IAuthRateLimitService {

    /**
     * 鉴权限流
     * @param commandEntity 鉴权限流命令
     * @return true: 限流 false: 不限流
     */
    boolean rateLimit(RateLimitCommandEntity commandEntity);
}
