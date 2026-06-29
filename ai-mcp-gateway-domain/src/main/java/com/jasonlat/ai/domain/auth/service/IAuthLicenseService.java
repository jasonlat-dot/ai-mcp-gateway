package com.jasonlat.ai.domain.auth.service;


import com.jasonlat.ai.domain.auth.model.entity.LicenseCommandEntity;

public interface IAuthLicenseService {

    /**
     * 验证授权
     * @param commandEntity 入参信息
     * @return true:授权成功 false:授权失败
     */
    boolean checkLicense(LicenseCommandEntity commandEntity);
}
