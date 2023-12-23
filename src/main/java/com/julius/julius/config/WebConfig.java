package com.julius.julius.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    private final String caminho = "C:\\Users\\marco\\OneDrive\\Documentos\\Julius da promo back end\\julius\\src\\main\\resources\\static\\lojas";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/loja/")
                .addResourceLocations("file:"+caminho);
        
    }
    
}
