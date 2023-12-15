package ru.softlab.efr.infrastructure.apigateway.controllers.auth;

import ru.softlab.efr.services.auth.UserData;

import java.util.List;

/**
 * Контейнер информации о пользователе
 */
public class UserInfo {
    private String surname;
    private String name;
    private String middleName;
    private String mobilePhone;
    private String email;
    private String position;
    private String personnelNumber;
    private String office;
    private String branch;
    List<Office> offices;
    private Boolean changePassword;
    private List<String> groupCodes;

    public UserInfo() {
    }

    /**
     * @param user данные о пользователе
     */
    UserInfo(UserData user) {
        surname = user.getSecondName();
        name = user.getFirstName();
        middleName = user.getMiddleName();
        mobilePhone = user.getMobilePhone();
        email = user.getEmail();
        position = user.getPosition();
        personnelNumber = user.getPersonnelNumber();
        office = user.getOffice();
        branch = user.getBranch();
        changePassword = user.getChangePassword();
        groupCodes = user.getGroupCodes();
    }

    /**
     * @return имя пользователя
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return фамилия пользователя
     */
    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    /**
     * @return отчество пользователя
     */
    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    /**
     * @return номер мобильного телефона пользователя
     */
    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    /**
     * @return электронная почта пользователя
     */
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return должность пользователя
     */
    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    /**
     * Получить офис
     *
     * @return офис
     */
    public String getOffice() {
        return office;
    }

    /**
     * Задать офис
     *
     * @param office офис
     */
    public void setOffice(String office) {
        this.office = office;
    }

    /**
     * Получить филиал
     *
     * @return филиал
     */
    public String getBranch() {
        return branch;
    }

    /**
     * Задать филилал
     *
     * @param branch филиал
     */
    public void setBranch(String branch) {
        this.branch = branch;
    }

    /**
     * Получить табельный номер
     *
     * @return табельный номер
     */
    public String getPersonnelNumber() {
        return personnelNumber;
    }

    /**
     * Задать табельный номер
     *
     * @param personnelNumber табельный номер
     */
    public void setPersonnelNumber(String personnelNumber) {
        this.personnelNumber = personnelNumber;
    }

    /**
     * Получить значение признака необходимости изменить пароль
     *
     * @return признак
     */
    public Boolean getChangePassword() {
        return changePassword;
    }

    /**
     * Задать признак необходимости изменить пароль
     *
     * @param changePassword признак
     */
    public void setChangePassword(Boolean changePassword) {
        this.changePassword = changePassword;
    }

    public List<Office> getOffices() {
        return offices;
    }

    public void setOffices(List<Office> offices) {
        this.offices = offices;
    }

    public List<String> getGroupCodes() {
        return groupCodes;
    }

    public void setGroupCodes(List<String> groupCodes) {
        this.groupCodes = groupCodes;
    }
}
