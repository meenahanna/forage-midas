package com.jpmc.midascore.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration

public class RestClientConfig {
    @Bean
    public RestTemplate restTemplate() {
        // Default converters include JSON (Jackson) from spring-boot-starter-web
        return new RestTemplate();
    }
}
