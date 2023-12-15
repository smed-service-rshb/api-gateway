#!/bin/bash

test_exit_code=0
# Запускаем контейнер интеграционных тестов (с ожидание ответа от ключевых систем), результат выполнения интеграционных тестов будет сохранен в директорию билда
# Тест ru.softlab.efr.infrastructure.apigateway.ApiGatewayIntegrationTest
docker-compose -f docker-compose.test.yml -f docker-compose.integration-tests.yml run -u $(id -u) integration-test \
        ../wait-for-it.sh api-gateway-app:8080 -s -t 100 -- \
        ../wait-for-it.sh auth-service-app:7001 -s -t 100 -- \
        ../wait-for-it.sh some-service-app:8080 -s -t 100 -- \
        mvn surefire:test -Dtest="*IntegrationTest*,!*Without*" -DfailIfNoTests=false
if [ $? -ne 0 ]; then test_exit_code=1 ; fi

# Тест ru.softlab.efr.infrastructure.apigateway.WithoutAuthServiceIntegrationTest
docker-compose -f docker-compose.test.yml pause auth-service-app
docker-compose -f docker-compose.test.yml -f docker-compose.integration-tests.yml run -u $(id -u) integration-test \
        mvn surefire:test -Dtest=ru.softlab.efr.infrastructure.apigateway.WithoutAuthServiceIntegrationTest -DfailIfNoTests=false
if [ $? -ne 0 ]; then test_exit_code=1 ; fi

# Тест ru.softlab.efr.infrastructure.apigateway.WithoutSomeServiceIntegrationTest
docker-compose unpause auth-service-app
docker-compose pause some-service-app
docker-compose -f docker-compose.test.yml -f docker-compose.integration-tests.yml run -u $(id -u) integration-test \
        ../wait-for-it.sh auth-service-app:7001 -s -t 100 -- \
        mvn surefire:test -Dtest=ru.softlab.efr.infrastructure.apigateway.WithoutSomeServiceIntegrationTest -DfailIfNoTests=false
if [ $? -ne 0 ]; then test_exit_code=1 ; fi

# Тест ru.softlab.efr.infrastructure.apigateway.WithoutBrokerIntegrationTest
docker-compose unpause some-service-app
docker-compose pause efr-mq
docker-compose -f docker-compose.test.yml -f docker-compose.integration-tests.yml run -u $(id -u) integration-test \
        ../wait-for-it.sh some-service-app:8080 -s -t 100 -- \
        mvn surefire:test -Dtest=ru.softlab.efr.infrastructure.apigateway.WithoutBrokerIntegrationTest -DfailIfNoTests=false
if [ $? -ne 0 ]; then test_exit_code=1 ; fi

exit "$test_exit_code"