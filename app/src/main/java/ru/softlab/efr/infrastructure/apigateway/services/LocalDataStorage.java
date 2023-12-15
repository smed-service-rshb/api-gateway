package ru.softlab.efr.infrastructure.apigateway.services;

import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * Абстракция "хранилище данных"
 */
@Service
public interface LocalDataStorage {
    /**
     * Установить атрибут в хранилище
     *
     * @param attributeName имя атрибута
     * @param data          значение
     */
    void setAttribute(String attributeName, Serializable data);

    /**
     * Получить атрибут из хранилища
     *
     * @param attributeName имя атрибута
     * @return значние атрибута, или null.
     */
    Object getAttribute(String attributeName);

    /**
     * Удалить атрибут из хранилища
     *
     * @param attributeName имя атрибута
     */
    void removeAttribute(String attributeName);
}
