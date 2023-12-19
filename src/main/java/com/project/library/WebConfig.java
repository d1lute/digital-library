package com.project.library;

import java.io.File;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
    	String currentPath = new File(".").getAbsolutePath();
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:/"+currentPath+"/src/main/resources/static/images/");
        registry.addResourceHandler("/epubs/**")
        .addResourceLocations("file:/"+currentPath+"/src/main/resources/static/epubs/");
    }
}

