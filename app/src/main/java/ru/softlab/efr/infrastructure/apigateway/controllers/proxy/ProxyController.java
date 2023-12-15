package ru.softlab.efr.infrastructure.apigateway.controllers.proxy;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UrlPathHelper;
import ru.softlab.efr.infrastructure.apigateway.services.auth.AuthenticationService;
import ru.softlab.efr.infrastructure.apigateway.services.route.AmbiguousRoutesException;
import ru.softlab.efr.infrastructure.apigateway.services.route.ResourceType;
import ru.softlab.efr.infrastructure.apigateway.services.route.Route;
import ru.softlab.efr.infrastructure.apigateway.services.route.RouteMatcher;
import ru.softlab.efr.infrastructure.transport.client.MicroServiceTemplate;
import ru.softlab.efr.infrastructure.transport.client.impl.JmsUriBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Контролер, проксирующий запросы в конечные сервисы
 */
@RestController
public class ProxyController {
    private static final Logger LOGGER = Logger.getLogger(ProxyController.class);
    private static final String PROXY_MAPPING = "/**";
    private static final String UTF_8 = "UTF-8";

    private final RouteMatcher routeMatcher;
    private final MicroServiceTemplate microServiceTemplate;
    private final AuthenticationService authenticationService;

    /**
     * Конструктор контроллера
     *
     * @param routeMatcher          Матчер маршрутов
     * @param microServiceTemplate  клиент транспортной компоненты
     * @param authenticationService сервис аутентифкации
     */
    public ProxyController(RouteMatcher routeMatcher, MicroServiceTemplate microServiceTemplate, AuthenticationService authenticationService) {
        this.routeMatcher = routeMatcher;
        this.microServiceTemplate = microServiceTemplate;
        this.authenticationService = authenticationService;
    }

    /**
     * <p>Проксирование запросов по правилам маршрутизации.</p>
     * <ul>
     * <li>Для приватных маршрутов всегда возвращается 404.</li>
     * <li>Для публичных маршрутов ответ проксируется от конечного сервиса.</li>
     * <li>Для защищенных маршрутов:
     * <ul>
     * <li>если пользователь аутентифцирован - ответ проксируется от конечного сервиса.</li>
     * <li>если пользователь неаутентифцирован возвращается 401</li>
     * </ul>
     * </li>
     * </ul>
     *
     * @param httpServletRequest сырой http-запрос
     * @return ответ
     * @throws Exception при ошибках
     */
    @RequestMapping(value = PROXY_MAPPING)
    public ListenableFuture<?> proxy(HttpServletRequest httpServletRequest) throws Exception {
        String originalUrl = new UrlPathHelper().getPathWithinApplication(httpServletRequest);

        AntPathMatcher apm = new AntPathMatcher();
        String path = "/" + apm.extractPathWithinPattern(PROXY_MAPPING, originalUrl);

        Route route = routeMatcher.findRoute(path);
        if (route == null) {
            throw new DestinationServiceNotFoundException(httpServletRequest.getMethod(), originalUrl);
        }

        if (route.getResourceType() == ResourceType.PRIVATE) {
            throw new PrivateResourceAccessException(httpServletRequest.getMethod(), originalUrl);
        }

        if (route.getResourceType() == ResourceType.PROTECTED && !authenticationService.isUserAuthenticated()) {
            throw new AuthenticationRequiredException();
        }

        URI url = JmsUriBuilder.service(route.getServiceName())
                .path(path)
                .query(decode(httpServletRequest.getQueryString()))
                .build();

        ServletServerHttpRequest inputMessage = new ServletServerHttpRequest(httpServletRequest);

        long contentLength = inputMessage.getHeaders().getContentLength();
        ByteArrayOutputStream bos =
                new ByteArrayOutputStream(contentLength >= 0 ? (int) contentLength : StreamUtils.BUFFER_SIZE);
        StreamUtils.copy(inputMessage.getBody(), bos);

        RequestEntity<Object> request = new RequestEntity<>(bos.toByteArray(), inputMessage.getHeaders(), inputMessage.getMethod(), url);

        return microServiceTemplate.exchange(request, String.class);
    }

    private static String decode(String query) throws UnsupportedEncodingException {
        return StringUtils.isEmpty(query) ? query : URLDecoder.decode(query, UTF_8);
    }

    @ExceptionHandler
    ResponseEntity handleException(final AmbiguousRoutesException exception) {
        String routes = Arrays.stream(exception.getRoutes()).map(Route::toString).collect(Collectors.joining(" | "));
        LOGGER.warn("Для " + exception.getPath() + " найден более 1 маршрута (" + routes + ")");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @ExceptionHandler
    ResponseEntity handleException(final DestinationServiceNotFoundException exception) {
        LOGGER.warn("Не найден сервис для обработки запроса " + exception.getMethod() + " " + exception.getUrl());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @ExceptionHandler
    ResponseEntity handleException(final PrivateResourceAccessException exception) {
        LOGGER.warn("Доступ к защищенному ресурсу " + exception.getMethod() + " " + exception.getUrl());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @ExceptionHandler(AuthenticationRequiredException.class)
    ResponseEntity handleException() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
