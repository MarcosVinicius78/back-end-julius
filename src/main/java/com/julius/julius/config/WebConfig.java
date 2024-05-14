package com.julius.julius.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// @EnableWebMvc
@Configuration
public class WebConfig{

    // @Override
    // public void addCorsMappings(CorsRegistry registry) {
    //     registry.addMapping("/**")
    //     .allowedOrigins("http://localhost:4200")
    //     .allowedMethods("*")
    //     .allowCredentials(true)
    //     .allowedHeaders("*")
    //     .exposedHeaders("Authorization")
    //     .maxAge(3600L);
    // }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
}
