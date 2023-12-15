package ru.softlab.efr.infrastructure.apigateway;

import org.springframework.web.WebApplicationInitializer;
import ru.softlab.efr.infrastructure.transport.server.init.AbstractTransportInitializer;

/**
 * Инициазатор приложения
 *
 * @see AbstractTransportInitializer
 */
public class ApiGatewayInitializer extends AbstractTransportInitializer implements WebApplicationInitializer {
    protected Class<?> getConfiguration() {
        return ApiGatewayApplicationConfiguration.class;
    }
}

