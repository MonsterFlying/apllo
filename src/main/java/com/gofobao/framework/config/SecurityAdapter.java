package com.gofobao.framework.config;

import com.gofobao.framework.security.interceptor.Jwtintercepter;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class SecurityAdapter extends WebMvcConfigurerAdapter {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new Jwtintercepter())
                .addPathPatterns("/**")
                .excludePathPatterns("/**/pub/**")
                .excludePathPatterns("/swagger-resources/**", "/v2/**")
                .excludePathPatterns("/*.html", "/favicon.ico", "/**/*.html", "/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg")
                .excludePathPatterns("/index/**")
                .excludePathPatterns("/test/**");
        super.addInterceptors(registry);
    }
}

