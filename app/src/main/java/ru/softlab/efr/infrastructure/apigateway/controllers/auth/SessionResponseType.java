package ru.softlab.efr.infrastructure.apigateway.controllers.auth;

import ru.softlab.efr.services.auth.Right;

import java.util.Collection;

/**
 * Тип ответа с информацией о текущей сесии
 */
public class SessionResponseType {
    private UserInfo user;
    private Collection<Right> rights;

    public SessionResponseType() {
    }

    /**
     * @param user  информация о пользователе
     * @param rights перечень разрешений пользователя в разрезе ролей
     */
    SessionResponseType(UserInfo user, Collection<Right> rights) {

        this.user = user;
        this.rights = rights;
    }

    /**
     * @return информация о пользователе
     */
    public UserInfo getUser() {
        return user;
    }

    /**
     * @return перечень прав пользователя
     */
    public Collection<Right> getRights() {
        return rights;
    }

}
