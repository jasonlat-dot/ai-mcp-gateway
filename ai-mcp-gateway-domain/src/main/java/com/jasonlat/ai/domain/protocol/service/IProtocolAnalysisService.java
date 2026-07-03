package com.jasonlat.ai.domain.protocol.service;

import com.jasonlat.ai.domain.protocol.model.entity.AnalysisCommandEntity;
import com.jasonlat.ai.domain.protocol.model.valobj.http.HTTPProtocolVO;

import java.util.List;

public interface IProtocolAnalysisService {

    /**
     * 解析HTTP协议
     * @param commandEntity 解析命令
     * @return 解析结果
     */
    List<HTTPProtocolVO> analysis(AnalysisCommandEntity commandEntity);
}
