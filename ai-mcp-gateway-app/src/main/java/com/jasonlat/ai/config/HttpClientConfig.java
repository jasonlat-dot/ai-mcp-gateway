package com.jasonlat.ai.config;

import com.google.gson.Gson;
import com.jasonlat.ai.infrastructure.gateway.GenericHttpGateway;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Configuration
public class HttpClientConfig {

    // 连接池配置参数
    private static final int MAX_IDLE_CONNECTIONS = 10;
    private static final long KEEP_ALIVE_MINUTE = 5;

    /**
     * OkHttpClient 交由Spring单例管理，全局唯一，连接池复用
     */
    @Bean
    public OkHttpClient okHttpClient() {
        // 日志拦截器，调试BODY，生产改为HEADERS/NONE
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // 全局请求拦截器
        Interceptor headerInterceptor = new Interceptor() {
            @Override
            public @NotNull Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                Request.Builder builder = original.newBuilder();

                // 只有原请求没有Content-Type才填充，避免覆盖自定义Header
                if (original.header("Content-Type") == null) {
                    builder.header("Content-Type", "application/json");
                }

                // 核心修复：强制Accept只接收JSON，杜绝WebFlux自动协商SSE(text/event-stream)
                builder.header("Accept", "application/json;charset=UTF-8");

                Request newRequest = builder.build();
                return chain.proceed(newRequest);
            }
        };

        // 初始化连接池
        ConnectionPool connectionPool = new ConnectionPool(MAX_IDLE_CONNECTIONS, KEEP_ALIVE_MINUTE, TimeUnit.MINUTES);

        return new OkHttpClient.Builder()
                .connectionPool(connectionPool)
                .retryOnConnectionFailure(true)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .callTimeout(60, TimeUnit.SECONDS)
                // 拦截器顺序：自定义头在前，日志在后
                .addInterceptor(headerInterceptor)
                .addInterceptor(loggingInterceptor)
                .followRedirects(true)
                .build();
    }

    /**
     * Retrofit 单例Bean，依赖注入OkHttpClient
     * baseUrl填占位符，适配你@Url动态全路径调用场景
     */
    @Bean
    public Retrofit retrofit(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl("https://api.jasonlat.cc/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(new Gson()))
                .build();
    }

    /**
     * 自动注入网关接口
     */
    @Bean
    public GenericHttpGateway genericHttpGateway(Retrofit retrofit) {
        return retrofit.create(GenericHttpGateway.class);
    }

    /**
     * 可选工具方法：外部快速创建Api
     */
    public static <T> T createApi(Retrofit retrofit, Class<T> clazz) {
        return retrofit.create(clazz);
    }
}