package com.hackathon.simulacao.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openApiConfig() {
        return new OpenAPI().info(apiInfo());
    }

    private Info apiInfo() {
        Info info = new Info();
        info
                .title("Simulação Caixa API")
                .description("Api de simulação para teste na caixa")
                .version("V1.0.0");
        return info;
    }
}