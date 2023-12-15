package ru.softlab.efr.infrastructure.apigateway.services.route;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Матчер маршрутов
 */
public class RouteMatcher {

    private RoutesProvider routesProvider;

    RouteMatcher(RoutesProvider routesProvider) {
        Assert.notNull(routesProvider, "routesProvider должен быть задан");

        this.routesProvider = routesProvider;
    }

    /**
     * Найти маршрут, соответсвующий пути
     *
     * @param path путь
     * @return найденный маршрут или null, если маршрут не найден для пути
     * @throws AmbiguousRoutesException в случае невозможности однозначно определить маршрут
     */
    public Route findRoute(String path) throws AmbiguousRoutesException {
        Assert.notNull(path, "Path должен быть задан");
        Collection<Route> routes = routesProvider.getRoutes();
        if (routes == null) {
            return null;
        }

        AntPathMatcher apm = new AntPathMatcher();
        List<Route> matches = new ArrayList<>();
        for (Route route : routes) {
            if (apm.match(route.getPattern(), path)) {
                matches.add(route);
            }
        }
        if (matches.isEmpty()) {
            return null;
        }

        if (matches.size() == 1) {
            return matches.get(0);

        }

        Comparator<Route> patternComparator = new RouteComparatorAdapter(apm.getPatternComparator(path));
        matches.sort(patternComparator);
        Route bestMatch = matches.get(0);
        Route secondBestMatch = matches.get(1);
        if (patternComparator.compare(bestMatch, secondBestMatch) == 0) {
            throw new AmbiguousRoutesException(path, bestMatch, secondBestMatch);
        }
        return bestMatch;

    }

    private static final class RouteComparatorAdapter implements Comparator<Route> {
        private Comparator<String> patternComparator;

        RouteComparatorAdapter(Comparator<String> patternComparator) {
            this.patternComparator = patternComparator;
        }

        @Override
        public int compare(Route route1, Route route2) {
            return patternComparator.compare(route1.getPattern(), route2.getPattern());
        }
    }
}
