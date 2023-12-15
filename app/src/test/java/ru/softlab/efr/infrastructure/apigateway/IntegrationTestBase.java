package ru.softlab.efr.infrastructure.apigateway;

import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import ru.softlab.efr.services.auth.exchange.BadEntityRs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;
import static ru.softlab.efr.infrastructure.apigateway.Utils.CORRECT_PASSWORD;
import static ru.softlab.efr.infrastructure.apigateway.Utils.DOMAIN_USER_CORRECT_LOGIN;

class IntegrationTestBase {
    public static final String PUBLIC_RESOURCE_RESPONSE = "public resource";
    public static final String PRIVATE_RESOURCE_RESPONSE = "private resource";
    public static final String PROTECTED_RESOURCE_RESPONSE = "protected resource";

    @Value("${server.uri.auth-service}")
    private String authServiceUrl;

    @Value("${server.uri.api-gateway}")
    private String apiGatewayUrl;

    protected String apiGatewayPath(String path) {
        return apiGatewayUrl + path;
    }

    protected HttpResponse correctLogin(CookieStore cookieStore) throws IOException {
        return login(cookieStore, DOMAIN_USER_CORRECT_LOGIN, CORRECT_PASSWORD);
    }

    protected HttpResponse login(CookieStore cookieStore, String login, String passwd) throws IOException {
        return Executor.newInstance(createClient())
                .use(cookieStore)
                .execute(Request
                        .Post(apiGatewayPath("/auth/v1/login"))
                        .bodyString(Utils.createLoginRequest(login, passwd), ContentType.APPLICATION_JSON))
                .returnResponse();
    }

    protected HttpResponse logout(BasicCookieStore cookieStore) throws IOException {
        return Executor.newInstance(createClient())
                .use(cookieStore)
                .execute(Request.Post(apiGatewayPath("/auth/v1/logout")))
                .returnResponse();
    }

    protected HttpResponse session(CookieStore cookieStore) throws IOException {
        return Executor.newInstance(createClient())
                .use(cookieStore)
                .execute(Request.Get(apiGatewayPath("/auth/v1/session")))
                .returnResponse();
    }

    protected HttpResponse apiGateWayWrongRoute(CookieStore cookieStore) throws IOException {
        return Executor.newInstance(createClient())
                .use(cookieStore)
                .execute(Request.Get(apiGatewayPath("/wrong-route/v1/session")))
                .returnResponse();
    }

    protected HttpResponse someServicePublic(CookieStore cookieStore) throws IOException {
        return Executor.newInstance(createClient())
                .use(cookieStore)
                .execute(Request.Get(apiGatewayPath("/super-service/v1/public/resource")))
                .returnResponse();
    }

    protected HttpResponse someServicePrivate(CookieStore cookieStore) throws IOException {
        return Executor.newInstance(createClient())
                .use(cookieStore)
                .execute(Request.Get(apiGatewayPath("/super-service/v1/private/resource")))
                .returnResponse();
    }

    protected HttpResponse someServiceProtected(CookieStore cookieStore) throws IOException {
        return Executor.newInstance(createClient())
                .use(cookieStore)
                .execute(Request.Get(apiGatewayPath("/super-service/v1/protected/resource")))
                .returnResponse();
    }

    protected HttpResponse someServiceWrongPath(CookieStore cookieStore) throws IOException {
        return Executor.newInstance(createClient())
                .use(cookieStore)
                .execute(Request.Get(apiGatewayPath("/super-service/v1/public/wrong-path/resource")))
                .returnResponse();
    }

    protected HttpResponse someServicePublicEcho(CookieStore cookieStore, String message) throws IOException {
        return Executor.newInstance(createClient())
                .use(cookieStore)
                .execute(Request.Get(apiGatewayPath("/super-service/v1/public/echo?message=" + message)))
                .returnResponse();
    }

    protected HttpResponse someServiceGetRequest(CookieStore cookieStore, URI uri) throws IOException {
        return Executor.newInstance(createClient())
                .use(cookieStore)
                .execute(Request.Get(uri))
                .returnResponse();
    }

    private HttpClient createClient() {
        return HttpClients.
                custom()
                .setRetryHandler(new DefaultHttpRequestRetryHandler(3, false))
                .build();
    }

    protected String getContent(HttpResponse response) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        response.getEntity().writeTo(outputStream);
        return outputStream.toString(StandardCharsets.UTF_8.name());
    }

    protected void checkEmptyResponse(HttpResponse response) throws IOException {
        assertThat(getContent(response), isEmptyString());
    }

    protected void checkErrorResponse(HttpResponse response, String errorCode, String errorMessage) throws IOException {
        Utils.checkErrorResponse(getContent(response), errorCode, errorMessage);
    }

    protected void checkNotAcceptableErrorResponse(HttpResponse response, BadEntityRs.Type type) throws IOException {
        Utils.checkNotAcceptableErrorResponse(getContent(response), type);
    }

    protected void checkSessionSession(HttpResponse response) throws IOException {
        String content = getContent(response);
        assertThat(content, not(isEmptyString()));
        Utils.checkSessionResponse(content);
    }
}
