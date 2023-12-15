
package ru.softlab.efr.infrastructure.apigateway.controllers.auth;

import org.hibernate.validator.constraints.NotBlank;

/**
 * Контейнер запрос на аутентифкацию
 */
public class LoginRequestType {

    @NotBlank
    private String login;

    @NotBlank
    private String passwd;

    private Long officeId;
    /**
     * Конструктор
     */
    public LoginRequestType() {
    }

    /**
     * Конструктор
     *
     * @param login  логин
     * @param passwd пароль
     * @param officeId идентификатор офиса
     */
    public LoginRequestType(final String login, final String passwd, Long officeId) {
        this.login = login;
        this.passwd = passwd;
        this.officeId = officeId;
    }

    /**
     * Получить логин
     *
     * @return логин
     */
    public String getLogin() {
        return login;
    }

    /**
     * Задать логин
     *
     * @param value логин
     */
    public void setLogin(String value) {
        this.login = value;
    }

    /**
     * Получить пароль
     *
     * @return пароль
     */
    public String getPasswd() {
        return passwd;
    }

    /**
     * Задать пароль
     *
     * @param value пароль
     */
    public void setPasswd(String value) {
        this.passwd = value;
    }

    /**
     * Получить идентификатор офиса
     *
     * @return идентификатор офиса
     */
    public Long getOfficeId() {
        return officeId;
    }

    /**
     * Задать идентификатор офиса
     *
     * @param officeId идентификатор офиса
     */
    public void setOfficeId(Long officeId) {
        this.officeId = officeId;
    }
}
