package com.jasonlat.ai.types.enums;

import com.jasonlat.ai.types.exception.AppException;
import lombok.Getter;

/**
 * 网关枚举；共用枚举抽取。
 */
public enum GatewayEnum {

    ;

    @Getter
    public enum  GatewayAuthStatusEnum {

        NOT_VERIFIED(0, "不校验"),

        STRONG_VERIFIED(1, "强校验"),
        ;

        private final Integer code;
        private final String info;

        GatewayAuthStatusEnum(Integer code, String info) {
            this.code = code;
            this.info = info;
        }

        public static GatewayAuthStatusEnum get(Integer code) {
            if (code == null) return null;
            for (GatewayAuthStatusEnum val : GatewayAuthStatusEnum.values()) {
                if (val.code.equals(code)) {
                    return val;
                }
            }
            throw new AppException(ResponseCode.ENUM_NOT_FOUND.getCode(), ResponseCode.ENUM_NOT_FOUND.getInfo());
        }
    }

    @Getter
    public enum  GatewayStatus{

        ENABLE(1,"启用"),
        DISABLE(0,"禁用")

        ;

        private final Integer code;
        private final String info;

        GatewayStatus(Integer code, String info) {
            this.code = code;
            this.info = info;
        }

        public static GatewayStatus getByCode(Integer code){
            if(null == code){
                return null;
            }
            for (GatewayStatus anEnum : GatewayStatus.values()) {
                if(anEnum.getCode().equals(code)){
                    return anEnum;
                }
            }

            throw new AppException(ResponseCode.ENUM_NOT_FOUND.getCode(), ResponseCode.ENUM_NOT_FOUND.getInfo());
        }

    }

}
