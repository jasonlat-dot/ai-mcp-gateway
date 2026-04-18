package com.jasonlat.ai.infrastructure.generator;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.TemplateType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AutoGenerator {

    // ====================== 这里改成 PG 数据库连接信息 ======================
    private static final String URL = "jdbc:postgresql://127.0.0.1:13308/order?useUnicode=true&characterEncoding=utf8&autoReconnect=true&serverTimezone=UTC&useSSL=false";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "lijiaqiang12@";

    private static final String outputDir =
            System.getProperty("user.dir") + "\\"  + "ai-mcp-gateway-infrastructure\\src\\main\\java";

    private static final String parentPacket = "com.jasonlat.ai.infrastructure";

    private static final String mapperXmlPath =
            System.getProperty("user.dir") + "\\" +  "ai-mcp-gateway-app\\src\\main\\resources\\mybatis\\mapper";

    public static void main(String[] args) {
        // 需要生成代码的表
        List<String> tables = new ArrayList<>();

        // 配置全局设置
        FastAutoGenerator.create(URL, USERNAME, PASSWORD)
                .globalConfig(builder -> {
                    builder
                            .author("jasonlat")
                            .outputDir(outputDir)
                            .commentDate("yyyy-MM-dd");
                })
                .packageConfig(builder -> {
                    builder.parent(parentPacket)
                            .mapper("dao")
                            .entity("dao.po")
                            .xml("mybatis/mapper").pathInfo(Collections.singletonMap(OutputFile.xml, mapperXmlPath));
                })
                .templateConfig(builder -> {
                    builder
                            .disable(TemplateType.CONTROLLER)
                            .disable(TemplateType.SERVICE)
                            .disable(TemplateType.SERVICE_IMPL);
                })
                // ====================== 策略配置完全不用改 ======================
                .strategyConfig(builder -> {
                    builder.addInclude(tables)
                            .entityBuilder()
                            .formatFileName("%s")
                            .enableLombok()
                            .enableFileOverride()
                            .enableTableFieldAnnotation()

                            .mapperBuilder()
                            .enableBaseResultMap()
                            .formatMapperFileName("I%sDao")
                            .enableFileOverride()
                            .formatXmlFileName("%sMapper");

                })
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }

}