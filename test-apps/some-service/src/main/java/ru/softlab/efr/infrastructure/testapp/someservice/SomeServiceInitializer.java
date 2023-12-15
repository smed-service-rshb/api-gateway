package ru.softlab.efr.infrastructure.testapp.someservice;

import ru.softlab.efr.infrastructure.transport.server.init.AbstractTransportInitializer;

public class SomeServiceInitializer extends AbstractTransportInitializer {
    protected Class<?> getConfiguration() {
        return ApplicationConfiguration.class;
    }
}
