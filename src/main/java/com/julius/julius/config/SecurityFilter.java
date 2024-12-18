package com.julius.julius.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import com.julius.julius.filter.JWTGeneratorFilter;
import com.julius.julius.filter.JWTValidationFilter;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;

@Configuration
@EnableWebSecurity
public class SecurityFilter {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName("_csrf");
        
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        .csrf(csrf -> csrf.csrfTokenRequestHandler(requestHandler).ignoringRequestMatchers("/produto/**","/loja/**", "/categoria/**","/banners/**", "/generate-image","/report/**","/post/**", "/promos/**", "/mensagem/**", "/eventos/**")
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                // .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
                .addFilterAfter(new JWTGeneratorFilter(), BasicAuthenticationFilter.class)
                .addFilterBefore(new JWTValidationFilter(), BasicAuthenticationFilter.class);

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET,"/produto/**").permitAll()
                .requestMatchers("/produto/**").authenticated()
                .requestMatchers(HttpMethod.GET,"/categoria/**").permitAll()
                .requestMatchers("/categoria/**").authenticated()
                .requestMatchers(HttpMethod.GET ,"/loja/**").permitAll()
                .requestMatchers("/loja/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/report/**").permitAll()
                .requestMatchers("/report/**").authenticated()
                .requestMatchers("/user").authenticated()
                .requestMatchers(HttpMethod.GET,"/banners/**").permitAll()
                .requestMatchers("/banners/**").authenticated()
                .requestMatchers("/generate-image").permitAll()
                .requestMatchers( HttpMethod.GET,"/scraper/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/post/**").permitAll()
                .requestMatchers("/post/**").authenticated()
                .requestMatchers(HttpMethod.GET,"/promos/**").permitAll()
                .requestMatchers("/promos/**").permitAll()
                .requestMatchers("/mensagem/**").permitAll()
                        .requestMatchers("/eventos/**").permitAll())
                .formLogin(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults());

        return http.build();

    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

}