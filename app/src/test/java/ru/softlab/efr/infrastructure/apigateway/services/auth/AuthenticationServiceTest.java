package ru.softlab.efr.infrastructure.apigateway.services.auth;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.softlab.efr.infrastructure.apigateway.TestConfiguration;
import ru.softlab.efr.infrastructure.apigateway.TimeoutConfiguration;
import ru.softlab.efr.infrastructure.apigateway.services.LocalDataStorage;
import ru.softlab.efr.infrastructure.apigateway.services.SimpleLocalDataStorage;
import ru.softlab.efr.services.auth.SessionsManageAuthServiceClient;
import ru.softlab.efr.services.auth.UserData;
import ru.softlab.efr.services.auth.exceptions.*;
import ru.softlab.efr.services.auth.exchange.CreateSessionRq;
import ru.softlab.efr.services.auth.exchange.CreateSessionRs;
import ru.softlab.efr.services.auth.exchange.model.OrgUnitData;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static ru.softlab.efr.infrastructure.apigateway.Utils.CORRECT_PASSWORD;

/**
 * @author krenev
 * @since 10.04.2017
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class AuthenticationServiceTest {
    private static final String CORRECT_LOGIN1 = "CORRECT_LOGIN1";
    private static final String CORRECT_LOGIN2 = "CORRECT_LOGIN2";
    private static final String INCORRECT_LOGIN = "INCORRECT_LOGIN";
    private static final String INCORRECT_PASSWORD = "INCORRECT_PASSWORD";
    private static final String EXCEPTION_LOGIN = "EXCEPTION_LOGIN";
    private static final String BLOCKED_LOGIN = "BLOCKED_LOGIN";
    private static final String INVALID_OFFICE_LOGIN = "INVALID_OFFICE_LOGIN";
    private static final String INVALID_ROLE_LOGIN = "INVALID_ROLE_LOGIN";
    private static final String EMPTY_ROLE_LOGIN = "EMPTY_ROLE_LOGIN";

    private static final CreateSessionRq CORRECT_LOGIN1_CORRECT_PASSWORD = new CreateSessionRq(CORRECT_LOGIN1, CORRECT_PASSWORD);
    private static final CreateSessionRq CORRECT_LOGIN2_CORRECT_PASSWORD = new CreateSessionRq(CORRECT_LOGIN2, CORRECT_PASSWORD);
    private static final CreateSessionRq CORRECT_LOGIN1_INCORRECT_PASSWORD = new CreateSessionRq(CORRECT_LOGIN1, INCORRECT_PASSWORD);
    private static final CreateSessionRq CORRECT_LOGIN2_INCORRECT_PASSWORD = new CreateSessionRq(CORRECT_LOGIN2, INCORRECT_PASSWORD);
    private static final CreateSessionRq EXCEPTION_CREDENTIALS = new CreateSessionRq(EXCEPTION_LOGIN, CORRECT_PASSWORD);
    private static final CreateSessionRq INCORRECT_LOGIN_CORRECT_PASSWORD = new CreateSessionRq(INCORRECT_LOGIN, CORRECT_PASSWORD);
    private static final CreateSessionRq BLOCKED_LOGIN_CREDENTIALS = new CreateSessionRq(BLOCKED_LOGIN, CORRECT_PASSWORD);
    private static final CreateSessionRq INVALID_OFFICE_LOGIN_CREDENTIALS = new CreateSessionRq(INVALID_OFFICE_LOGIN, CORRECT_PASSWORD);
    private static final CreateSessionRq INVALID_ROLE_LOGIN_CREDENTIALS = new CreateSessionRq(INVALID_ROLE_LOGIN, CORRECT_PASSWORD);
    private static final CreateSessionRq EMPTY_ROLE_LOGIN_CREDENTIALS = new CreateSessionRq(EMPTY_ROLE_LOGIN, CORRECT_PASSWORD);

    private static final UserData CORRECT_USER1 = new UserData();

    static {
        CORRECT_USER1.setId(123L);
        CORRECT_USER1.setLogin(CORRECT_LOGIN1);
        CORRECT_USER1.setFirstName("Василий");
        CORRECT_USER1.setSecondName("Пупкин");
        CORRECT_USER1.setMiddleName(null);
        CORRECT_USER1.setMobilePhone("+7911-908-07-06");
        CORRECT_USER1.setEmail("pupkin@ya.ru");
        CORRECT_USER1.setPosition("Исполняющий обязанности");
        CORRECT_USER1.setRights(Collections.emptyList());
        CORRECT_USER1.setOffice("1236757567");
        CORRECT_USER1.setBranch("3453547578");
        CORRECT_USER1.setPersonnelNumber("4569959949056");
    }

    private static final UserData CORRECT_USER2 = new UserData();

    static {
        OrgUnitData data = new OrgUnitData();
        data.setOffice("565634568");
        data.setBranch("43567757");
        data.setId(123L);
        CORRECT_USER1.setId(123L);
        CORRECT_USER1.setLogin(CORRECT_LOGIN2);
        CORRECT_USER1.setFirstName("Иван");
        CORRECT_USER1.setSecondName("Иванов");
        CORRECT_USER1.setMiddleName("Иванович");
        CORRECT_USER1.setMobilePhone("+7921-908-07-06");
        CORRECT_USER1.setEmail("alvanov@ya.ru");
        CORRECT_USER1.setPosition("Труженик");
        CORRECT_USER1.setRights(Collections.emptyList());
        CORRECT_USER1.setOffice("565634568");
        CORRECT_USER1.setBranch("43567757");
        CORRECT_USER1.setOffices(Collections.singletonList(data));
        CORRECT_USER1.setPersonnelNumber("3333766573563");
    }

    @InjectMocks
    private AuthenticationService authService;

    @Mock
    private SessionsManageAuthServiceClient authServiceClient;

    @Mock
    private TimeoutConfiguration timeoutConfiguration;

    @Spy
    private LocalDataStorage storage = new SimpleLocalDataStorage();

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(timeoutConfiguration.getTimeout()).thenReturn(5 * 1000);

        when(authServiceClient.postSession(eq(CORRECT_LOGIN1_CORRECT_PASSWORD), anyLong()))
                .thenReturn(new CreateSessionRs("SESSION1", CORRECT_USER1));
        when(authServiceClient.postSession(eq(CORRECT_LOGIN2_CORRECT_PASSWORD), anyLong()))
                .thenReturn(new CreateSessionRs("SESSION2", CORRECT_USER2));
        when(authServiceClient.postSession(eq(CORRECT_LOGIN1_INCORRECT_PASSWORD), anyLong())).thenThrow(new UserAuthenticationException());
        when(authServiceClient.postSession(eq(CORRECT_LOGIN2_INCORRECT_PASSWORD), anyLong())).thenThrow(new UserAuthenticationException());
        when(authServiceClient.postSession(eq(EXCEPTION_CREDENTIALS), anyLong())).thenThrow(new RuntimeException());
        when(authServiceClient.postSession(eq(INCORRECT_LOGIN_CORRECT_PASSWORD), anyLong())).thenThrow(new UserIdentificationException());
        when(authServiceClient.postSession(eq(BLOCKED_LOGIN_CREDENTIALS), anyLong())).thenThrow(new LoginBlockException());
        when(authServiceClient.postSession(eq(INVALID_OFFICE_LOGIN_CREDENTIALS), anyLong())).thenThrow(new UserOfficeNotFoundException());
        when(authServiceClient.postSession(eq(INVALID_ROLE_LOGIN_CREDENTIALS), anyLong())).thenThrow(new UserRoleNotFoundException());
        when(authServiceClient.postSession(eq(EMPTY_ROLE_LOGIN_CREDENTIALS), anyLong())).thenThrow(new UserWithoutRoleException());
    }

    @Test
    public void whenCorrectAuthenticationData_ShouldReturnUser() throws Exception {
        UserData user = authService.authenticate(CORRECT_LOGIN1, CORRECT_PASSWORD);
        assertSame(user, CORRECT_USER1);
    }

    @Test
    public void whenIncorrectPassword_ShouldThrowAuthenticationException() throws Exception {
        exception.expect(AuthenticationException.class);
        try {
            authService.authenticate(CORRECT_LOGIN1, INCORRECT_PASSWORD);
            fail("Ожидается AuthenticationException");
        } finally {
            assertNull(authService.getUser());
            assertFalse(authService.isUserAuthenticated());
        }
    }

    @Test
    public void whenIncorrectLogin_ShouldThrowAuthenticationException() throws Exception {
        exception.expect(AuthenticationException.class);
        try {
            authService.authenticate(INCORRECT_LOGIN, CORRECT_PASSWORD);
            fail("Ожидается AuthenticationException");
        } finally {
            assertFalse(authService.isUserAuthenticated());
            assertNull(authService.getUser());
        }
    }

    @Test
    public void whenLoginException_ShouldThrowException() throws Exception {
        exception.expect(Exception.class);
        try {
            authService.authenticate(EXCEPTION_LOGIN, CORRECT_PASSWORD);
            fail("Ожидается Exception");
        } finally {
            assertNull(authService.getUser());
            assertFalse(authService.isUserAuthenticated());
        }
    }

    @Test
    public void whenLoginBlocked_ShouldThrowException() throws Exception {
        testError(BLOCKED_LOGIN_CREDENTIALS, LoginBlockException.class);
    }

    @Test
    public void whenLoginOfficeNotFound_ShouldThrowException() throws Exception {
        testError(INVALID_OFFICE_LOGIN_CREDENTIALS, UserOfficeNotFoundException.class);
    }

    @Test
    public void whenLoginRoleNotFound_ShouldThrowException() throws Exception {
        testError(INVALID_ROLE_LOGIN_CREDENTIALS, UserRoleNotFoundException.class);
    }

    @Test
    public void whenLoginEmptyRole_ShouldThrowException() throws Exception {
        testError(EMPTY_ROLE_LOGIN_CREDENTIALS, UserWithoutRoleException.class);
    }

    private void testError(CreateSessionRq createSessionRq, Class<? extends Exception> expected) throws Exception {
        exception.expect(expected);
        try {
            authService.authenticate(createSessionRq.getLogin(), createSessionRq.getPassword());
            fail("Ожидается Exception");
        } finally {
            assertNull(authService.getUser());
            assertFalse(authService.isUserAuthenticated());
        }
    }

    @Test
    public void isUserAuthenticatedWithoutAuthenticate_ShouldReturnFalse() throws Exception {
        assertFalse(authService.isUserAuthenticated());
    }

    @Test
    public void userWithoutAuthenticate_ShouldReturnNull() throws Exception {
        assertNull(authService.getUser());
    }

    @Test
    public void userAfterAuthenticate_ShouldReturnUser() throws Exception {
        authService.authenticate(CORRECT_LOGIN1, CORRECT_PASSWORD);

        UserData user = authService.getUser();
        assertSame(user, CORRECT_USER1);
    }

    @Test
    public void isUserAfterAuthenticate_ShouldReturnTrue() throws Exception {
        authService.authenticate(CORRECT_LOGIN1, CORRECT_PASSWORD);
        assertTrue(authService.isUserAuthenticated());
    }

    @Test
    public void authenticate_logout_user_ShouldReturnNull() throws Exception {
        authService.authenticate(CORRECT_LOGIN1, CORRECT_PASSWORD);
        assertSame(authService.getUser(), CORRECT_USER1);
        assertTrue(authService.isUserAuthenticated());

        authService.logout();

        assertNull(authService.getUser());
        assertFalse(authService.isUserAuthenticated());
    }

    @Test
    public void whenFewAuthenticate_ShouldReturnLastUser() throws Exception {
        authService.authenticate(CORRECT_LOGIN1, CORRECT_PASSWORD);
        assertSame(authService.getUser(), CORRECT_USER1);
        assertTrue(authService.isUserAuthenticated());

        authService.authenticate(CORRECT_LOGIN2, CORRECT_PASSWORD);
        assertSame(authService.getUser(), CORRECT_USER2);
        assertTrue(authService.isUserAuthenticated());
    }

    @Test
    public void whenCloseSessionFailed_ShouldClearUser() throws Exception {
        doThrow(new RuntimeException()).when(authServiceClient).deleteSession(anyString(), anyLong());

        authService.authenticate(CORRECT_LOGIN1, CORRECT_PASSWORD);
        assertSame(authService.getUser(), CORRECT_USER1);
        assertTrue(authService.isUserAuthenticated());

        exception.expect(RuntimeException.class);
        try {
            authService.logout();
            fail("Ожидается RuntimeException");
        } finally {
            assertNull(authService.getUser());
            assertFalse(authService.isUserAuthenticated());
        }
    }

}
