package com.gofobao.framework.config;

import com.gofobao.framework.security.filter.OtherOpenApiFilter;
import com.gofobao.framework.security.interceptor.Jwtintercepter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class SecurityAdapter extends WebMvcConfigurerAdapter {

    @Value("${starfire.ips}")
    private String starFireIps;

    @Value("${windmill.ips}")
    private String windmillIps;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new Jwtintercepter())
                .addPathPatterns("/**")
                .excludePathPatterns("/pub/**")
                .excludePathPatterns("/**/pub/**")
                .excludePathPatterns("/swagger-resources/**", "/v2/**")
                .excludePathPatterns("/*.html", "/favicon.ico", "/**/*.html", "/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg")
                .excludePathPatterns("/index/**")
                .excludePathPatterns("/version/**") ;

        super.addInterceptors(registry);
    }

    @Bean
    public FilterRegistrationBean otherOpenApiFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.addUrlPatterns("/*");
        registration.addInitParameter("starFireIps",starFireIps);
        registration.addInitParameter("windmillIps",windmillIps);
        registration.setFilter(new OtherOpenApiFilter());
        return registration;
    }

}

