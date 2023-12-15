package ru.softlab.efr.infrastructure.apigateway.controllers.proxy;

/**
 * @author krenev
 * @since 14.04.2017
 */
class PrivateResourceAccessException extends RuntimeException {
    private final String method;
    private final String url;

    PrivateResourceAccessException(String method, String url) {

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
