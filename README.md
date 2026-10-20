# Legacy Order Service

A Spring Boot REST service for managing orders. The codebase is layered into Controller, Service, and Repository. It uses Jakarta Bean Validation, `BigDecimal` for money, SQL-backed search and filtering, pagination, and proper HTTP error responses.

## Requirements

- Java 17
- Maven 3.6+

## Run

From the project root:

```bash
mvn spring-boot:run
```

- Service: http://localhost:8080
- H2 Console: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:legacydb`
- Username: `sa`
- Password: empty

Tests:

```bash
mvn test
```

## Architecture

```
controller  — HTTP, request validation, pagination
service     — business logic (total calculation, CRUD)
repository  — Spring Data JPA queries
entity      — JPA Order model
dto         — request and response contracts
exception   — 404 / 400 (ProblemDetail)
```

Money fields `price` and `total` are `BigDecimal`. The order total is computed on the server as `price * quantity`.

## API

Base path: `/api/orders`.

Existing routes are preserved. List endpoints return a Spring Data `Page` object (`content`, `totalElements`, `totalPages`, `size`, `number`). Defaults: `page=0`, `size=20`, sort `id,asc`. Query params: `page`, `size`, `sort`.

### Create order

`POST /api/orders` — **201 Created**

```json
{
  "customerName": "Aida",
  "productName": "Keyboard",
  "price": 2500.00,
  "quantity": 2
}
```

Do not send `total`; the service calculates it.

Validation:

- `customerName`, `productName` — not blank
- `price` — greater than 0
- `quantity` — at least 1

Invalid body → **400**.

### List orders

```http
GET /api/orders
GET /api/orders?page=0&size=10&sort=id,asc
```

### Search

Case-insensitive search by customer or product name (JPQL, not in-memory `findAll`):

```http
GET /api/orders/search?q=Key
```

Blank `q` → **400**.

### Expensive orders

Orders whose `price` is strictly greater than the threshold (default `100`):

```http
GET /api/orders/expensive
GET /api/orders/expensive?price=1000
```

### Order total

```http
GET /api/orders/1/total
```

Response:

```json
{
  "orderId": 1,
  "total": 5000.00
}
```

Missing order → **404**.

### Update order

`PUT /api/orders/{id}` — same body as create. Missing order → **404**. Invalid body → **400**.

### Delete order

```http
DELETE /api/orders/1
```

Response: `{ "message": "deleted" }`. Missing order → **404**.

## Errors

Responses use RFC 7807 `ProblemDetail`.

| Case | HTTP |
|------|------|
| Order not found | 404 |
| Bean Validation failure | 400 |

## Tests

- `src/test/java/.../service/OrderServiceTest.java` — unit tests for business logic (Mockito)
- `src/test/java/.../controller/OrderControllerIntegrationTest.java` — API integration tests (MockMvc + H2)
