# Reactive Billing API

A non-blocking invoice management REST API built with **Spring WebFlux**, **Project Reactor** and **Spring Data R2DBC** on **PostgreSQL**, with interactive **Swagger UI** documentation.

**Stack:** Java 17 · Spring Boot 3.3 · WebFlux · R2DBC · PostgreSQL · springdoc-openapi · Jakarta Validation · Gradle · Docker Compose · JUnit 5 · Mockito · Reactor Test (`StepVerifier`) · `WebTestClient`

## Features

- Fully reactive stack: controllers, services and database access all return `Mono` / `Flux`
- Invoice lifecycle: an invoice starts as `DRAFT` and can be paid or cancelled once; after that it is final
- Request validation with clear field-level errors
- Consistent error responses using RFC 7807 `application/problem+json`
- Request logging through a `WebFilter`
- OpenAPI 3 spec and Swagger UI generated from the code

## Quick start

**With Docker only** (app + database):

```bash
docker compose up --build
```

**For development** (database in Docker, app from Gradle):

```bash
docker compose up -d postgres
gradle wrapper        # one time, or open the folder in IntelliJ
./gradlew bootRun
```

Then open:

| What | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |

## API

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/invoices` | Create an invoice. Returns `201` with a `Location` header |
| `GET` | `/api/invoices` | List invoices as a stream. Optional `status` and `customer` filters |
| `GET` | `/api/invoices/{id}` | Get one invoice (`404` if missing) |
| `PUT` | `/api/invoices/{id}` | Update an invoice (`DRAFT` only, otherwise `409`) |
| `POST` | `/api/invoices/{id}/pay` | `DRAFT` → `PAID` |
| `POST` | `/api/invoices/{id}/cancel` | `DRAFT` → `CANCELLED` |
| `DELETE` | `/api/invoices/{id}` | Delete an invoice (`DRAFT` only). Returns `204` |
| `GET` | `/api/invoices/summary` | Total amount per status |

### Try it

```bash
# create
curl -s -X POST localhost:8080/api/invoices -H "Content-Type: application/json" \
  -d '{"customerName":"Acme Corp","amount":1500.00,"currency":"INR"}'

# list, pay, then pay again
curl -s "localhost:8080/api/invoices?status=DRAFT&customer=acme"
curl -s -X POST localhost:8080/api/invoices/1/pay
curl -s -X POST localhost:8080/api/invoices/1/pay    # 409: already paid
curl -s localhost:8080/api/invoices/summary
```

### Validation rules

| Field | Rule |
|---|---|
| `customerName` | required, max 100 characters |
| `amount` | required, at least `0.01`, up to 2 decimal places |
| `currency` | required, one of `INR`, `USD`, `EUR` |

### Error format

```json
{
  "type": "about:blank",
  "title": "Validation failed",
  "status": 400,
  "detail": "Request body is invalid",
  "errors": { "currency": "must be one of INR, USD, EUR" }
}
```

| Status | When |
|---|---|
| `400` | Request body failed validation |
| `404` | Invoice does not exist |
| `409` | Invoice is no longer `DRAFT` |

## Project structure

```
src/main/java/com/billing
├── config/       OpenAPI / Swagger metadata
├── domain/       Invoice, InvoiceStatus
├── repository/   R2DBC repository
├── service/      Business rules and reactive pipelines
├── web/          Controller, DTOs, global exception handler
├── filter/       RequestLoggingFilter (WebFilter)
└── error/        Domain exceptions
```

## Reactive concepts in this project

| Concept | Where |
|---|---|
| Non-blocking end to end | every class under `service/` and `web/` |
| R2DBC repository | `repository/InvoiceRepository`, `application.yml`, `schema.sql` |
| `Mono` vs `Flux` | `get()` returns one `Mono`; `list()` returns a `Flux` |
| `flatMap` for chained async steps | `InvoiceService.update`, `transition`, `delete` |
| `switchIfEmpty` to turn "no row" into a 404 | `InvoiceService.get` |
| Collecting a stream into one result | `InvoiceService.totalsByStatus` |
| Input validation | `web/dto/InvoiceRequest` |
| Reactive error handling | `web/GlobalExceptionHandler` |
| `WebFilter` for cross-cutting logic | `filter/RequestLoggingFilter` |
| Testing reactive code | `InvoiceServiceTest` (`StepVerifier`), `InvoiceControllerTest` (`WebTestClient`) |

## Testing

```bash
./gradlew test
```

Tests run without Docker.

## Roadmap

- [ ] Router Functions (functional endpoints)
- [ ] Flyway migrations instead of `schema.sql`
- [ ] Integration tests with Testcontainers
- [ ] Pagination and optimistic locking

---

Built while working through the *Spring WebFlux Masterclass: High-Performance Reactive APIs* course.
