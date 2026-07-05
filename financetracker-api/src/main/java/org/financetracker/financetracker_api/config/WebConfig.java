package org.financetracker.financetracker_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:5173",
                        "https://finance-tracker-frontend-six-rho.vercel.app"
                )
                // "*" means ANY method (GET, POST, PUT, DELETE,
                // PATCH, etc.) is allowed — not just a hand-picked
                // list. This stops us from ever hitting this same
                // bug again if we add a new HTTP method later.
                .allowedMethods("*")
                // "*" here means ANY header is allowed through —
                // this covers our Authorization header AND any
                // future headers we might add (like ones Plaid's
                // library might send).
                .allowedHeaders("*");
    }
}