package com.example.springwebgateway.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;


@Configuration
public class WebMvcConfig {

    @Bean
    public FilterRegistrationBean<RequestCachingFilter> requestCachingFilter() {
        FilterRegistrationBean<RequestCachingFilter> filterRegBean = new FilterRegistrationBean<>();
        filterRegBean.setFilter(new RequestCachingFilter());
        // 可以指定需要缓存请求的URL模式
        filterRegBean.addUrlPatterns("/*");
        // 设置过滤器优先级
        filterRegBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return filterRegBean;
    }
}
