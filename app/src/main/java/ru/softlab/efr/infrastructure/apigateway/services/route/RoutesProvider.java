package ru.softlab.efr.infrastructure.apigateway.services.route;

import java.util.Collection;

/**
 * Поставщик правил маршрутизации запросов
 */
public interface RoutesProvider {
    Collection<Route> getRoutes();
}
