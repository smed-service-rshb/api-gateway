package ru.softlab.efr.infrastructure.apigateway;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.impl.client.BasicCookieStore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.util.UriComponentsBuilder;
import ru.softlab.efr.infrastructure.apigateway.config.TestApplicationConfiguration;
import ru.softlab.efr.infrastructure.apigateway.services.auth.AuthenticationFailedException;
import ru.softlab.efr.services.auth.exchange.BadEntityRs;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static ru.softlab.efr.infrastructure.apigateway.Utils.*;

/**
 * @author krenev
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestApplicationConfiguration.class)
public class ApiGatewayIntegrationTest extends IntegrationTestBase {

    @Test
    public void testCorrectLogin_ShouldSuccess() throws Exception {
        HttpResponse response = correctLogin(new BasicCookieStore());

        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_OK));
        checkSessionSession(response);
    }

    @Test
    public void testIncorrectPassword_ShouldUnauthorized() throws Exception {
        HttpResponse response = login(new BasicCookieStore(), CORRECT_LOGIN, INCORRECT_PASSWORD);

        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_UNAUTHORIZED));

        checkErrorResponse(response, AuthenticationFailedException.ERROR_CODE, AuthenticationFailedException.ERROR_MESSAGE);
    }

    @Test
    public void testLoginBlocked_ShouldThrowNotAcceptable() throws Exception {
        HttpResponse response = login(new BasicCookieStore(), USER_BLOCKED_LOGIN, CORRECT_PASSWORD);

        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_NOT_ACCEPTABLE));

        checkNotAcceptableErrorResponse(response, BadEntityRs.Type.BLOCKED);
    }

    @Test
    public void testLoginOfficeNotFound_ShouldNotAcceptable() throws Exception {
        HttpResponse response = login(new BasicCookieStore(), USER_WRONG_DEPARTMENT_LOGIN, CORRECT_PASSWORD);

        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_NOT_ACCEPTABLE));

        checkNotAcceptableErrorResponse(response, BadEntityRs.Type.OFFICE_NOT_FOUND);
    }

    @Test
    public void testLoginRoleNotFound_ShouldNotAcceptable() throws Exception {
        HttpResponse response = login(new BasicCookieStore(), USER_WRONG_ROLE_LOGIN, CORRECT_PASSWORD);

        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_NOT_ACCEPTABLE));

        checkNotAcceptableErrorResponse(response, BadEntityRs.Type.ROLE_NOT_FOUND);
    }

    @Test
    public void testLoginEmptyRole_ShouldNotAcceptable() throws Exception {
        HttpResponse response = login(new BasicCookieStore(), USER_EMPTY_ROLE_LOGIN, CORRECT_PASSWORD);

        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_NOT_ACCEPTABLE));

        checkNotAcceptableErrorResponse(response, BadEntityRs.Type.EMPTY_ROLE);
    }

    @Test
    public void testSession_ShouldUnauthorized() throws Exception {
        HttpResponse response = session(new BasicCookieStore());

        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_UNAUTHORIZED));
        checkEmptyResponse(response);
    }

    @Test
    public void testSessionInfoAfterLogin_ShouldSuccess() throws Exception {
        BasicCookieStore cookieStore = new BasicCookieStore();
        HttpResponse response = correctLogin(cookieStore);
        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_OK));
        checkSessionSession(response);

        response = session(cookieStore);

        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_OK));
        checkSessionSession(response);
    }

    @Test
    public void testCorrectLoginAndLogoutAndSessionInfo_ShouldUnauthorized() throws Exception {
        BasicCookieStore cookieStore = new BasicCookieStore();

        HttpResponse response = correctLogin(cookieStore);
        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_OK));
        checkSessionSession(response);

        response = session(cookieStore);
        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_OK));
        checkSessionSession(response);

        response = logout(cookieStore);
        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_OK));

        response = session(cookieStore);
        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_UNAUTHORIZED));
        checkEmptyResponse(response);
    }

    @Test
    public void testLogout_ShouldSuccess() throws Exception {
        HttpResponse response = logout(new BasicCookieStore());
        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_OK));
        checkEmptyResponse(response);
    }


    //////////////////Proxy
    @Test
    public void testProxyPublicResourceWithoutAuthentication_ShouldSuccess() throws Exception {
        HttpResponse response = someServicePublic(new BasicCookieStore());

        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_OK));
        String content = getContent(response);
        assertThat(content, is(PUBLIC_RESOURCE_RESPONSE));
    }

    @Test
    public void testProxyPublicResourceWithAuthentication_ShouldSuccess() throws Exception {
        BasicCookieStore cookieStore = new BasicCookieStore();

        HttpResponse response = correctLogin(cookieStore);
        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_OK));
        checkSessionSession(response);

        response = someServicePublic(cookieStore);
        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_OK));
        String content = getContent(response);
        assertThat(content, is(PUBLIC_RESOURCE_RESPONSE));
    }

    @Test
    public void testProxyPrivateResourceWithoutAuthentication_ShouldNotFound() throws Exception {
        HttpResponse response = someServicePrivate(new BasicCookieStore());

        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_NOT_FOUND));
        checkEmptyResponse(response);
    }

    @Test
    public void testProxyPrivateResourceWithAuthentication_ShouldNotFound() throws Exception {
        BasicCookieStore cookieStore = new BasicCookieStore();

        HttpResponse response = correctLogin(cookieStore);
        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_OK));
        checkSessionSession(response);

        response = someServicePrivate(cookieStore);
        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_NOT_FOUND));
        checkEmptyResponse(response);
    }

    @Test
    public void testProxyProtectedResourceWithoutAuthentication_ShouldUnauthorized() throws Exception {
        HttpResponse response = someServiceProtected(new BasicCookieStore());

        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_UNAUTHORIZED));
        checkEmptyResponse(response);
    }

    @Test
    public void testProxyProtectedResourceWithAuthentication_ShouldSuccess() throws Exception {
        BasicCookieStore cookieStore = new BasicCookieStore();

        HttpResponse response = correctLogin(cookieStore);
        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_OK));
        checkSessionSession(response);

        response = someServiceProtected(cookieStore);
        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_OK));
        String content = getContent(response);
        assertThat(content, is(PROTECTED_RESOURCE_RESPONSE));
    }

    @Test
    public void testProxyWrongPath_ShouldNotFound() throws Exception {
        HttpResponse response = someServiceWrongPath(new BasicCookieStore());

        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_NOT_FOUND));
        checkEmptyResponse(response);
    }

    @Test
    public void testWrongRoute_ShouldNotFound() throws Exception {
        HttpResponse response = apiGateWayWrongRoute(new BasicCookieStore());

        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_NOT_FOUND));
        checkEmptyResponse(response);
    }

    @Test
    public void testCyrillicParam() throws Exception {
        String message = "кириллица";
        HttpResponse response = someServicePublicEcho(new BasicCookieStore(), message);

        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_OK));
        String content = getContent(response);
        assertThat(content, is(message));
    }

    @Test
    public void testComplexCyrillicParams() throws Exception {
        String first = "параметр с пробелом";
        String second1 = "элемент 1";
        String second2 = "элемент 2";
        String third = second1 + "," + second2;

        HttpResponse response = someServiceGetRequest(new BasicCookieStore(), UriComponentsBuilder
                .fromUriString(apiGatewayPath("/super-service/v1/public/three-params"))
                .queryParam("first", first)
                .queryParam("second", second1, second2)
                .queryParam("third", third)
                .build()
                .toUri());

        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_OK));
        String content = getContent(response);
        assertThat(content, is(String.format("%s%s%s", first,
                Arrays.toString(Arrays.asList(second1, second2).toArray()),
                Arrays.toString(Arrays.asList(second1, second2).toArray()))));
    }

    @Test
    public void testHttpClientErrorException() throws Exception {
        HttpResponse response = someServiceGetRequest(new BasicCookieStore(), UriComponentsBuilder
                .fromUriString(apiGatewayPath("/super-service/v1/public/exception/client"))
                .build()
                .toUri());
        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_BAD_REQUEST));
        assertEquals(getContent(response), "Http client error exception");
    }

    @Test
    public void testHttpServerErrorException() throws Exception {
        HttpResponse response = someServiceGetRequest(new BasicCookieStore(), UriComponentsBuilder
                .fromUriString(apiGatewayPath("/super-service/v1/public/exception/server"))
                .build()
                .toUri());
        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_INTERNAL_SERVER_ERROR));
        assertThat(getContent(response), isEmptyString());
    }

    @Test
    public void testCheckCacheHeader() throws Exception {
        HttpResponse response = correctLogin(new BasicCookieStore());

        Header[] cacheControlHeaders = response.getHeaders(HttpHeaders.CACHE_CONTROL);
        assertThat(cacheControlHeaders, arrayWithSize(1));
        assertThat(cacheControlHeaders[0].getValue(), is("no-cache"));

    }
}