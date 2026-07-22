package com.jasonlat.ai.domain.session.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * SSE 跨机器转发的统一响应 VO
 * <p>
 * 适配不同 HTTP 客户端（OkHttp、HttpClient、RestTemplate）的返回结构，
 * 让 case 层拿到一个不依赖具体实现的响应对象。
 *
 * @author jasonlat
 * 2026-07-22  20:35
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SessionForwardResponseVO {

    /**
     * HTTP 状态码
     */
    private int statusCode;

    /**
     * 是否成功（2xx）
     */
    private boolean success;

    /**
     * 响应体（String）
     */
    private String body;

    /**
     * 错误信息（失败时填充）
     */
    private String errorMessage;

    /**
     * 构造成功响应
     */
    public static SessionForwardResponseVO ok(int statusCode, String body) {
        return SessionForwardResponseVO.builder()
                .statusCode(statusCode)
                .success(true)
                .body(body)
                .build();
    }

    /**
     * 构造失败响应
     */
    public static SessionForwardResponseVO fail(int statusCode, String errorMessage) {
        return SessionForwardResponseVO.builder()
                .statusCode(statusCode)
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}