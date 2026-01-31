# Микросервисы бронирования отелей (Spring Boot, каркас)

Этот репозиторий содержит много-модульный каркас платформы бронирования отелей на базе Spring Boot с микросервисной архитектурой.

## Сервисы (самостоятельные Spring Boot приложения с БД H2 в памяти):

- eureka-server - реестр сервисов (Spring Cloud Eureka), порт 8761
- api-gateway - API Gateway (Spring Cloud Gateway), порт 8080
- hotel-service - управление отелями и номерами, порт 8082
- booking-service - бронирования и простая демо‑аутентификация, порт 8081
- common-lib - общая библиотека (DTO/утилиты)

Заметки

- Проект — учебный каркас: реализованы базовые эндпойнты и взаимодействие.
- H2 работает в памяти с авто‑обновлением схемы; данные сбрасываются при перезапуске.

## Технологические версии

- Java 21
- Spring Boot 3.2.12
- Spring Cloud 2023.0.3 (совместимо с Boot 3.2.x)
- Maven 3.8.7+

## Запуск тестов

- Все модули:
    - `mvn test`

Примечания:
- По умолчанию Maven выводит краткий отчёт; ключ `-q` (quiet) может скрыть детали. Уберите `-q`, если хотите видеть прогресс/отчёт Surefire.
- В корневом POM настроен современный maven-surefire-plugin, поэтому тесты JUnit 5 обнаруживаются автоматически (классы, оканчивающиеся на `*Test`, `*Tests`, `*TestCase`).

## Сборка

```bash
mvn -q package
```

## Порядок запуска (в отдельных терминалах из корня проекта)

1) Eureka Server (8761):
```bash
mvn -pl eureka-server spring-boot:run
```

2) Hotel Service (8082):
```bash
mvn -pl hotel-service spring-boot:run
```

3) Booking Service (8081):
```bash
mvn -pl booking-service spring-boot:run
```

4) API Gateway (8080):
```bash
mvn -pl api-gateway spring-boot:run
```

## Интерфейсы

Сервис‑дискавери (UI)

- Панель Eureka: http://localhost:8761

Консоли H2 (dev)

- Booking: http://localhost:8081/h2-console (JDBC URL: `jdbc:h2:mem:bookingDb`)
- Hotel: http://localhost:8082/h2-console (JDBC URL: `jdbc:h2:mem:hotelDb`)

OpenAPI

- http://localhost:8081/swagger-ui.html
- http://localhost:8082/swagger-ui.html


## Ручная проверка работоспособности

Быстрый сценарий через гейтвей (:8080)

1) Регистрация пользователя (возвращается демо‑токен-заглушка):
```bash
curl -X POST http://localhost:8080/api/user/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"Madonna","password":"pwd"}'
```

2) Создание отеля:
```bash
curl -X POST http://localhost:8080/api/hotels \
  -H 'Content-Type: application/json' \
  -d '{"name":"Grand Hotel","address":"Main St"}'
```

3) Добавление номера (используйте id созданного отеля):
```bash
curl -X POST http://localhost:8080/api/rooms \
  -H 'Content-Type: application/json' \
  -d '{"hotelId":1, "number":"101"}'
```

4) Создание бронирования (упрощённая согласованность: booking-service вызывает hotel-service на подтверждение доступности):
```bash
curl -X POST http://localhost:8080/api/booking \
  -H 'Content-Type: application/json' \
  -d '{"username":"Madonna","roomId":1, "autoSelect":false}'
```

5) Список бронирований пользователя:
```bash
curl "http://localhost:8080/api/bookings?username=Madonna"
```

## Маршрутизация

GateWay:

- /api/bookings/**, /api/booking/**, /api/user/** → booking-service
- /api/hotels/**, /api/rooms/** → hotel-service

Эндпойнты Hotel Service

- GET /api/hotels — список отелей
- POST /api/hotels — создать отель
- GET /api/rooms — список доступных номеров
- GET /api/rooms/recommend — рекомендации (сортировка по `timesBooked`, затем по `id`)
- POST /api/rooms/{id}/confirm-availability — внутреннее подтверждение + временная блокировка
- POST /api/rooms/{id}/release — внутренняя компенсация (снятие блокировки)

Эндпойнты Booking Service

- POST /api/user/register — регистрация пользователя (демо‑токен)
- POST /api/user/auth — аутентификация (демо‑токен)
- POST /api/booking — создать бронирование (двухшаговая логика: PENDING → CONFIRMED/CANCELLED)
- GET /api/bookings — получить бронирования пользователя (через параметр `username` либо из Principal при реальной аутентификации)

## Поведение и валидация

- В бронировании поля дат обязательны; на текущем этапе сервис подставляет значения по умолчанию: `startDate=today`, `endDate=today+1`.
- При `autoSelect=false` поле `roomId` обязательно, иначе вернётся 400 (Bad Request).
- Ошибки уровня клиента (например, неизвестный пользователь) возвращают статус 400 с сообщением.

## Замечания по сборке/запуску

- В проекте включён флаг компилятора `-parameters` (maven-compiler-plugin), чтобы связывание параметров в аннотациях Spring (например, `@RequestParam("username")`, `@PathVariable("id")`) работало предсказуемо.
- Требуется Java 21+. Убедитесь, что `mvn -v` показывает Java 21+, иначе настройте `JAVA_HOME`.

## Устранение неполадок

1) Сообщение: "Name for argument ... Ensure that the compiler uses the '-parameters' flag"

- Включено глобально в родительском POM. Если ошибка повторяется, проверьте, что контроллеры указывают имя параметров явно: `@RequestParam(name="...")`, `@PathVariable("...")`.

2) Ошибка компиляции: "release version 17 not supported" или аналогичная

- Убедитесь, что Maven использует JDK 21: `mvn -v` должен показать Java 21+. Настройте `JAVA_HOME`/`PATH` при необходимости.
