package ru.softlab.efr.infrastructure.apigateway.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.softlab.efr.infrastructure.apigateway.ServiceUnavailableException;
import ru.softlab.efr.services.auth.exceptions.DataValidationException;


/**
 * Обработчик глобальных исключений
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler
    ResponseEntity handleException(final Throwable exception) {
        logger.error("Ошибка обработки запроса", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @ExceptionHandler
    ResponseEntity handleException(final ServiceUnavailableException exception) {
        logger.error("Ошибка обработки запроса. Сервис " + exception.getServiceName() + " недоступен.", exception);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    @ExceptionHandler
    ResponseEntity handleException(final HttpClientErrorException exception) {
        logger.error("Ошибка обработки запроса", exception);
        return ResponseEntity.status(exception.getRawStatusCode())
                .headers(exception.getResponseHeaders())
                .body(exception.getResponseBodyAsString());
    }

    @ExceptionHandler
    ResponseEntity handleException(final DataValidationException exception) {
        logger.error("Ошибка обработки запроса: запрос не прошёл валидацию", exception);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getErrors());
    }
}
