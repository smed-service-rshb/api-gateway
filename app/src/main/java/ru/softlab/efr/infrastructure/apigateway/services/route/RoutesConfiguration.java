package ru.softlab.efr.infrastructure.apigateway.services.route;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import ru.softlab.efr.infrastructure.apigateway.services.route.xml.XmlFileRoutesProvider;

import java.io.IOException;

/**
 * Конфигурация маршрутизазатора запросов
 */
@Configuration
public class RoutesConfiguration {

    /**
     * @return Поставщик правил маршрутизации запросов
     * @throws IOException при ошибках чтения файла
     */
    @Bean
    protected RoutesProvider routesProvider() throws IOException {
        ClassPathResource resource = new ClassPathResource("/api-gateway-routes.xml");
        return new XmlFileRoutesProvider(resource);
    }

    /**
     * @return Матчер маршрутов
     * @throws IOException при ошибках
     */
    @Bean
    protected RouteMatcher routeMatcher() throws IOException {
        return new RouteMatcher(routesProvider());
    }
}
