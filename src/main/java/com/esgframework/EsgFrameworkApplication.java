package com.esgframework;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class EsgFrameworkApplication {
    private static final Logger logger = LoggerFactory.getLogger(EsgFrameworkApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(EsgFrameworkApplication.class, args);
        logger.info("ESG Framework Application Started");
    }

    // Password encoder is defined in SecurityConfig
}
