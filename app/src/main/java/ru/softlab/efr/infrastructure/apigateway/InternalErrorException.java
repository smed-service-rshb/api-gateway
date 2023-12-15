package ru.softlab.efr.infrastructure.apigateway;

/**
 * Исключение, сигнализирующее о внутренней ошибке
 */
public class InternalErrorException extends RuntimeException {
    /**
     * Конструктор
     *
     * @param message сообщение об ошибке
     */
    public InternalErrorException(String message) {
        super(message);
    }

    /**
     * Конструктор
     *
     * @param cause исключение - причина ошибки
     */
    public InternalErrorException(Throwable cause) {
        super(cause);
    }
}
