package com.jasonlat.ai.domain.protocol.adapter.repository;


import com.jasonlat.ai.domain.protocol.model.valobj.dubbo.DubboProtocolVO;
import com.jasonlat.ai.domain.protocol.model.valobj.http.HTTPProtocolVO;

import java.util.List;

/**
 * 协议仓储服务接口
 */
public interface IProtocolRepository {

    List<Long> saveHttpProtocolAndMapping(List<HTTPProtocolVO> httpProtocolVOS);

    boolean updateHttpProtocolAndMapping(HTTPProtocolVO httpProtocolVO);

    void deleteGatewayProtocol(Long protocolId);

    /**
     * 保存 Dubbo 协议配置 + 字段映射。
     * <p>
     * 每个 DubboProtocolVO 生成一个新 protocolId(雪花算法),
     * DubboProtocolVO.mappings 展开为 mcp_protocol_mapping 多行。
     *
     * @param dubboProtocolVOS Dubbo VO 列表
     * @return 新生成的 protocolId 列表,与入参顺序对齐
     */
    List<Long> saveDubboProtocolAndMapping(List<DubboProtocolVO> dubboProtocolVOS);

    /**
     * 更新 Dubbo 协议配置 + 字段映射。
     * <p>
     * 行为与 {@link #updateHttpProtocolAndMapping(HTTPProtocolVO)} 对齐:
     * 先按 protocolId 更新 mcp_protocol_dubbo 行,
     * 再 delete + batchInsert 重置 mcp_protocol_mapping 行。
     * <p>
     * VO 中的 {@code mappings} 是要替换的"全量集合",传 null/空表示不写 mapping。
     */
    boolean updateDubboProtocolAndMapping(DubboProtocolVO dubboProtocolVO);
}