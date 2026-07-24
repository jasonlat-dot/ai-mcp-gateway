package com.jasonlat.ai.config;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MCP 网关作为 Dubbo Consumer 的运行时配置。
 * <p>
 * 不需要 @EnableDubbo(那是启服务暴露),只需要 ApplicationConfig + RegistryConfig
 * 供 DubboInvoker 动态构造 ReferenceConfig。
 */
@Configuration
public class DubboGatewayConfig {

    @Value("${dubbo.application.name:ai-mcp-gateway}")
    private String dubboAppName;

    @Value("${dubbo.registry.address:nacos://127.0.0.1:8848}")
    private String registryAddress;

    @Bean
    public ApplicationConfig applicationConfig() {
        ApplicationConfig cfg = new ApplicationConfig();
        cfg.setName(dubboAppName);
        return cfg;
    }

    @Bean
    public RegistryConfig registryConfig() {
        RegistryConfig cfg = new RegistryConfig();
        cfg.setAddress(registryAddress);
        // 同步 Consumer 注册信息(Nacos 治理用)
        cfg.setRegister(true);
        return cfg;
    }
}