package ru.softlab.efr.infrastructure.apigateway;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.impl.client.BasicCookieStore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.softlab.efr.infrastructure.apigateway.config.TestApplicationConfiguration;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author krenev
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestApplicationConfiguration.class)
public class WithoutAuthServiceIntegrationTest extends IntegrationTestBase {

    @Test
    public void testCorrectLogin_ShouldServiceUnavailable() throws Exception {
        HttpResponse response = correctLogin(new BasicCookieStore());
        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_SERVICE_UNAVAILABLE));
        checkEmptyResponse(response);
    }

    @Test
    public void testSessionInfo_ShouldUnauthorized() throws Exception {
        HttpResponse response = session(new BasicCookieStore());

        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_UNAUTHORIZED));
        checkEmptyResponse(response);
    }

    @Test
    public void testLogout_ShouldSuccess() throws Exception {
        HttpResponse response = logout(new BasicCookieStore());
        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_OK));
        checkEmptyResponse(response);
    }

    @Test
    public void testProxyPublicResourceWithoutAuthentication_ShouldSuccess() throws Exception {
        HttpResponse response = someServicePublic(new BasicCookieStore());

        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_OK));
        String content = getContent(response);
        assertThat(content, is(PUBLIC_RESOURCE_RESPONSE));
    }

    @Test
    public void testProxyProtectedResourceWithoutAuthentication_ShouldUnauthorized() throws Exception {
        HttpResponse response = someServiceProtected(new BasicCookieStore());

        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_UNAUTHORIZED));
        checkEmptyResponse(response);
    }

    @Test
    public void testProxyPrivateResourceWithoutAuthentication_ShouldNotFound() throws Exception {
        HttpResponse response = someServicePrivate(new BasicCookieStore());

        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_NOT_FOUND));
        checkEmptyResponse(response);
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
}