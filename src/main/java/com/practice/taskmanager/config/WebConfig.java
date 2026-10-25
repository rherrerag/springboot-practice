package com.practice.taskmanager.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS (Cross-Origin Resource Sharing): por defecto el navegador BLOQUEA requests
 * JS desde un origen (http://localhost:4200, Angular) hacia otro origen
 * (http://localhost:8080, esta API), aunque ambos corran en tu misma maquina.
 *
 * Esta configuracion le dice a Spring que agregue los headers
 * "Access-Control-Allow-*" necesarios para que el navegador permita esas requests
 * desde el dev server de Angular.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:4200")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE");
    }
}
