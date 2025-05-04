package com.esgframework;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.esgframework.services.MetricCategoryService;

@SpringBootApplication
public class EsgFrameworkApplication {
    private static final Logger logger = LoggerFactory.getLogger(EsgFrameworkApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(EsgFrameworkApplication.class, args);
        logger.info("ESG Framework Application Started");
    }

    @Bean
    public CommandLineRunner initializeData(@Autowired MetricCategoryService metricCategoryService) {
        return args -> {
            // Initialize default categories
            metricCategoryService.initializeDefaultCategories();
            System.out.println("Default metric categories initialized");
        };
    }

    // Password encoder is defined in SecurityConfig
}
