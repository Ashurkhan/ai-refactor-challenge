# Legacy Order Service

Учебный Spring Boot проект с намеренно оставленными проблемами для практики рефакторинга.

## Требования
- Java 17
- Maven 3.6+ (или Maven Wrapper, если добавить его отдельно)

## Запуск
Из папки проекта:
```bash
mvn spring-boot:run
```
Сервис: http://localhost:8080  
H2 Console: http://localhost:8080/h2-console  
JDBC URL для консоли: `jdbc:h2:mem:legacydb`, пользователь `sa`, пароль пустой.

## Примеры запросов

Создать заказ:
```http
POST http://localhost:8080/api/orders
Content-Type: application/json

{
  "customerName": "Aida",
  "productName": "Keyboard",
  "price": 2500,
  "quantity": 2
}
```

Получить все заказы:
```http
GET http://localhost:8080/api/orders
```

Поиск:
```http
GET http://localhost:8080/api/orders/search?q=Key
```

Стоимость заказа:
```http
GET http://localhost:8080/api/orders/1/total
```

Дорогие товары:
```http
GET http://localhost:8080/api/orders/expensive?price=1000
```

## Намеренно оставленные проблемы
1. Нет Bean Validation: отрицательные цена и количество принимаются.
2. Плохие имена переменных: `x`, `a`, `b`, `d`, `tmp`.
3. Повторяется вычисление `price * quantity`.
4. Нет тестов.
5. `/search` и `/expensive` вызывают `findAll()` и фильтруют всё в памяти.
6. Используется `double` для денег вместо `BigDecimal`.
7. `findById(id).get()` может выбросить исключение, если заказа нет.
8. Контроллер одновременно отвечает за HTTP, поиск, вычисления и формат ответа.
9. Нет пагинации и ограничений на размер входных данных.

Это намеренно небезопасный учебный пример. Не используй его в production.

## Идеи для задания AI
- Добавить DTO и Bean Validation.
- Перенести бизнес-логику в сервисный слой.
- Использовать `BigDecimal` для денежных сумм.
- Перенести поиск в запросы репозитория и добавить пагинацию.
- Обрабатывать отсутствие заказа через корректный HTTP 404.
- Добавить unit- и интеграционные тесты.
- Переименовать переменные и убрать дублирование.
