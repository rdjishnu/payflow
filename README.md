# PayFlow — Distributed Order & Payment Processing System

A backend system simulating a real-world e-commerce checkout flow (inspired by large-scale platforms like Walmart), built to demonstrate distributed systems patterns: async event-driven processing, idempotency, resilience (retries/circuit breakers), the saga pattern, and JWT-secured RBAC.

## Why this project exists
Most CRUD projects stop at "create and fetch data." PayFlow goes further — it models what actually happens when an order is placed at scale: payment processing that can fail, retries that don't double-charge customers, and compensating actions when something breaks mid-flow.

## Tech Stack
- **Java 21**, **Spring Boot 3**
- **PostgreSQL** — persistent storage
- **RabbitMQ** — async event-driven order/payment processing
- **Redis** — idempotency caching, rate limiting
- **Resilience4j** — circuit breaker + retry logic
- **Docker Compose** — local infra orchestration
- **Spring Security + JWT** — authentication & role-based access control

## Current Features (Day 1)
- Order entity with status lifecycle (`CREATED → PAYMENT_PENDING → PAID/FAILED → FULFILLED`)
- `POST /orders` — create an order
- `GET /orders/{id}` — fetch an order by ID
- Dockerized PostgreSQL for local development

## Roadmap
- [ ] Idempotency key enforcement
- [ ] JWT authentication + role-based access (CUSTOMER / ADMIN)
- [ ] Async payment processing via RabbitMQ
- [ ] Circuit breaker + retry on simulated payment gateway
- [ ] Saga-style compensating transactions on payment failure
- [ ] Redis caching + rate limiting
- [ ] Integration with [Notification Queue Simulator](https://github.com/rdjishnu/notification-queue) for order lifecycle notifications
- [ ] Load testing + deployment

## Running Locally
```bash
docker compose up -d
./mvnw spring-boot:run
```

## Author
Built by [Jishnu](https://github.com/rdjishnu) as part of a backend portfolio project.