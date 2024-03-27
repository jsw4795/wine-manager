package com.winemanager;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceProvider implements WebMvcConfigurer {
	
	@Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");

        registry
                .addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
        
        registry
		        .addResourceHandler("/images/wine/**")
		        //for Unix: file:/opt/files
		        //TODO: use path here
		        .addResourceLocations("file:/Users/jsw4795/springboot-workspace/wine-manager-images/wine-pic/");

        registry
		        .addResourceHandler("/images/**")
		        .addResourceLocations("classpath:/static/images/");

    }
}
