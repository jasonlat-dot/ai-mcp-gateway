package com.jasonlat.ai.domain.session.model.valobj.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SessionTransportTypeEnumVO {

    SSE("sse", "SSE 传输协议"),
    STREAMABLE("streamable", "Streamable HTTP 传输协议"),

    ;

    private final String code;
    private final String info;

}