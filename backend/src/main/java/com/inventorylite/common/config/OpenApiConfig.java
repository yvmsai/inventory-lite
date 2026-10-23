package com.inventorylite.common.config;

import java.math.BigDecimal;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class OpenApiConfig {

    private static final String BEARER = "bearerAuth";

    static {
        SpringDocUtils.getConfig().replaceWithSchema(BigDecimal.class, new StringSchema().example("19.99"));
    }

    @Bean
    OpenAPI inventoryLiteOpenApi() {
        return new OpenAPI()
                .info(new Info().title("Inventory Lite API").version("v1"))
                .components(new Components().addSecuritySchemes(BEARER,
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }
}
