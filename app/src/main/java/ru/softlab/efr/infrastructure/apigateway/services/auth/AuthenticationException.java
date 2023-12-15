package ru.softlab.efr.infrastructure.apigateway.services.auth;

/**
 * Базовое исключение ошибок аутентификации
 */
public abstract class AuthenticationException extends Exception {
    AuthenticationException(String description) {
        super(description);
    }

    AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @return код ошибки, определяемый наследником
     */
    public abstract String getErrorCode();
}
