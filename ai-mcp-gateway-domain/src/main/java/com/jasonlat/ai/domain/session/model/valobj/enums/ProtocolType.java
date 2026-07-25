package com.jasonlat.ai.domain.session.model.valobj.enums;

import com.jasonlat.ai.types.exception.AppException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProtocolType {

    DUBBO("dubbo"),
    HTTP("http"),

    ;
    private final String value;

    public static ProtocolType get(String value) {
        for (ProtocolType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new AppException("Invalid ProtocolType value: " + value);
    }
}
