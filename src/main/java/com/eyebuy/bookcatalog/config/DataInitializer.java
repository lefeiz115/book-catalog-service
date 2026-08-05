package com.eyebuy.bookcatalog.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile({"default", "dev"})
public class DataInitializer implements CommandLineRunner {

    @Override
    public void run(String... args) {
        log.info("Book Catalog Service started successfully!");
        log.info("API Documentation: http://localhost:8080/swagger-ui.html");
    }
}
