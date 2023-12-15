package ru.softlab.efr.infrastructure.apigateway;

/**
 * Класс-настройка  таймаутов
 */

public class TimeoutConfiguration {
    private int timeout;

    TimeoutConfiguration(int timeout) {

        this.timeout = timeout;
    }

    /**
     * @return таймаут в мс
     */
    public int getTimeout() {
        return timeout;
    }
}
