package ru.softlab.efr.infrastructure.apigateway.controllers;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.softlab.efr.infrastructure.apigateway.services.LocalDataStorage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.Serializable;

/**
 * Хранилище данных на основе http-session
 */
@Service
public class SessionLocalDataStorage implements LocalDataStorage {

    private final HttpServletRequest httpRequest;

    /**
     * Конструктор
     *
     * @param httpRequest запрос
     */
    public SessionLocalDataStorage(HttpServletRequest httpRequest) {
        Assert.notNull(httpRequest, "Параметр httpRequest не должен быть пустым");
        this.httpRequest = httpRequest;
    }

    @Override
    public void setAttribute(String attributeName, Serializable data) {
        HttpSession session = httpRequest.getSession();
        session.setAttribute(attributeName, data);
    }

    @Override
    public Object getAttribute(String attributeName) {
        HttpSession session = httpRequest.getSession(false);
        if (session == null) {
            return null;
        }
        return session.getAttribute(attributeName);
    }

    @Override
    public void removeAttribute(String attributeName) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.removeAttribute(attributeName);
        }
    }
}
