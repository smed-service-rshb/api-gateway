package ru.softlab.efr.infrastructure.apigateway.services.route;

/**
 * Исключение, сигнализирующее о необнозначности соответсвия пути маршрутам
 */
public class AmbiguousRoutesException extends RuntimeException {
    private final String path;
    private final Route[] routes;

    AmbiguousRoutesException(String path, Route... routes) {
        this.path = path;
        this.routes = routes;
    }

    /**
     * @return путь, который вызвал коллизии
     */

    public String getPath() {
        return path;
    }

    /**
     * @return перечень конфликтующих маршрутов для пути
     */
    public Route[] getRoutes() {
        return routes;
    }
}
