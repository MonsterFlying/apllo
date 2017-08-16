package com.gofobao.framework.config;

import com.gofobao.framework.security.interceptor.UrlInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by admin on 2017/8/15.
 */

/*@EnableWebMvc
@Configuration*/
public class WebUrlInterceptor extends WebMvcConfigurerAdapter {
/*
    @Bean
    public HandlerInterceptor getWebInterceptor() {
        return new UrlInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        System.out.println("初始化值自定义拦截器");
        registry.addInterceptor(getWebInterceptor())
                .addPathPatterns("*//*").excludePathPatterns("*//*swagger-ui.html");
        super.addInterceptors(registry);
    }*/


}

