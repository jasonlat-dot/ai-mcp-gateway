package com.jasonlat.ai.domain.protocol.model.entity;

import com.jasonlat.ai.domain.protocol.model.valobj.dubbo.DubboProtocolVO;
import com.jasonlat.ai.domain.protocol.model.valobj.http.HTTPProtocolVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 存储协议实体
 * <p>
 * 同时支持 HTTP 与 Dubbo 两类协议,字段独立、不互相影响。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageCommandEntity {

    /**
     * HTTP 协议列表(原结构,保留)
     */
    private List<HTTPProtocolVO> httpProtocolVOS;

    /**
     * Dubbo 协议列表
     */
    private List<DubboProtocolVO> dubboProtocolVOS;

}