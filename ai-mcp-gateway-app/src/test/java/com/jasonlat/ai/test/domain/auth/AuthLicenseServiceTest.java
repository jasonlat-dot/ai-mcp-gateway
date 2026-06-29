package com.jasonlat.ai.test.domain.auth;

import com.jasonlat.ai.domain.auth.model.entity.LicenseCommandEntity;
import com.jasonlat.ai.domain.auth.service.IAuthLicenseService;
import com.jasonlat.ai.domain.auth.service.IAuthRegisterService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

import static org.junit.Assert.assertTrue;


@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class AuthLicenseServiceTest {

    @Resource
    private IAuthLicenseService authLicenseService;

    @Resource
    private IAuthRegisterService authRegisterService;

    @Test
    public void test_checkLicenseLicense() {
        // 1. 注册 可以走新注册 apiKey，或者找到数据表中现存的数据测试。
       /* RegisterCommandEntity registerCommandEntity = new RegisterCommandEntity();
        registerCommandEntity.setGatewayId("gateway_001");
        registerCommandEntity.setRateLimit(10);
        registerCommandEntity.setExpireTime(new Date(System.currentTimeMillis() + 1000L * 60 * 60)); // 1小时后过期
        String apiKey = authRegisterService.register(registerCommandEntity);
        log.info("注册结果 apiKey: {}", apiKey);*/

        // 2. 鉴权
        LicenseCommandEntity commandEntity = new LicenseCommandEntity();
        commandEntity.setGatewayId("gateway_001");
//        commandEntity.setApiKey(apiKey);
        commandEntity.setApiKey("gw-SjnhpA5S6CI5bx9Vxvh8ATyezZzqclNNP2ikf18wSHHJMR1H");

        boolean success = authLicenseService.checkLicense(commandEntity);
        log.info("鉴权结果 success: {}", success);
        assertTrue(success);
    }

}
