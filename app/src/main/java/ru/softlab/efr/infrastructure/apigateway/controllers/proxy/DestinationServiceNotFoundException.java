package ru.softlab.efr.infrastructure.apigateway.controllers.proxy;

/**
 * @author krenev
 * @since 13.04.2017
 */

final class DestinationServiceNotFoundException extends RuntimeException {

    private final String method;
    private final String url;

    DestinationServiceNotFoundException(String method, String url) {

        this.method = method;
        this.url = url;
    }

    String getMethod() {
        return method;
    }

    String getUrl() {
        return url;
    }
}