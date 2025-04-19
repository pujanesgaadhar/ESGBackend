package com.esgframework.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class JwtConfig {
    private String jwtSecret;
    private long jwtExpirationInMs;

    public String getJwtSecret() {
        return jwtSecret;
    }
    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }
    public long getJwtExpirationInMs() {
        return jwtExpirationInMs;
    }
    public void setJwtExpirationInMs(long jwtExpirationInMs) {
        this.jwtExpirationInMs = jwtExpirationInMs;
    }
}
