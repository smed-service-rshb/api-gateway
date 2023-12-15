package ru.softlab.efr.infrastructure.apigateway;

/**
 * Исключение, сигнализирующее о недоступности сервиса
 */
public class ServiceUnavailableException extends RuntimeException {
    private final String serviceName;

    /**
     * Конструктор
     *
     * @param serviceName имя недоступного сервиса
     * @param cause       исключение причина
     */
    public ServiceUnavailableException(String serviceName, Exception cause) {
        super(cause);
        this.serviceName = serviceName;
    }

    /**
     * @return имя недоступного сервиса
     */
    public String getServiceName() {
        return serviceName;
    }
}
