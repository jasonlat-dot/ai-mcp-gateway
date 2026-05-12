package com.jasonlat.ai.config;

import com.jasonlat.ai.config.properties.OpenAiClientProperties;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

/**
 * Spring AI OpenAI 客户端配置类。
 *
 * 该类负责把自定义 RestTemplate 转换成 RestClient.Builder，
 * 然后交给 Spring AI 的 OpenAiApi 使用。
 */
@Configuration
public class OpenAiSpringAiConfig {

    /**
     * 基于自定义 RestTemplate 构建 RestClient.Builder。
     *
     * 这样 OpenAiApi 发起请求时，就会使用我们前面配置好的代理、
     * 连接池、超时和代理认证等 HTTP 参数。
     */
    @Bean
    public RestClient.Builder openAiRestClientBuilder(@Qualifier("openAiRestTemplate") RestTemplate openAiRestTemplate) {
        return RestClient.builder(openAiRestTemplate);
    }

    /**
     * 构建 Spring AI 的 OpenAiApi。
     *
     * baseUrl、apiKey、completionsPath、embeddingsPath 都从 yml 中读取。
     */
    @Bean
    public OpenAiApi openAiApi(RestClient.Builder openAiRestClientBuilder, OpenAiClientProperties properties) {
        return OpenAiApi.builder()
                .baseUrl(properties.baseUrl())
                .apiKey(properties.apiKey())
                .completionsPath(properties.completionsPath())
                .embeddingsPath(properties.embeddingsPath())
                .restClientBuilder(openAiRestClientBuilder)
                .build();
    }

    /**
     * 构建 Spring AI 的 ChatModel。
     *
     * 业务代码中可以直接注入 ChatModel 使用，
     * 不需要关心底层代理和 HTTP 客户端细节。
     */
    @Bean
    public ChatModel chatModel(OpenAiApi openAiApi, OpenAiClientProperties properties) {
        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(properties.model())
                        .build())
                .build();
    }

    @Bean
    public OpenAiChatModel.Builder chatModelBuilder(OpenAiApi openAiApi) {
        return OpenAiChatModel.builder()
                .openAiApi(openAiApi);
    }

}
