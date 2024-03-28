package com.julius.julius.config;

import java.util.Arrays;
import java.util.Collections;

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

import com.julius.julius.filter.CsrfCookieFilter;
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

        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).cors(cors -> {
            cors.configurationSource(new CorsConfigurationSource() {

                @Override
                @Nullable
                public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                    CorsConfiguration config = new CorsConfiguration();

                    config.setAllowedOrigins(Collections.singletonList("*"));
                    config.setAllowedMethods(Collections.singletonList("*"));
                    config.setAllowCredentials(true);
                    config.setAllowedHeaders(Collections.singletonList("*"));
                    config.setExposedHeaders(Arrays.asList("Authorization"));
                    config.setMaxAge(3600L);

                    return config;
                }

            });
        });

        http.csrf(csrf -> csrf.csrfTokenRequestHandler(requestHandler).ignoringRequestMatchers("/produto/**","/loja/**")
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                // .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
                .addFilterAfter(new JWTGeneratorFilter(), BasicAuthenticationFilter.class)
                .addFilterBefore(new JWTValidationFilter(), BasicAuthenticationFilter.class);

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET,"/produto/**").permitAll()
                .requestMatchers("/produto/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/categoria/**").permitAll()
                .requestMatchers("/categoria/**").authenticated()
                .requestMatchers( "/loja/**").permitAll()
                // .requestMatchers("/loja").authenticated()
                .requestMatchers(HttpMethod.GET, "/report").permitAll()
                .requestMatchers("report/**").authenticated()
                .requestMatchers("/user").authenticated()
                .requestMatchers("/banners/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/scraper").permitAll())
                .formLogin(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults());

        return http.build();

    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

}