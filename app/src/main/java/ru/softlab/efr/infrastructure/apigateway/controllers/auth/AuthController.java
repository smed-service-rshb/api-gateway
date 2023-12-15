package ru.softlab.efr.infrastructure.apigateway.controllers.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.softlab.efr.infrastructure.apigateway.controllers.ErrorResponseType;
import ru.softlab.efr.infrastructure.apigateway.services.auth.AuthenticationException;
import ru.softlab.efr.infrastructure.apigateway.services.auth.AuthenticationService;
import ru.softlab.efr.services.auth.UserData;
import ru.softlab.efr.services.auth.exceptions.*;
import ru.softlab.efr.services.auth.exchange.BadEntityRs;
import ru.softlab.efr.services.auth.exchange.model.OfficeData;
import ru.softlab.efr.services.auth.exchange.model.OrgUnitData;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.stream.Collectors;

/**
 * Контролер аутентификации
 */
@RestController
@RequestMapping("/auth/v1")
public class AuthController {
    private final AuthenticationService authenticationService;

    /**
     * Конструктор
     *
     * @param authenticationService сервис аутентификации
     */
    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }


    /**
     * Аутентифкация
     *
     * @param loginRequest       десериализованный запрос на аутентификацию
     * @param httpServletRequest сырой http-запрос
     * @return результат аутентификации
     * @throws AuthenticationException в случае ошибок
     * @throws AuthServiceException    ошибка аутентификации сотрудника
     */
    @PostMapping(value = "/login")
    public ResponseEntity<?> login(@Validated @RequestBody LoginRequestType loginRequest, HttpServletRequest httpServletRequest) throws AuthenticationException, AuthServiceException {
        if (authenticationService.isUserAuthenticated() && loginRequest.getOfficeId() != null) {
            UserData user = updateUserData(authenticationService.getUser(), loginRequest.getOfficeId());
            authenticationService.updateSession(user.getOffice(), user.getBranch());
            return createSessionInfoResponse(user);
        }
        logout(httpServletRequest);
        UserData user = authenticationService.authenticate(loginRequest.getLogin(), loginRequest.getPasswd());
        return createSessionInfoResponse(user);
    }

    /**
     * Закрытие сессии
     *
     * @param httpServletRequest сырой http-запрос
     * @return результат закрытия сессии
     */
    @PostMapping(value = "/logout")
    public ResponseEntity<?> logout(HttpServletRequest httpServletRequest) {
        authenticationService.logout();
        invalidateSession(httpServletRequest);
        return ResponseEntity.ok().build();
    }

    /**
     * Получение информации о текущей сесиии
     *
     * @return информация о текущем пользовате или 401 для неаутентифицированного пользоватля
     */

    @GetMapping(value = "/session")
    public ResponseEntity<?> session() {
        UserData user = authenticationService.getUser();
        if (user == null || Boolean.TRUE.equals(user.getChangePassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return createSessionInfoResponse(user);
    }

    private static void invalidateSession(HttpServletRequest httpServletRequest) {
        if (httpServletRequest == null) {
            return;
        }
        HttpSession session = httpServletRequest.getSession(false);
        if (session == null || session.isNew()) {
            return;
        }
        session.invalidate();
    }

    @PostMapping(value = "/changePassword")
    public ResponseEntity<?> changePassword(@Validated @RequestBody ChangePasswordType data) throws AuthenticationException, AuthServiceException {
        authenticationService.changePassword(data.getLogin(), data.getOldPassword(), data.getNewPassword());
        UserData user = authenticationService.getUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        user.setChangePassword(false);
        authenticationService.updateUser(user);
        return createSessionInfoResponse(user);
    }

    private ResponseEntity<SessionResponseType> createSessionInfoResponse(UserData user) {
        UserInfo userInfo = new UserInfo(user);
        if (user.getOffices() != null) {
            userInfo.setOffices(user.getOffices().stream().map(officeData -> {
                Office office = new Office();
                office.setOfficeId(officeData.getId());
                office.setOfficeName(officeData.getOffice());
                return office;
            }).collect(Collectors.toList()));
        }
        return ResponseEntity.ok().body(new SessionResponseType(userInfo,
                (Boolean.TRUE.equals(user.getChangePassword()) || userInfo.getOffice() == null)
                        ? null : user.getRights()));
    }

    private UserData updateUserData(UserData user, Long currentOfficeId) {
        if (currentOfficeId != null) {
            OrgUnitData currentOffice = user.getOffices().stream()
                    .filter(officeData -> officeData.getId().equals(currentOfficeId))
                    .findFirst().orElse(null);
            if (currentOffice != null) {
                user.setOrgUnitId(currentOfficeId);
                user.setOffice(currentOffice.getOffice());
                user.setBranch(currentOffice.getBranch());
                authenticationService.updateUser(user);
            }
        }
        return user;
    }

    @ExceptionHandler
    ResponseEntity handleException(final AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseType(e.getErrorCode(), e.getMessage()));
    }

    @ExceptionHandler
    ResponseEntity handleException(final LoginBlockException e) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(BadEntityRs.userBlockedResponse(e));
    }

    @ExceptionHandler
    ResponseEntity handleException(final UserOfficeNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(BadEntityRs.userOfficeNotFoundResponse(e));
    }

    @ExceptionHandler
    ResponseEntity handleException(final UserRoleNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(BadEntityRs.userRoleNotFoundExceptionResponse(e));
    }

    @ExceptionHandler
    ResponseEntity handleException(final UserWithoutRoleException e) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(BadEntityRs.userWithoutRoleExceptionResponse(e));
    }
}
