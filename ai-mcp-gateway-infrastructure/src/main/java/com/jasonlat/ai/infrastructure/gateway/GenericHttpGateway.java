package com.jasonlat.ai.infrastructure.gateway;


import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

public interface GenericHttpGateway {

    @POST
    Call<ResponseBody> post(
            @Url String url,
            @HeaderMap Map<String, Object> headers,
            @Body RequestBody body
    );

    @GET
    Call<ResponseBody> get(
            @Url String url,
            @HeaderMap Map<String, Object> headers,
            @QueryMap Map<String, Object> queryParams
    );
}
