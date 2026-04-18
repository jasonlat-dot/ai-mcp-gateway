package com.jasonlat.ai.trigger.api.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author jasonlat
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> implements Serializable {

    private String code;
    private String info;
    private T data;

    public static <T> Response<T> build(String code, String msg, T data) {
        return new Response<T>(code, msg, data);
    }

}
