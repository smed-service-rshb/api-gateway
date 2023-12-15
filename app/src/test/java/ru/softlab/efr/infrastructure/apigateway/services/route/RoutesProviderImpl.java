package ru.softlab.efr.infrastructure.apigateway.services.route;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author krenev
 * @since 17.04.2017
 */
class RoutesProviderImpl implements RoutesProvider {
    private final List<Route> routes;

    RoutesProviderImpl(String[] patterns) {
        routes = new ArrayList<>();
        for (String pattern : patterns) {
            routes.add(new Route(pattern, "some-service", ResourceType.PRIVATE));
        }


    }

    @Override
    public Collection<Route> getRoutes() {
        return routes;
    }
}
