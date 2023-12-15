# Docker

### Сборка образов
 
Для сборки pr (pre release) docker образов сервиса требуется выполнить 

```bash
    mvn clean package docker:build
```   

### Публикация образов в Docker Registry
 
Для публикации pr (pre release) docker образов сервиса требуется выполнить

```bash
    mvn docker:push
```

Для работы плагина (docker-maven-plugin) требуется установка [переменных среды](http://git.dos.softlab.ru/RSHB/EFR/communication-systems/wikis/configuring-workstation) 