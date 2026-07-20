package com.jasonlat.ai.domain.llm.model.valobj;

import lombok.Getter;

@Getter
public enum McpTypeEnumVO {
    /** SSE 方式（默认）*/
    SSE("sse"),
    /** Streamable 方式 */
    STREAMABLE("streamable");

    private final String code;

    McpTypeEnumVO(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return code;
    }
}