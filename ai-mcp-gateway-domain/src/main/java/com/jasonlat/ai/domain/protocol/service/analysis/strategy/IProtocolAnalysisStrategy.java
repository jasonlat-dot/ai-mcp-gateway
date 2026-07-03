package com.jasonlat.ai.domain.protocol.service.analysis.strategy;


import com.alibaba.fastjson2.JSONObject;
import com.jasonlat.ai.domain.protocol.model.valobj.http.HTTPProtocolVO;

import java.util.List;

public interface IProtocolAnalysisStrategy {

    /**
     * 执行解析
     * @param operation    操作
     * @param definitions  定义
     * @param mappings     映射
     */
    void doAnalysis(JSONObject operation, JSONObject definitions, List<HTTPProtocolVO.ProtocolMapping> mappings);
}
