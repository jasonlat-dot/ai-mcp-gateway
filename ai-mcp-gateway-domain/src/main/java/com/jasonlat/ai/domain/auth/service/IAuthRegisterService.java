package com.jasonlat.ai.domain.auth.service;


import com.jasonlat.ai.domain.auth.model.entity.RegisterCommandEntity;

public interface IAuthRegisterService {

    /**
     * 注册
     * @param command 注册命令入参
     * @return 注册结果 apiKey
     */
    String register(RegisterCommandEntity command);
}
