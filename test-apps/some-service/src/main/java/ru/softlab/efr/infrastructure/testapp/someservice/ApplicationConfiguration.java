package ru.softlab.efr.infrastructure.testapp.someservice;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ru.softlab.efr.infrastructure.transport.annotations.EnableTransport;

@Configuration
@ComponentScan
@EnableTransport(ApplicationConfiguration.APP_NAME)
public class ApplicationConfiguration {

    public static final String APP_NAME = "some-service";
}