package com.inventorylite.common.config;

import java.math.BigDecimal;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.ToStringSerializer;

@Configuration
class JacksonConfig {

    /** Money is serialized as a JSON string ("19.99") so clients never round through floats. */
    @Bean
    JacksonModule bigDecimalAsStringModule() {
        return new SimpleModule("BigDecimalAsString").addSerializer(BigDecimal.class, ToStringSerializer.instance);
    }
}
