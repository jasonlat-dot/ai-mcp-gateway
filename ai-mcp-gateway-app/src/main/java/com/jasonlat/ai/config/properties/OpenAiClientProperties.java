package com.jasonlat.ai.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OpenAI 客户端配置属性类。
 *
 * 该类用于读取 application.yml 中 openai.client 前缀下的配置，
 * 包括 OpenAI 兼容接口地址、API Key、模型名称、接口路径以及 HTTP 连接参数。
 */
@ConfigurationProperties(prefix = "openai.client")
public record OpenAiClientProperties(
        // OpenAI 兼容接口的基础地址，例如：https://api.jasonlat.com/
        String baseUrl,

        // API Key，建议从环境变量或配置中心读取，不要硬编码
        String apiKey,

        // 默认使用的模型名称，例如：gpt-5.4
        String model,

        // 聊天补全接口路径，例如：v1/chat/completions
        String completionsPath,

        // 向量嵌入接口路径，例如：v1/embeddings
        String embeddingsPath,

        // HTTP 客户端相关配置
        Http http
) {
    /**
     * HTTP 连接配置。
     */
    public record Http(
            // 建立 TCP 连接的超时时间，单位：毫秒
            int connectTimeoutMs,

            // 等待服务端响应的超时时间，单位：毫秒
            int responseTimeoutMs,

            // 从连接池获取连接的等待超时时间，单位：毫秒
            int connectionRequestTimeoutMs,

            // 连接池最大连接数
            int maxTotal,

            // 单个路由，也就是单个目标主机的最大连接数
            int maxPerRoute,

            // 空闲连接回收时间，单位：秒
            int evictIdleSeconds,

            // 是否自动回收已过期连接
            boolean evictExpiredConnections,

            // 网络代理配置
            Proxy proxy
    ) {}

    /**
     * HTTP 代理配置。
     */
    public record Proxy(
            // 是否启用代理，true 表示走代理，false 表示直连
            boolean enabled,

            // 代理服务器地址，例如：127.0.0.1
            String host,

            // 代理服务器端口，例如：7890
            int port,

            // 代理认证用户名，没有认证时可为空
            String username,

            // 代理认证密码，没有认证时可为空
            String password
    ) {}
}
