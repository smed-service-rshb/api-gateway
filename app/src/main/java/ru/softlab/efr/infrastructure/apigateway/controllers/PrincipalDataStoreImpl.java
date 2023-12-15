package ru.softlab.efr.infrastructure.apigateway.controllers;

import org.springframework.context.ApplicationContext;
import ru.softlab.efr.infrastructure.apigateway.services.auth.AuthenticationService;
import ru.softlab.efr.services.auth.UserData;
import ru.softlab.efr.services.auth.exchange.model.OfficeData;
import ru.softlab.efr.services.auth.exchange.model.OrgUnitData;
import ru.softlab.efr.services.authorization.PrincipalData;
import ru.softlab.efr.services.authorization.PrincipalDataImpl;
import ru.softlab.efr.services.authorization.PrincipalDataStore;

import java.util.stream.Collectors;

/**
 * Реализция хранилища данных пользователя
 *
 * @author niculichev
 * @since 07.06.2017
 */
public class PrincipalDataStoreImpl implements PrincipalDataStore {
    private ApplicationContext applicationContext;

    /**
     * Конструктор
     *
     * @param applicationContext контест приложения
     */
    public PrincipalDataStoreImpl(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setPrincipalData(PrincipalData principalData) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearPrincipalData() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PrincipalData getPrincipalData() {
        AuthenticationService authenticationService = applicationContext.getBean(AuthenticationService.class);
        UserData userData = authenticationService.getUser();
        if (userData == null) {
            return null;
        }

        PrincipalDataImpl principalData = new PrincipalDataImpl();
        principalData.setId(userData.getId());
        principalData.setFirstName(userData.getFirstName());
        principalData.setSecondName(userData.getSecondName());
        principalData.setMiddleName(userData.getMiddleName());
        principalData.setRights(userData.getRights());
        principalData.setOffice(userData.getOffice());
        principalData.setOfficeId(userData.getOrgUnitId());
        principalData.setBranch(userData.getBranch());
        principalData.setMobilePhone(userData.getMobilePhone());
        principalData.setPersonnelNumber(userData.getPersonnelNumber());
        principalData.setOffices(userData.getOffices().stream().map(OrgUnitData::getOffice).collect(Collectors.toList()));
        principalData.setGroups(userData.getGroupCodes());
        return principalData;
    }
}
