package ru.softlab.efr.infrastructure.apigateway.services;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author krenev
 * @since 10.04.2017
 */
@Service
@Profile("test")
@Primary
public class SimpleLocalDataStorage implements LocalDataStorage {

    private Map<String, Serializable> storage = new HashMap<>();

    @Override
    public void setAttribute(String attributeName, Serializable data) {
        storage.put(attributeName, data);
    }

    @Override
    public Object getAttribute(String attributeName) {
        return storage.get(attributeName);
    }

    @Override
    public void removeAttribute(String attributeName) {
        storage.remove(attributeName);
    }
}
