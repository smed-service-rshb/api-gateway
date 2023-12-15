package ru.softlab.efr.infrastructure.apigateway.controllers.auth;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.softlab.efr.infrastructure.apigateway.TestConfiguration;
import ru.softlab.efr.infrastructure.apigateway.controllers.GlobalExceptionHandler;
import ru.softlab.efr.infrastructure.apigateway.services.auth.AuthenticationFailedException;
import ru.softlab.efr.infrastructure.apigateway.services.auth.AuthenticationService;
import ru.softlab.efr.services.auth.Right;
import ru.softlab.efr.services.auth.UserData;

import javax.servlet.http.HttpSession;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.*;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.softlab.efr.infrastructure.apigateway.Utils.*;

/**
 * @author krenev
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
@WebAppConfiguration
public class AuthControllerTest {
    private static final String LOGIN_PATH = "/auth/v1/login";
    private static final String LOGOUT_PATH = "/auth/v1/logout";
    private static final String SESSION_PATH = "/auth/v1/session";

    private static final UserData CORRECT_USER = new UserData();

    static {
        CORRECT_USER.setId(123L);
        CORRECT_USER.setLogin(CORRECT_LOGIN);
        CORRECT_USER.setFirstName("Василий");
        CORRECT_USER.setSecondName("Пупкин");
        CORRECT_USER.setMiddleName("Сергеевич");
        CORRECT_USER.setMobilePhone("+7911-908-07-06");
        CORRECT_USER.setEmail("pupkin@ya.ru");
        CORRECT_USER.setPosition("Исполняющий обязанности");
        CORRECT_USER.setRights(Arrays.asList(Right.VIEW_ROLES, Right.EDIT_ROLES));
        CORRECT_USER.setOffice("8805553555");
        CORRECT_USER.setBranch("2316798666");
        CORRECT_USER.setPersonnelNumber("937742");
    }

    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthenticationService authService;

    private MockMvc mockMvc;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        when(authService.authenticate(eq(CORRECT_LOGIN), eq(CORRECT_PASSWORD))).thenReturn(CORRECT_USER);
        when(authService.authenticate(eq(CORRECT_LOGIN), not(eq(CORRECT_PASSWORD)))).thenThrow(new AuthenticationFailedException());
        when(authService.authenticate(eq(INCORRECT_LOGIN), anyString())).thenThrow(new AuthenticationFailedException());
        when(authService.authenticate(eq(EXCEPTION_LOGIN), anyString())).thenThrow(new RuntimeException());
        when(authService.getUser()).thenReturn(CORRECT_USER);
    }


    @Test
    public void loginWithCorrectData_ShouldLogin() throws Exception {
        ResultActions mvcResult = doLogin(CORRECT_LOGIN, CORRECT_PASSWORD, status().isOk());

        checkSession(mvcResult, CORRECT_USER);

        verify(authService).authenticate(eq(CORRECT_LOGIN), anyString());
    }

    @Test
    public void loginIncorrectPassword_ShouldReturnUnauthorized() throws Exception {
        testIncorrectAuth(CORRECT_LOGIN, INCORRECT_PASSWORD);
    }

    @Test
    public void loginIncorrectLogin_ShouldReturnUnauthorized() throws Exception {
        testIncorrectAuth(INCORRECT_LOGIN, INCORRECT_PASSWORD);
    }

    private void testIncorrectAuth(String login, String password) throws Exception {
        MvcResult mvcResult = doLogin(login, password, status().isUnauthorized()).andReturn();

        checkErrorResponse(mvcResult.getResponse().getContentAsString(), AuthenticationFailedException.ERROR_CODE, AuthenticationFailedException.ERROR_MESSAGE);
        HttpSession createdSession = mvcResult.getRequest().getSession(false);

        assertNull("Новая сессия не открыта", createdSession);

        verify(authService).authenticate(eq(login), eq(password));
    }

    @Test
    public void loginException_ShouldReturnInternalServerErrorWithEmptyBody() throws Exception {
        String request = createLoginRequest(EXCEPTION_LOGIN, CORRECT_PASSWORD);

        String response = mockMvc.perform(post(LOGIN_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))

                .andExpect(status().isInternalServerError())
                .andReturn().getResponse().getContentAsString();

        assertThat("Респонс", response, isEmptyString());

        verify(authService).authenticate(eq(EXCEPTION_LOGIN), anyString());
    }

    @Test
    public void loginWithGETMethod_ShouldReturnMethodNotAllowed() throws Exception {
        mockMvc.perform(get(LOGIN_PATH))
                .andExpect(status().isMethodNotAllowed());

        verify(authService, never()).authenticate(anyString(), anyString());
    }

    @Test
    public void loginWithoutData_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post(LOGIN_PATH))
                .andExpect(status().isBadRequest());
        verify(authService, never()).authenticate(anyString(), anyString());
    }

    @Test
    public void loginIncorrectContentType_ShouldReturnUnsupportedMediaType() throws Exception {
        String request = createLoginRequest(CORRECT_LOGIN, CORRECT_PASSWORD);
        mockMvc.perform(post(LOGIN_PATH)
                .contentType(MediaType.TEXT_PLAIN)
                .content(request))
                .andExpect(status().isUnsupportedMediaType());
        verify(authService, never()).authenticate(anyString(), anyString());
    }

    @Test
    public void loginIncorrectData_ShouldReturnBadRequest() throws Exception {
        String request = "SomeString";
        mockMvc.perform(post(LOGIN_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest());
        verify(authService, never()).authenticate(anyString(), anyString());
    }

    @Test
    public void loginWithoutLogin_ShouldReturnBadRequest() throws Exception {
        String request = createLoginRequest(null, null);
        mockMvc.perform(post(LOGIN_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest());
        verify(authService, never()).authenticate(anyString(), anyString());
    }

    @Test
    public void loginWithoutPassword_ShouldReturnBadRequest() throws Exception {
        String request = createLoginRequest(CORRECT_LOGIN, null);
        mockMvc.perform(post(LOGIN_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getSession_ShouldReturnSessionInfo() throws Exception {
        ResultActions resultActions = mockMvc.perform(get(SESSION_PATH))
                .andExpect(status().isOk());
        checkSession(resultActions, CORRECT_USER);

        verify(authService).getUser();
    }

    @Test
    public void logoutWithoutSession_ShouldOK() throws Exception {
        MvcResult mvcResult = doLogout(null);
        assertThat("Респонс", mvcResult.getResponse().getContentAsString(), isEmptyString());
        verify(authService).logout();
    }

    @Test
    public void logoutWithSession_ShouldOKAndSessionInvalidated() throws Exception {
        MockHttpSession oldSession = new MockHttpSession();
        MvcResult mvcResult = doLogout(oldSession);
        assertTrue("Сессия инвалидирована", oldSession.isInvalid());

        assertThat("Респонс", mvcResult.getResponse().getContentAsString(), isEmptyString());
        verify(authService).logout();
    }

    private ResultActions doLogin(String login, String pwd, ResultMatcher resultMatcher) throws Exception {
        MockHttpSession session = new MockHttpSession();

        String request = createLoginRequest(login, pwd);

        ResultActions mvcResult = mockMvc.perform(post(LOGIN_PATH)
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(resultMatcher);

        assertTrue("Старая сессия инвалидирована", session.isInvalid());
        return mvcResult;
    }

    private MvcResult doLogout(MockHttpSession oldSession) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = post(LOGOUT_PATH);
        if (oldSession != null) {
            requestBuilder.session(oldSession);
        }
        MvcResult mvcResult = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn();
        assertNull("Сессия не создана", mvcResult.getRequest().getSession(false));
        return mvcResult;
    }

    private void checkSession(ResultActions response, UserData correctUser) throws Exception {
        response.andExpect(jsonPath("$.user.name", is(correctUser.getFirstName())));
        response.andExpect(jsonPath("$.user.surname", is(correctUser.getSecondName())));
        response.andExpect(jsonPath("$.user.middleName", is(correctUser.getMiddleName())));
        response.andExpect(jsonPath("$.user.mobilePhone", is(correctUser.getMobilePhone())));
        response.andExpect(jsonPath("$.user.email", is(correctUser.getEmail())));
        response.andExpect(jsonPath("$.user.position", is(correctUser.getPosition())));
        response.andExpect(jsonPath("$.user.office", is(correctUser.getOffice())));
        response.andExpect(jsonPath("$.user.branch", is(correctUser.getBranch())));
        response.andExpect(jsonPath("$.user.personnelNumber", is(correctUser.getPersonnelNumber())));

        response.andExpect(jsonPath("$.rights.length()", is(correctUser.getRights().size())));

        for (Right right : correctUser.getRights()) {
            response.andExpect(jsonPath("$.rights[?(@ == '%s')]", right).exists());
        }
    }
}
