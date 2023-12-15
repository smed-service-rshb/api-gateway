package ru.softlab.efr.infrastructure.apigateway.services.route;

/**
 * Тип ресурса
 */
public enum ResourceType {
    /**
     * Публичный ресурс, не требующий авторизации
     */
    PUBLIC,

    /**
     * Защищенный ресурс, требующий авторизации
     */
    PROTECTED,

    /**
     * Закрытый ресурс, доступ к которому запрещен
     */
    PRIVATE,

}
