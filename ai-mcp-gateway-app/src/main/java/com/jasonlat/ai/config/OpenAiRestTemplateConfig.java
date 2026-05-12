package com.jasonlat.ai.config;

import com.jasonlat.ai.config.properties.OpenAiClientProperties;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

/**
 * OpenAI 专用 RestTemplate 配置类。
 *
 * 这里使用 Apache HttpClient 5 作为底层 HTTP 客户端，
 * 主要是为了支持连接池、代理、代理认证、超时控制和连接回收等生产级能力。
 */
@Configuration
@EnableConfigurationProperties(OpenAiClientProperties.class)
public class OpenAiRestTemplateConfig {

    /**
     * 构建 OpenAI 专用 RestTemplate。
     *
     * @param properties 从 application.yml 读取的 OpenAI 客户端配置
     * @return 配置好代理、连接池和超时时间的 RestTemplate
     */
    @Bean("openAiRestTemplate")
    public RestTemplate openAiRestTemplate(OpenAiClientProperties properties) {
        OpenAiClientProperties.Http httpProperties = properties.http();

        // 创建连接池管理器，用于复用 HTTP 连接，避免每次请求都重新创建连接
        PoolingHttpClientConnectionManager connectionManager =
                new PoolingHttpClientConnectionManager();

        // 设置连接池最大连接数
        connectionManager.setMaxTotal(httpProperties.maxTotal());

        // 设置单个目标主机的最大连接数
        connectionManager.setDefaultMaxPerRoute(httpProperties.maxPerRoute());

        // 设置请求级别的超时参数
        RequestConfig requestConfig = RequestConfig.custom()
                // 建立连接超时时间
                .setConnectTimeout(Timeout.ofMilliseconds(httpProperties.connectTimeoutMs()))
                // 等待接口响应的超时时间，LLM 接口通常建议设置得长一些
                .setResponseTimeout(Timeout.ofMilliseconds(httpProperties.responseTimeoutMs()))
                // 从连接池获取连接的等待超时时间
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(httpProperties.connectionRequestTimeoutMs()))
                .build();

        // 创建 Apache HttpClient 构建器
        HttpClientBuilder httpClientBuilder = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                // 自动回收空闲连接，避免长时间不用的连接占用资源
                .evictIdleConnections(TimeValue.ofSeconds(httpProperties.evictIdleSeconds()));

        // 根据配置决定是否回收已过期连接
        if (httpProperties.evictExpiredConnections()) {
            httpClientBuilder.evictExpiredConnections();
        }

        // 根据 yml 配置设置网络代理
        configureProxy(httpClientBuilder, httpProperties.proxy());

        // 构建最终的 HttpClient
        HttpClient httpClient = httpClientBuilder.build();

        // 将 Apache HttpClient 适配给 Spring 的 ClientHttpRequestFactory
        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        // 使用自定义 requestFactory 创建 RestTemplate
        return new RestTemplate(requestFactory);
    }

    /**
     * 配置 HTTP 代理。
     *
     * 如果 proxy.enabled=false，则不设置代理，RestTemplate 会直接访问目标地址。
     * 如果 proxy.enabled=true，则会通过指定的 host 和 port 访问网络。
     * 如果同时配置了 username/password，则会启用代理认证。
     */
    private void configureProxy(
            HttpClientBuilder httpClientBuilder,
            OpenAiClientProperties.Proxy proxyProperties
    ) {
        // 未配置代理或代理开关关闭时，直接返回
        if (proxyProperties == null || !proxyProperties.enabled()) {
            return;
        }

        // 设置代理服务器地址和端口
        HttpHost proxy = new HttpHost(proxyProperties.host(), proxyProperties.port());
        httpClientBuilder.setProxy(proxy);

        // 如果代理服务器需要用户名和密码，则配置代理认证
        if (StringUtils.hasText(proxyProperties.username())
                && StringUtils.hasText(proxyProperties.password())) {
            BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();

            credentialsProvider.setCredentials(
                    new AuthScope(proxyProperties.host(), proxyProperties.port()),
                    new UsernamePasswordCredentials(
                            proxyProperties.username(),
                            proxyProperties.password().toCharArray()
                    )
            );

            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        }
    }
}
