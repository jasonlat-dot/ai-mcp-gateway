package com.jasonlat.ai.test.domain.session.message;

import com.alibaba.fastjson.JSON;
import com.jasonlat.ai.domain.session.model.valobj.McpSchemaVO;
import com.jasonlat.ai.domain.session.service.message.handle.IRequestHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class RequestHandlerTest {

    @Resource
    private IRequestHandler toolsListHandler;

    @Test
    public void test_handle() {
        McpSchemaVO.JsonRpcResponse handle = toolsListHandler.
                handleMessage("gateway_001",
                        new McpSchemaVO.JsonRpcRequest("2.0","tool/list","a355a5f7-0",""));
        log.info("测试结果:{}", JSON.toJSONString(handle.result()));
    }

}
