package org.financetracker.financetracker_api;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer{

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // which endpoints to cover
                .allowedOrigins("http://localhost:5173") // who is allowed in
                .allowedMethods("GET", "POST", "PUT", "DELETE");
    }

}
