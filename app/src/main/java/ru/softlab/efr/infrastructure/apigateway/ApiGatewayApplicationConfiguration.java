package ru.softlab.efr.infrastructure.apigateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import ru.softlab.efr.infrastructure.apigateway.controllers.PrincipalDataStoreImpl;
import ru.softlab.efr.infrastructure.transport.MicroServiceRequestInterceptor;
import ru.softlab.efr.infrastructure.transport.annotations.EnableTransport;
import ru.softlab.efr.infrastructure.transport.client.MicroServiceTemplate;
import ru.softlab.efr.services.auth.RolesManageAuthServiceClient;
import ru.softlab.efr.services.auth.SessionsManageAuthServiceClient;
import ru.softlab.efr.services.authorization.interceptors.ClientInterceptor;

/**
 * Конфигурация приложения
 */
@Configuration
@PropertySource("classpath:application.properties")
@ComponentScan
@EnableTransport(ApiGatewayApplicationConfiguration.APP_NAME)
public class ApiGatewayApplicationConfiguration {

    public static final String APP_NAME = "api-gateway";

    @Value("${services.interaction.timeout}")
    private int timeout;

    /**
     * @return настройки таймаута
     */
    @Bean
    public TimeoutConfiguration timeoutConfiguration() {
        return new TimeoutConfiguration(timeout);
    }

    /**
     * @param microServiceTemplate клиент транспортной компоненты
     * @return клиент сервиса аутентификации по работе с сессиями
     */
    @Bean
    public SessionsManageAuthServiceClient sessionsManageAuthServiceClient(MicroServiceTemplate microServiceTemplate) {
        return new SessionsManageAuthServiceClient(microServiceTemplate);
    }

    /**
     * @param microServiceTemplate клиент транспортной компоненты
     * @return клиент сервиса аутентификации по работе с ролями и разрешениями
     */
    @Bean
    public RolesManageAuthServiceClient rolesManageAuthServiceClient(MicroServiceTemplate microServiceTemplate) {
        return new RolesManageAuthServiceClient(microServiceTemplate);
    }

    /**
     * Регистрация кастомного интерсептора, прокидывающего в заголовках данные аутентифицированного пользователя.
     *
     * @param applicationContext контекст приложения
     * @return интерсептор
     */
    @Bean
    public MicroServiceRequestInterceptor microServiceRequestInterceptor(ApplicationContext applicationContext) {
        return new ClientInterceptor(new PrincipalDataStoreImpl(applicationContext));
    }
}
