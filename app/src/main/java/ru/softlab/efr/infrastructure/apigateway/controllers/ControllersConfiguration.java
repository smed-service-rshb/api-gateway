package ru.softlab.efr.infrastructure.apigateway.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.WebContentInterceptor;
import ru.softlab.efr.infrastructure.apigateway.TimeoutConfiguration;

/**
 * Конфигурация spring mvc, задающая таймаут для асинхронных вызовов контролера.
 */
@Configuration
@EnableWebMvc
public class ControllersConfiguration extends WebMvcConfigurerAdapter {

    @Autowired
    private TimeoutConfiguration timeoutConfiguration;

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setDefaultTimeout(timeoutConfiguration.getTimeout());
        super.configureAsyncSupport(configurer);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        WebContentInterceptor webContentInterceptor = new WebContentInterceptor();
        webContentInterceptor.setCacheControl(CacheControl.noCache());
        registry.addInterceptor(webContentInterceptor);
    }
}