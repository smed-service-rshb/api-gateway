package ru.softlab.efr.infrastructure.apigateway.controllers;

/**
 * тип данных для ответа, содержищего ошибку
 */
public class ErrorResponseType {

    private String errorCode;
    private String errorMessage;

    public ErrorResponseType() {
    }


    /**
     * Конструктор
     *
     * @param errorCode    бизнесовый код ошибки
     * @param errorMessage сообщение об ощибке
     */
    public ErrorResponseType(final String errorCode, final String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String value) {
        this.errorCode = value;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String value) {
        this.errorMessage = value;
    }
}
