package ru.softlab.efr.infrastructure.apigateway.services.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.softlab.efr.infrastructure.apigateway.InternalErrorException;
import ru.softlab.efr.infrastructure.apigateway.ServiceUnavailableException;
import ru.softlab.efr.infrastructure.apigateway.TimeoutConfiguration;
import ru.softlab.efr.infrastructure.apigateway.services.LocalDataStorage;
import ru.softlab.efr.services.auth.AuthServiceInfo;
import ru.softlab.efr.services.auth.SessionsManageAuthServiceClient;
import ru.softlab.efr.services.auth.UserData;
import ru.softlab.efr.services.auth.exceptions.AuthServiceException;
import ru.softlab.efr.services.auth.exceptions.UserAuthenticationException;
import ru.softlab.efr.services.auth.exceptions.UserIdentificationException;
import ru.softlab.efr.services.auth.exchange.ChangePasswordRq;
import ru.softlab.efr.services.auth.exchange.CreateSessionRq;
import ru.softlab.efr.services.auth.exchange.CreateSessionRs;
import ru.softlab.efr.services.auth.exchange.model.OrgUnitData;

import java.util.concurrent.TimeoutException;

/**
 * Сервис аутентификации.
 * Непосредственно аутенификация производится через клиента auth-service
 * В задачи данного сервиса входит поддрежание сесиии в хранилище данных
 */
@Service
public class AuthenticationService {
    private static final String USER_STORAGE_ATTRIBUTE_NAME = AuthenticationService.class.getName() + "_USER_ATTRIBUTE";
    private static final String SESSION_STORAGE_ATTRIBUTE_NAME = AuthenticationService.class.getName() + "_SESSION_ATTRIBUTE";
    private final SessionsManageAuthServiceClient authServiceClient;
    private final LocalDataStorage storage;
    private final TimeoutConfiguration timeoutConfiguration;

    /**
     * @param authServiceClient    клиент сервиса аутентификации по работе с сессиями
     * @param timeoutConfiguration настройки таймаутов
     * @param storage              хранилище данных
     */
    @Autowired
    public AuthenticationService(SessionsManageAuthServiceClient authServiceClient, TimeoutConfiguration timeoutConfiguration, LocalDataStorage storage) {
        this.authServiceClient = authServiceClient;
        this.timeoutConfiguration = timeoutConfiguration;
        this.storage = storage;
    }

    /**
     * Аутентифцировать пользователя и создать сесиию в хранилище
     *
     * @param login  логин
     * @param passwd пароль
     * @return Информация о пользователе в случае успешной аутентификации
     * @throws AuthenticationException в случае ошибок
     * @throws AuthServiceException    ошибка аутентификации сотрудника
     */
    public UserData authenticate(String login, String passwd) throws AuthenticationException, AuthServiceException {
        removeUser();

        CreateSessionRs loginResponse;
        try {
            loginResponse = authServiceClient.postSession(new CreateSessionRq(login, passwd), timeoutConfiguration.getTimeout());
        } catch (TimeoutException e) {
            throw new ServiceUnavailableException(AuthServiceInfo.SERVICE_NAME, e);
        } catch (UserIdentificationException | UserAuthenticationException e) {
            throw new AuthenticationFailedException(e);
        } catch (AuthServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalErrorException(e);
        }

        UserData user = loginResponse.getUser();
        if (user == null) {
            throw new InternalErrorException("Не заполнен user");
        }
        String sessionUUID = loginResponse.getId();
        if (sessionUUID == null) {
            throw new InternalErrorException("Не заполнен идентификатор сесссии");
        }
       if (user.getOffice() != null && user.getOrgUnitId() == null) {
           user.setOrgUnitId(user.getOffices().stream()
                   .filter(orgUnitData -> user.getOffice().equals(orgUnitData.getOffice()))
                   .map(OrgUnitData::getId).findFirst().orElse(null));
       }
        saveSessionUUID(sessionUUID);
        saveUser(user);
        return user;
    }

    public void changePassword(String login, String password, String newPassword) throws AuthenticationException, AuthServiceException {
        try {
            authServiceClient.changePassword(new ChangePasswordRq(login, password, newPassword), timeoutConfiguration.getTimeout());
        } catch (TimeoutException e) {
            throw new ServiceUnavailableException(AuthServiceInfo.SERVICE_NAME, e);
        } catch (UserAuthenticationException e) {
            throw new AuthenticationFailedException(e);
        } catch (AuthServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalErrorException(e);
        }
    }

    public void updateSession( String office, String branch) {
        try {
            authServiceClient.updateSession(getSessionUUID(), office, branch, timeoutConfiguration.getTimeout());
        } catch (TimeoutException e) {
            throw new ServiceUnavailableException(AuthServiceInfo.SERVICE_NAME, e);
        }  catch (Exception e) {
            throw new InternalErrorException(e);
        }
    }

    /**
     * закрытие сесиии
     */
    public void logout() {
        removeUser();
        String sessionUUID = getSessionUUID();
        if (StringUtils.isEmpty(sessionUUID)) {
            return;
        }
        removeSessionUUID();

        try {
            authServiceClient.deleteSession(sessionUUID, timeoutConfiguration.getTimeout());
        } catch (TimeoutException e) {
            throw new ServiceUnavailableException(AuthServiceInfo.SERVICE_NAME, e);
        } catch (Exception e) {
            throw new InternalErrorException(e);
        }
    }

    /**
     * @return данные аутентифицированного пользователя или null
     */
    public UserData getUser() {
        return (UserData) storage.getAttribute(USER_STORAGE_ATTRIBUTE_NAME);
    }

    /**
     * @return проверка аутентифицирован ли пользователь(есть ли информация о нем в хранилище)
     */
    public boolean isUserAuthenticated() {
        return getUser() != null;
    }

    /**
     * @return идентификатор сесиии, полученный от auth-service при аутентификации
     */
    public String getSessionUUID() {
        return (String) storage.getAttribute(SESSION_STORAGE_ATTRIBUTE_NAME);
    }

    private void saveUser(UserData user) {
        storage.setAttribute(USER_STORAGE_ATTRIBUTE_NAME, user);
    }

    public void updateUser(UserData user) {
        removeUser();
        storage.setAttribute(USER_STORAGE_ATTRIBUTE_NAME, user);
    }

    private void removeUser() {
        storage.removeAttribute(USER_STORAGE_ATTRIBUTE_NAME);
    }

    private void saveSessionUUID(String sessionUUID) {
        storage.setAttribute(SESSION_STORAGE_ATTRIBUTE_NAME, sessionUUID);
    }

    private void removeSessionUUID() {
        storage.removeAttribute(SESSION_STORAGE_ATTRIBUTE_NAME);
    }

}
