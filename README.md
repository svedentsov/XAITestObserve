# XAI Observer App: Система Анализа Результатов Тестирования

XAI Observer App - это веб-приложение, предназначенное для сбора, визуализации и интеллектуального анализа результатов автоматизированных тестов. Оно помогает командам по обеспечению качества и разработчикам быстро идентифицировать причины сбоев тестов, отслеживать метрики качества и улучшать стабильность релизов.

## Возможности

* **Приём событий о завершении тестов:** Получение данных о выполнении тестов (успешно/провалено) от CI/CD-систем или тестовых фреймворков.
* **Детальная информация о тестовых запусках:** Просмотр подробностей каждого запуска, включая стек-трейсы, типы исключений и шаги выполнения.
* **Автоматический анализ причин (RCA):** Интеллектуальный анализ проваленных тестов для предложения наиболее вероятных причин сбоев и возможных решений.
* **Визуализация пути выполнения:** Отображение последовательности действий, выполненных тестом, с указанием шага, на котором произошёл сбой.
* **Статистика и тренды:**
    * Общая статистика по количеству пройденных/проваленных/пропущенных тестов.
    * Процент прохождения тестов.
    * Топ-10 самых часто падающих тестов.
    * Ежедневный тренд процента прохождения.
* **Обратная связь по анализу:** Возможность для пользователей оценивать корректность предложенного AI-анализа, что способствует улучшению модели.
* **Управление конфигурациями:** Отслеживание результатов тестов по различным версиям приложений, средам и тестовым наборам.
* **Уведомления о сбоях:** Базовая система уведомлений о проваленных тестах.

## Технологии

Проект построен на стеке Spring Boot и использует современные веб-технологии:
* **Backend:** Spring Boot, Spring Data JPA, Lombok
* **База данных:** H2 Database (для разработки и тестирования)
* **ORM:** Hibernate
* **Веб-интерфейс:** Thymeleaf, HTML, CSS
* **Логирование:** SLF4J / Logback

## Запуск приложения

Для запуска приложения убедитесь, что у вас установлен **Java Development Kit (JDK) 17** или выше и **Maven 3.x**.

1.  **Склонируйте репозиторий:**
    ```bash
    git clone [ссылка_на_ваш_репозиторий]
    cd XaiObserverApp
    ```

2.  **Соберите проект с помощью Maven:**
    ```bash
    mvn clean install
    ```

3.  **Запустите приложение Spring Boot:**
    ```bash
    mvn spring-boot:run
    ```

    Или, если вы собрали исполняемый JAR:
    ```bash
    java -jar target/xaiobserverapp-0.0.1-SNAPSHOT.jar
    ```

После запуска приложение будет доступно по адресу `http://localhost:8080/`.

## Точки доступа и интерфейсы

* **Главный дашборд:** `http://localhost:8080/`
    * Отображает список последних тестовых запусков и общую статистику.
* **Детали тестового запуска:** `http://localhost:8080/test/{id}`
    * Подробная страница для каждого тестового запуска.
* **API для отправки событий:** `POST http://localhost:8080/api/v1/events/test-finished`
    * Принимает JSON-тело типа `FailureEventDTO` для регистрации завершенных тестов.
* **API для получения статистики:** `GET http://localhost:8080/api/v1/statistics`
    * Возвращает JSON с агрегированной статистикой.
* **API для обратной связи:** `POST http://localhost:8080/api/v1/analysis/{analysisId}/feedback`
    * Позволяет отправить фидбек по конкретному результату анализа.
* **Консоль H2 (для просмотра данных):** `http://localhost:8080/h2-console`
    * Используйте `jdbc:h2:file:./data/xaiobserver` в качестве JDBC URL.

## Пример использования API (событие о сбое теста)

Вы можете отправить данные о проваленном тесте, используя `curl` или любой HTTP-клиент:

```bash
curl -X POST \
  http://localhost:8080/api/v1/events/test-finished \
  -H 'Content-Type: application/json' \
  -d '{
    "testRunId": "unique-test-run-id-1234",
    "testClass": "com.example.tests.LoginTests",
    "testMethod": "testLoginWithInvalidCredentials",
    "timestamp": 1719907200000,
    "status": "FAILED",
    "exceptionType": "java.lang.AssertionError: Ожидалось успешное сообщение, но получено сообщение об ошибке",
    "stackTrace": "Пример стектрейса...\n\tat com.example.tests.LoginTests.testLoginWithInvalidCredentials(LoginTests.java:50)\n\t...",
    "failedStep": {
      "action": "Проверка сообщения об ошибке",
      "locatorStrategy": "css",
      "locatorValue": ".error-message",
      "confidenceScore": 0.85,
      "result": "FAILURE"
    },
    "executionPath": [
      {"action": "Переход на страницу входа", "locatorStrategy": "url", "locatorValue": "/login", "confidenceScore": 0.99, "result": "SUCCESS"},
      {"action": "Ввод логина", "locatorStrategy": "id", "locatorValue": "username", "confidenceScore": 0.98, "result": "SUCCESS"},
      {"action": "Ввод пароля", "locatorStrategy": "name", "locatorValue": "password", "confidenceScore": 0.97, "result": "SUCCESS"},
      {"action": "Нажатие кнопки входа", "locatorStrategy": "xpath", "locatorValue": "//button[@type=\\"submit\\"]", "confidenceScore": 0.96, "result": "SUCCESS"},
      {"action": "Проверка сообщения об ошибке", "locatorStrategy": "css", "locatorValue": ".error-message", "confidenceScore": 0.85, "result": "FAILURE"}
    ],
    "appVersion": "1.0.0",
    "environment": "QA",
    "testSuite": "Smoke"
  }'
```
