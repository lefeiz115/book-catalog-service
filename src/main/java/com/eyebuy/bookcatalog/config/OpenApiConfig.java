package com.eyebuy.bookcatalog.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bookCatalogOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Book Catalog Service API")
                        .description("Book Catalog Management Service for EyeBuyDirect Interview")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Zhu LeFei")
                                .email("leopold_zhu@hotmail.com")));
    }
}
