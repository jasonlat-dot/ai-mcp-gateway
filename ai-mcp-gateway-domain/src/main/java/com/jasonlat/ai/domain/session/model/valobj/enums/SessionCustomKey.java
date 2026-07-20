package com.jasonlat.ai.domain.session.model.valobj.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SessionCustomKey {

    MCP_SESSION_HEADER_KEY("Mcp-Session-Id"),
    MCP_TRANSPORT_CONTEXT_KEY("MCP_TRANSPORT_CONTEXT"),
    ;

    private final String code;
}
