package ru.softlab.efr.infrastructure.apigateway.controllers.auth;

/**
 * Контейнер информации о офисе пользователе
 */
public class Office {

    private Long officeId;

    private String officeName;

    public Office() {
    }

    /**
     * @param office данные по офису
     */
    public Office(Office office) {
        this.officeId = office.getOfficeId();
        this.officeName = office.getOfficeName();
    }

    /**
     * @return идентификатор офиса
     */
    public Long getOfficeId() {
        return officeId;
    }

    public void setOfficeId(Long officeId) {
        this.officeId = officeId;
    }

    /**
     * @return наименование офиса
     */
    public String getOfficeName() {
        return officeName;
    }

    public void setOfficeName(String officeName) {
        this.officeName = officeName;
    }
}
