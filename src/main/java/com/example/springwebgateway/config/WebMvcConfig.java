package com.example.springwebgateway.config;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;


@Configuration
public class WebMvcConfig {

    @Bean
    public FilterRegistrationBean<RequestCachingFilter> requestCachingFilterRegistration(RequestCachingFilter requestCachingFilter) {
        FilterRegistrationBean<RequestCachingFilter> filterRegBean = new FilterRegistrationBean<>();
        filterRegBean.setFilter(requestCachingFilter);
        // 可以指定需要缓存请求的URL模式
        filterRegBean.addUrlPatterns("/*");
        // 设置过滤器优先级
        filterRegBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return filterRegBean;
    }


    @Bean
    public FilterRegistrationBean<SignVerifyFilter> signatureVerifyFilterRegistration(SignVerifyFilter signatureVerifyFilter) {
        FilterRegistrationBean<SignVerifyFilter> filterRegBean = new FilterRegistrationBean<>();
        filterRegBean.setFilter(signatureVerifyFilter);
        // 可以指定需要缓存请求的URL模式
        filterRegBean.addUrlPatterns("/*");
        // 设置过滤器优先级
        filterRegBean.setOrder(NumberUtils.INTEGER_ZERO);
        return filterRegBean;
    }
}
