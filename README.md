# Система анализа результатов тестирования

XAI Observer App - это веб-приложение на базе Spring Boot, предназначенное для сбора, визуализации и интеллектуального анализа результатов автоматизированных тестов. Оно помогает командам по обеспечению качества и разработчикам быстро идентифицировать причины сбоев тестов, отслеживать метрики качества и улучшать стабильность релизов.

## Ключевые возможности
*   **Централизованный сбор результатов:** Получение данных о выполнении тестов (успешно/провалено) по REST API от CI/CD систем или тестовых фреймворков.
*   **Интерактивная API-документация:** Полностью документированный REST API с помощью **Swagger UI**, позволяющий изучать и тестировать эндпоинты прямо из браузера.
*   **Автоматический анализ причин (RCA):** Интеллектуальный анализ проваленных тестов с использованием цепочки анализаторов для предложения наиболее вероятных причин сбоев и возможных решений.
*   **Детальная информация о запусках:** Просмотр подробностей каждого запуска, включая стек-трейсы, артефакты (логи, скриншоты), детали окружения и путь выполнения.
*   **Визуализация пути выполнения:** Наглядное отображение последовательности действий, выполненных тестом, с указанием шага, на котором произошёл сбой.
*   **Статистика и тренды:**
    *   Общая статистика по количеству пройденных/проваленных/пропущенных тестов.
    *   Динамика Pass Rate.
    *   Топ нестабильных и самых медленных тестов.
*   **Обратная связь по анализу:** Возможность для пользователей оценивать корректность предложенного AI-анализа для будущего дообучения моделей.
*   **Управление конфигурациями:** Отслеживание результатов по различным версиям приложений, средам и тестовым наборам.

## Технологический стек

#### Backend
*   **Фреймворк:** Spring Boot 3
*   **Доступ к данным:** Spring Data JPA / Hibernate
*   **База данных:** PostgreSQL / H2 Database (для разработки)
*   **Миграции БД:** Liquibase
*   **Документация API:** Springdoc OpenAPI (Swagger 3)
*   **Утилиты:** Lombok, MapStruct

#### Frontend
*   **Шаблонизатор:** Thymeleaf
*   **Стили:** CSS3, Font Awesome
*   **Скрипты:** Vanilla JavaScript (ES6+)
*   **Графики:** Chart.js

## Руководство по запуску

### Требования
*   **Java Development Kit (JDK) 21** или выше.
*   **Apache Maven 3.8.x** или выше.

### Установка и запуск

1.  **Склонируйте репозиторий:**
    ```bash
    git clone https://github.com/svedentsov/XAITestObserve.git
    cd XAITestObserve
    ```

2.  **Соберите проект с помощью Maven:**
    Эта команда скачает все зависимости, скомпилирует код и выполнит тесты.
    ```bash
    mvn clean install
    ```

3.  **Запустите приложение:**
    ```bash
    mvn spring-boot:run
    ```
    Или, если вы собрали исполняемый JAR-файл:
    ```bash
    java -jar target/xaiobserverapp-0.0.1-SNAPSHOT.jar
    ```
После запуска приложение будет доступно по адресу `http://localhost:8080`.

## Основные эндпоинты и интерфейсы

*   **Главный дашборд:** `http://localhost:8080/`
    *   Отображает список последних тестовых запусков и виджеты со статистикой.

*   **Документация API (Swagger UI):** `http://localhost:8080/swagger-ui.html`
    *   Интерактивная документация для всех REST-эндпоинтов. **Рекомендуется начать знакомство с API отсюда.**

*   **API-спецификация (JSON):** `http://localhost:8080/v3/api-docs`
    *   Машиночитаемая спецификация API в формате OpenAPI 3.

*   **Консоль H2 Database:** `http://localhost:8080/h2-console`
    *   **JDBC URL:** `jdbc:h2:file:./data/xaiobserver`
    *   **User Name:** `sa`
    *   **Password:** (оставьте пустым)

## Пример использования API

Вы можете отправить событие о завершении теста, используя `curl` или любой HTTP-клиент.

```bash
curl -X POST \
  http://localhost:8080/api/v1/events/test-finished \
  -H 'Content-Type: application/json' \
  -d '{
    "testRunId": "a1b2c3d4-e5f6-7890-a1b2-c3d4e5f67890",
    "testClass": "com.example.tests.CheckoutTests",
    "testMethod": "testGuestCheckoutWithInvalidEmail",
    "startTime": 1719907200000,
    "endTime": 1719907235000,
    "durationMillis": 35000,
    "status": "FAILED",
    "exceptionType": "org.openqa.selenium.TimeoutException",
    "exceptionMessage": "Expected condition failed: waiting for visibility of element located by By.cssSelector: .order-summary (tried for 30 second(s) with 500 milliseconds interval)",
    "stackTrace": "org.openqa.selenium.TimeoutException: ...\n\tat com.example.tests.CheckoutTests.testGuestCheckoutWithInvalidEmail(CheckoutTests.java:75)\n\t...",
    "failedStep": {
      "stepNumber": 5,
      "action": "Ожидание появления элемента .order-summary",
      "locatorStrategy": "css",
      "locatorValue": ".order-summary",
      "confidenceScore": 0.9,
      "result": "FAILURE",
      "errorMessage": "Элемент не появился за 30 секунд."
    },
    "executionPath": [
      {"action": "Переход на страницу товара", "result": "SUCCESS"},
      {"action": "Добавление товара в корзину", "result": "SUCCESS"},
      {"action": "Переход в корзину", "result": "SUCCESS"},
      {"action": "Ввод невалидного email", "result": "SUCCESS"},
      {"action": "Ожидание появления элемента .order-summary", "result": "FAILURE"}
    ],
    "appVersion": "2.3.1",
    "testSuite": "Regression",
    "testTags": ["checkout", "guest", "P1", "flaky"],
    "environmentDetails": {
      "name": "QA",
      "osType": "Linux",
      "browserType": "Chrome",
      "browserVersion": "126.0"
    },
    "artifacts": {
      "screenshotUrls": ["http://artifacts.example.com/run123/failure.png"],
      "videoUrl": "http://artifacts.example.com/run123/test_video.mp4"
    },
    "customMetadata": {
      "jiraTicket": "PROJ-789",
      "buildNumber": "build-452",
      "jenkinsJobUrl": "http://jenkins.example.com/job/Regression/452"
    }
  }
