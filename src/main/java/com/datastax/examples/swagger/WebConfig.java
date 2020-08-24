package com.datastax.examples.swagger;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Add a CORS Filter to allow cross-origin
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    /**
     * By overriding `addCorsMappings` from {@link WebMvcConfigurer}, 
     * we allow access with the full JavaScript Access.
     * 
     * {@inheritDoc}
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**");
    }
}
