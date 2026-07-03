package com.jasonlat.ai.domain.protocol.model.entity;

import com.jasonlat.ai.domain.protocol.model.valobj.enums.AnalysisTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 解析命令
 * @author jasonlat
 * 2026-07-02  18:57
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalysisCommandEntity {

    private AnalysisTypeEnum analysisType;

    private String openApiJson;

    private List<String> endpoints;
}
