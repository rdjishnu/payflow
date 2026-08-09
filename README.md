# PayFlow

**A distributed order & payment processing system** — simulating what happens behind the scenes when a real e-commerce platform processes an order at scale, from checkout through payment settlement.

Built to demonstrate the backend patterns that matter in production systems: async event-driven processing, idempotency, resilience under failure, the saga pattern, and JWT-secured role-based access control.

---

## Why this exists

Most backend portfolio projects stop at "create and fetch data." PayFlow goes further — it models what actually happens when a payment can fail, a request can be retried without double-charging a customer, and a mid-flow failure needs to be cleanly undone rather than left in a broken state. These are the exact problems large-scale e-commerce backends (like Walmart's) solve every day.

## Architecture
Client → JWT Auth → Order Service → RabbitMQ → Payment Consumer → Postgres
↓
Retry / Circuit Breaker
↓
Saga Compensation (on failure)

An order is created synchronously and validated, then payment processing is handed off asynchronously via a message queue — decoupling the fast "accept the request" path from the slower, failure-prone "process the payment" path. If payment fails, a compensating action runs automatically instead of leaving the system in an inconsistent state.

## Tech stack

| Layer | Technology |
|---|---|
| Language / Framework | Java 21, Spring Boot 3 |
| Database | PostgreSQL |
| Messaging | RabbitMQ |
| Security | Spring Security, JWT |
| Resilience | Resilience4j (retry, circuit breaker) |
| Infrastructure | Docker Compose |

## Features

- **Order lifecycle management** — `CREATED → PAYMENT_PENDING → PAID / FAILED → FULFILLED`
- **Idempotency key enforcement** — duplicate order submissions return the original order instead of creating a copy
- **JWT authentication** — register/login endpoints issue bearer tokens; protected routes require a valid token
- **Role-based access control** — `CUSTOMER` and `ADMIN` roles
- **Async payment processing** — order creation publishes an event to RabbitMQ; a separate consumer processes payment independently
- **Saga-style compensation** — failed payments trigger a compensating transaction rather than leaving orders in an inconsistent state

## API

| Method | Endpoint | Auth required |
|---|---|---|
| `POST` | `/auth/register` | No |
| `POST` | `/auth/login` | No |
| `POST` | `/orders` | Yes |
| `GET` | `/orders/{id}` | Yes |
| `GET` | `/health` | No |

## Running locally

```bash
docker compose up -d
./mvnw spring-boot:run
```

The app starts on `localhost:8080`. Postgres and RabbitMQ run as containers via Docker Compose.

## Roadmap

- [x] Idempotency key enforcement
- [x] JWT authentication + role-based access control
- [x] Async payment processing via RabbitMQ
- [x] Saga-style compensating transactions on payment failure
- [ ] Circuit breaker + retry on the simulated payment gateway
- [ ] Redis caching + rate limiting
- [ ] Integration with [Notification Queue](https://github.com/rdjishnu/notification-queue) for order lifecycle notifications
- [ ] Load testing + deployment

## Author

Built by [Jishnu](https://github.com/rdjishnu) as a backend engineering portfolio project.# test
