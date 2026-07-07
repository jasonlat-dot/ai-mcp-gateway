package com.jasonlat.ai.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;  
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;  
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;  
import org.springframework.context.annotation.Bean;  
import org.springframework.context.annotation.Configuration;  
import org.springframework.context.annotation.Primary;  
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;  
  
@Configuration  
public class JacksonConfig {  
  
    @Bean  
    @Primary  
    @ConditionalOnMissingBean(ObjectMapper.class)  
    public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {  
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();  
        SimpleModule simpleModule = new SimpleModule();  
        // 将Long类型序列化为String类型  
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);  
        objectMapper.registerModule(simpleModule);  
        return objectMapper;  
    }  
}
