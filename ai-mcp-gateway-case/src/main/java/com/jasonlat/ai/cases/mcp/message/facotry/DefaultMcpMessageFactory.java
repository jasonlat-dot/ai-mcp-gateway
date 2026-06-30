package com.jasonlat.ai.cases.mcp.message.facotry;

import com.jasonlat.ai.cases.mcp.message.node.RootNode;
import com.jasonlat.ai.cases.mcp.session.factory.DefaultMcpSessionFactory;
import com.jasonlat.ai.domain.session.model.entity.HandleMessageCommandEntity;
import com.jasonlat.ai.domain.session.model.valobj.SessionConfigVO;
import com.jasonlat.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jasonlat
 * 2026-06-26  19:22
 */
@Service
public class DefaultMcpMessageFactory {

    @Resource(name = "mcpMessageRootNode")
    private RootNode rootNode;

    public StrategyHandler<HandleMessageCommandEntity, DefaultMcpMessageFactory.DynamicContext, ResponseEntity<Object>> strategyHandler() {
        return rootNode;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext {

        private SessionConfigVO sessionConfigVO;

        private Map<String, Object> dataObjects = new HashMap<>(8);

        public <T> void setValue(String key, T value) {
            dataObjects.put(key, value);
        }

        @SuppressWarnings("unchecked")
        public <T> T getValue(String key) {
            return (T) dataObjects.get(key);
        }
    }
}
