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
public class WithoutSomeServiceIntegrationTest extends IntegrationTestBase {

    @Test
    public void testProxyPublicResourceWithoutAuthentication_ShouldServiceUnavailable() throws Exception {
        HttpResponse response = someServicePublic(new BasicCookieStore());

        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_SERVICE_UNAVAILABLE));
        checkEmptyResponse(response);
    }

    @Test
    public void testProxyPublicResourceWithAuthentication_ShouldServiceUnavailable() throws Exception {
        BasicCookieStore cookieStore = new BasicCookieStore();

        HttpResponse response = correctLogin(cookieStore);
        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_OK));
        checkSessionSession(response);

        response = someServicePublic(cookieStore);
        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_SERVICE_UNAVAILABLE));
        checkEmptyResponse(response);
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
    public void testProxyProtectedResourceWithAuthentication_ShouldServiceUnavailable() throws Exception {
        BasicCookieStore cookieStore = new BasicCookieStore();

        HttpResponse response = correctLogin(cookieStore);
        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_OK));
        checkSessionSession(response);

        response = someServiceProtected(cookieStore);
        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_SERVICE_UNAVAILABLE));
        checkEmptyResponse(response);
    }

    @Test
    public void testProxyWrongPath_ShouldServiceUnavailable() throws Exception {
        HttpResponse response = someServiceWrongPath(new BasicCookieStore());

        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_SERVICE_UNAVAILABLE));
        checkEmptyResponse(response);
    }

    @Test
    public void testWrongRoute_ShouldNotFound() throws Exception {
        HttpResponse response = apiGateWayWrongRoute(new BasicCookieStore());

        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_NOT_FOUND));
        checkEmptyResponse(response);
    }
}