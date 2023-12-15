package ru.softlab.efr.infrastructure.apigateway.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ru.softlab.efr.infrastructure.apigateway.ApiGatewayApplicationConfiguration;
import ru.softlab.efr.test.infrastructure.transport.rest.config.AbstractTestConfiguration;


@Configuration
@ComponentScan
public class TestApplicationConfiguration extends AbstractTestConfiguration {

    @Override
    protected String getTestAppName() {
        return ApiGatewayApplicationConfiguration.APP_NAME;
    }
}
