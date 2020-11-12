package com.agenson.cinema.config;

import com.agenson.cinema.security.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityContext securityContext() {
        return new SecurityContext();
    }
}
