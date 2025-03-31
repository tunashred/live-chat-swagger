package com.github.tunashred.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Kafka Swagger API")
                        .version("1.0")
                        .description("Demo API thesis")
                        .contact(new Contact()
                                .name("Enash")
                                .email("enash6969@example.com")));
    }
}
