package com.gofobao.framework.config;

import com.gofobao.framework.security.JwtAuthenticationEntryPoint;
import com.gofobao.framework.security.JwtAuthenticationTokenFilter;
import com.gofobao.framework.security.vo.ApolloPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Created by Max on 17/5/16.
 */

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    @Autowired
    private UserDetailsService userService;

    @Autowired
    public void configureAuthentication(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder
                .userDetailsService(this.userService)
                .passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new ApolloPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationTokenFilter authenticationTokenFilterBean() throws Exception {
        return new JwtAuthenticationTokenFilter();
    }


    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                // we don't need CSRF because our token is invulnerable
                .csrf().disable()

                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()

                // don't create session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()

                .authorizeRequests()
                //.antMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // allow anonymous resource requests
                .antMatchers(
                        HttpMethod.GET,
                        "/",
                        "/*.html",
                        "/favicon.ico",
                        "/**/*.html",
                        "/**/*.css",
                        "/**/*.js",
                        "/**/*.png",
                        "/**/*.jpg"
                ).permitAll()

                .antMatchers(
                        "/borrow/**")

                .permitAll()
                .antMatchers(
                        "/pc/**")

                .permitAll()
                .antMatchers(
                        "/redPackage/**")

                .permitAll()
                .antMatchers(
                        "/virtual/**")

                .permitAll()
                .antMatchers(
                        "/award/**")

                .permitAll()
                .antMatchers(
                        "/index/**")

                .permitAll()
                .antMatchers(
                        "/payment/**")

                .permitAll()
                .antMatchers(
                        "/repayment/**")

                .permitAll()
                .antMatchers(
                        "/transfer/**")

                .permitAll()
                .antMatchers(
                        "/lend/**")
                .permitAll()
                .antMatchers(
                        "/notices/**")
                .permitAll()
                .antMatchers(
                        "/coupon/**")
                .permitAll()
                .antMatchers(
                        "/loan/**")
                .permitAll()
                .antMatchers(
                        "/invite/**")
                .permitAll()
                .antMatchers(
                        "/pub/**")
                .permitAll()
                .antMatchers( //放行swagger-ui
                        "/swagger-resources/**", "/v2/**","/webjars/springfox-swagger-ui/**"
                )
                .permitAll()
                .antMatchers(
                        "/tender/**")
                .permitAll()
                .anyRequest().authenticated();

        // Custom JWT based security filter
        httpSecurity.addFilterBefore(authenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter.class);

        // disable page caching
        httpSecurity.headers().cacheControl();
    }
}
