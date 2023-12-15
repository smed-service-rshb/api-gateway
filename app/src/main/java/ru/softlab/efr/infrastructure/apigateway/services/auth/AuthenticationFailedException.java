package ru.softlab.efr.infrastructure.apigateway.services.auth;

/**
 * Исключение, сигнализирующее о неудачной попытке аутентификации(некорректная комбинация логин-пароль)
 *
 * @author krenev
 */
public class AuthenticationFailedException extends AuthenticationException {

    public static final String ERROR_CODE = "AUTHENTICATION_FAILED";
    public static final String ERROR_MESSAGE = "Некорректный логин или пароль.";

    /**
     * Конструктор по умолчанию
     */
    public AuthenticationFailedException() {
        super(ERROR_MESSAGE);
    }

    /**
     * Конструктор
     *
     * @param cause причина
     */
    AuthenticationFailedException(Throwable cause) {
        super(ERROR_MESSAGE, cause);
    }

    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }
}
