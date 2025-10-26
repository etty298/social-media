package ru.home.user.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.home.user.api.filter.TokenValidationFilter;

@Configuration
@RequiredArgsConstructor
public class FilterConfig {

    private final TokenValidationFilter tokenValidationFilter;

    @Bean
    public FilterRegistrationBean<TokenValidationFilter> tokenValidationFilterRegistration() {
        FilterRegistrationBean<TokenValidationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(tokenValidationFilter);
        registration.addUrlPatterns("/api/*");
        registration.setName("tokenValidationFilter");
        registration.setOrder(1);
        return registration;
    }
}
