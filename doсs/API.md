# API

### Аутентифкация сотрудника банка
Адрес: `/login`  
Метод: `POST`  
Запрос: -  
Обязательные параметры:
* `login`: логин сотрудника банка
* `passwd`: пароль сотрудника банка

```json
{
    "login":"k34n12",
    "passwd":"12345"
}
```

Ответ:  
`200 OK`

```json
{
    "user": {
        "surname": "Иванов",
        "name": "Сергей",
        "middleName": "Александрович",
        "mobilePhone": "+79211111111",
        "email": "mail1@mail.ru",
        "position": "Главный специалист",
        "personnelNumber": "1230",
        "office": "0002",
        "branch": "0000"
    },
    "rights": [
        "ACCESS_CLIENTS_EXCEPT_MAIN_OFFICE",
        ...
        "ACCESS_CLIENTS_MAIN_OFFICE"
    ]
}
```

`401 Unauthorized`: пользователь не аутентифицирован   
`406 Not Acceptable`: ошибка состояния сотрудника
```javascript
response {
    type: 'BLOCKED',                        // тип ошибки (BLOCKED, OFFICE_NOT_FOUND, ROLE_NOT_FOUND, EMPTY_ROLE)
    message: 'Пользователь заблокировн'     // серверное сообщение
}
```
`500 Internal Server Error`: внутренняя ошибка сервиса   
`503 Service Unavailable`: сервис назначения не найден   

### Закрытие сессии сотрудника банка
Адрес: `/logout`  
Метод: `POST`  
Ответ:  
`200 OK`   
`500 Internal Server Error`: внутренняя ошибка сервиса   
`503 Service Unavailable`: сервис назначения не найден   

### Получение информации о текущей сесиии
Адрес: `/session`  
Метод: `GET`  
Ответ:  
`200 OK`   

```json
{
    "user": {
        "surname": "Иванов",
        "name": "Сергей",
        "middleName": "Александрович",
        "mobilePhone": "+79211111111",
        "email": "mail1@mail.ru",
        "position": "Главный специалист",
        "personnelNumber": "1230",
        "office": "0002",
        "branch": "0000"
    },
    "rights": [
        "ACCESS_CLIENTS_EXCEPT_MAIN_OFFICE",
        ...
        "ACCESS_CLIENTS_MAIN_OFFICE"
    ]
}
```

`401 Unauthorized`: пользователь не аутентифицирован   
`500 Internal Server Error`: внутренняя ошибка сервиса   
`503 Service Unavailable`: сервис назначения не найден   