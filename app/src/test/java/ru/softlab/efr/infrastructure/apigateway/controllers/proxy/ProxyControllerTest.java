package ru.softlab.efr.infrastructure.apigateway.controllers.proxy;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.softlab.efr.infrastructure.apigateway.TestConfiguration;
import ru.softlab.efr.infrastructure.apigateway.controllers.GlobalExceptionHandler;
import ru.softlab.efr.infrastructure.apigateway.services.auth.AuthenticationService;
import ru.softlab.efr.infrastructure.apigateway.services.route.ResourceType;
import ru.softlab.efr.infrastructure.apigateway.services.route.Route;
import ru.softlab.efr.infrastructure.apigateway.services.route.RouteMatcher;
import ru.softlab.efr.infrastructure.transport.client.MicroServiceTemplate;
import ru.softlab.efr.infrastructure.transport.client.impl.JMSMicroServiceTemplate;

import static org.hamcrest.text.IsEmptyString.isEmptyString;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author krenev
 * @since 13.04.2017
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
@WebAppConfiguration
@Ignore // TODO: необходимо перевести тесты API Gateway на поддержку транпортной компоненты 4.1.0
public class ProxyControllerTest {

    private static final String NOT_FOUND_SERVICE_PATH = "/NOT-FOUND-SERVICE-PATH";
    private static final String PRIVATE_PATH = "/PRIVATE_PATH";
    private static final String PUBLIC_PATH = "/PUBLIC_PATH";
    private static final String EXCEPTION_PATH = "/EXCEPTION_PATH";
    private static final String PROTECTED_PATH = "/PROTECTED_PATH";

    @Spy
    @InjectMocks
    private ProxyController proxyController;

    @Mock
    private AuthenticationService authService;

    @Mock
    private JMSMicroServiceTemplate serviceTemplate;

    @Mock
    private RouteMatcher routeMatcher;

    private MockMvc mockMvc;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(proxyController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        when(routeMatcher.findRoute(eq(PUBLIC_PATH))).thenReturn(new Route("some-service", "some-path", ResourceType.PUBLIC));
        when(routeMatcher.findRoute(eq(PROTECTED_PATH))).thenReturn(new Route("some-service", "some-path", ResourceType.PROTECTED));
        when(routeMatcher.findRoute(eq(PRIVATE_PATH))).thenReturn(new Route("some-service", "some-path", ResourceType.PRIVATE));
        when(routeMatcher.findRoute(eq(EXCEPTION_PATH))).thenThrow(RuntimeException.class);
    }

    @Test
    public void whenRouteInfoNotFound_ShouldReturnNotFound() throws Exception {
        ResultActions mvcResult = mockMvc.perform(post(NOT_FOUND_SERVICE_PATH))
                .andExpect(status().isNotFound());

        assertThat(mvcResult.andReturn().getResponse().getContentAsString(), isEmptyString());
        verifyZeroInteractions(serviceTemplate);
    }

    @Test
    public void whenGetRouteInfoException_ShouldReturnInternalServerError() throws Exception {
        ResultActions mvcResult = mockMvc.perform(post(EXCEPTION_PATH))
                .andExpect(status().isInternalServerError());
        assertThat(mvcResult.andReturn().getResponse().getContentAsString(), isEmptyString());
        verifyZeroInteractions(serviceTemplate);
    }

    @Test
    public void whenProtectedRouteAndUserUnauthorized_ShouldReturnForbidden() throws Exception {
        when(authService.isUserAuthenticated()).thenReturn(false);

        ResultActions mvcResult = mockMvc.perform(post(PROTECTED_PATH))
                .andExpect(status().isUnauthorized());

        assertThat(mvcResult.andReturn().getResponse().getContentAsString(), isEmptyString());
        verifyZeroInteractions(serviceTemplate);
    }

    @Test
    public void whenPrivateRouteAndAuthorized_ShouldReturnNotFound() throws Exception {
        when(authService.isUserAuthenticated()).thenReturn(true);
        ResultActions mvcResult = mockMvc.perform(post(PRIVATE_PATH))
                .andExpect(status().isNotFound());

        assertThat(mvcResult.andReturn().getResponse().getContentAsString(), isEmptyString());
        verifyZeroInteractions(serviceTemplate);
    }

    @Test
    public void whenPrivateRouteAndUnauthorized_ShouldReturnNotFound() throws Exception {
        when(authService.isUserAuthenticated()).thenReturn(false);
        ResultActions mvcResult = mockMvc.perform(post(PRIVATE_PATH))
                .andExpect(status().isNotFound());

        assertThat(mvcResult.andReturn().getResponse().getContentAsString(), isEmptyString());
        verifyZeroInteractions(serviceTemplate);
    }

    @Test
    public void whenPublicRouteAndUnauthorized_ShouldOk() throws Exception {
        when(authService.isUserAuthenticated()).thenReturn(false);
        ResultActions mvcResult = mockMvc.perform(post(PUBLIC_PATH))
                .andExpect(status().isOk());
        //как проверить что был вызов любого метода serviceTemplate?
    }

    @Test
    public void whenPublicRouteAndAuthorized_ShouldOk() throws Exception {
        when(authService.isUserAuthenticated()).thenReturn(true);
        ResultActions mvcResult = mockMvc.perform(post(PUBLIC_PATH))
                .andExpect(status().isOk());
        //как проверить что был вызов любого метода serviceTemplate?
    }

    @Test
    public void whenProtectedRouteAndAuthorized_ShouldReturnOk() throws Exception {
        when(authService.isUserAuthenticated()).thenReturn(true);
        ResultActions mvcResult = mockMvc.perform(post(PROTECTED_PATH))
                .andExpect(status().isOk());
        //как проверить что был вызов любого метода serviceTemplate?
    }

}
