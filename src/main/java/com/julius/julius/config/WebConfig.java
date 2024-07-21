package com.julius.julius.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// @EnableWebMvc
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
        .allowedOrigins("http://localhost:4200","http://localhost:4100", "https://sergipeofertas.com.br")
        .allowedMethods("*")
        .allowCredentials(true)
        .allowedHeaders("*")
        .exposedHeaders("Authorization")
        .maxAge(3600L);
    }
    
}