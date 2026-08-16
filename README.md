# 🚀 PayFlow

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)

**A production-grade distributed order & payment processing backend.** 

PayFlow simulates what happens behind the scenes when a real e-commerce platform processes an order at scale. It goes beyond simple CRUD operations to tackle the messy, real-world challenges of distributed systems: network failures, duplicate charges, slow third-party gateways, and partial state rollbacks.

---

## 📑 Table of Contents
1. [Why This Exists](#-why-this-exists)
2. [System Architecture](#-system-architecture)
3. [Tech Stack](#-tech-stack)
4. [Core Features](#-core-features)
5. [Notification Queue Integration](#-notification-queue-integration)
6. [API Reference](#-api-reference)
7. [Running Locally](#-running-locally)
8. [Load Testing](#-load-testing)
9. [Roadmap](#-roadmap)
10. [Author](#-author)

---

## 💡 Why This Exists

Most portfolio projects assume the "happy path" where networks never fail and databases never lock. PayFlow was built to answer a harder question: **What does it actually take to process a payment reliably at scale?**

If a payment gateway times out, the system retries. If a customer accidentally double-taps the checkout button, the system prevents duplicate charges. If a transaction fails mid-flight, the system gracefully rolls back reserved inventory instead of leaving the database in a broken state. These are the exact problems large-scale e-commerce backends (like Walmart's) solve every day.

---

## 🏗️ System Architecture

PayFlow decouples the fast "accept the request" path from the slower, failure-prone "process the payment" path. 

> **Client** → **JWT Auth Filter** → **Order Controller** (Synchronous)
> ↓ *Order Created Event* 
> **RabbitMQ Message Broker** (Asynchronous Queue)
> ↓ *Event Consumed*
> **Payment Consumer** → **Resilience4j Circuit Breaker** → **Mock Gateway**
> ↓ *Saga Pattern*
> **Database / Compensation Logic** 

---

## 🛠️ Tech Stack

| Layer | Technology | Purpose |
|---|---|---|
| **Language / Framework** | Java 21 & Spring Boot 3 | Core application framework and dependency injection. |
| **Database** | PostgreSQL | Relational data storage enforcing strict constraints and idempotency keys. |
| **Messaging** | RabbitMQ | Async event-driven communication decoupling checkout from payment. |
| **Security** | Spring Security & JWT | Stateless, role-based access control (RBAC). |
| **Resilience** | Resilience4j | Retries and Circuit Breakers for fault tolerance against flaky APIs. |
| **Caching & Limits** | Redis | High-speed data retrieval and distributed rate-limiting. |
| **Infrastructure** | Docker Compose | Containerization for reproducible local deployments. |

---

## 🔥 Core Features

*   **Idempotency Key Enforcement:** Client-generated keys ensure that duplicate network retries return the original order instead of double-charging the customer.
*   **Async Event-Driven Processing:** Order creation publishes an event to RabbitMQ. A separate, decoupled consumer processes the payment independently, keeping the checkout API lightning fast.
*   **Saga-Style Compensation:** If a payment ultimately fails, compensating transactions are automatically triggered to clean up and revert the system state instead of leaving the system in an inconsistent state.
*   **Fault Tolerance:** Integrated Circuit Breakers and Exponential Backoff Retries prevent cascading failures when external payment gateways go down.
*   **Advanced Security:** Passwords hashed with BCrypt. Stateless JWTs secure protected endpoints with `CUSTOMER` and `ADMIN` role restrictions.
*   **Distributed Rate Limiting:** Redis-backed request tracking prevents API abuse and DDoS attempts on the checkout endpoints.
*   **Performance Optimization:** Redis caching is implemented for fast order retrieval.

---

## 📡 Notification Queue Integration

PayFlow does not just operate in isolation. It is actively integrated with my other project, **[Notification Queue](https://github.com/rdjishnu/notification-queue)**. 

As orders transition through their lifecycle (`CREATED` → `PAYMENT_PENDING` → `PAID` / `FAILED`), PayFlow broadcasts real-time status events. The external Notification Queue consumes these payloads to dispatch timely alerts, demonstrating genuine cross-service communication and an event-driven microservices approach.

---

## 🔌 API Reference

| HTTP Method | Endpoint | Authorization | Description |
|---|---|---|---|
| `POST` | `/auth/register` | Public | Register a new user account. |
| `POST` | `/auth/login` | Public | Authenticate and receive a JWT. |
| `POST` | `/api/orders` | Bearer Token | Create a new order (requires Idempotency Key). |
| `GET` | `/api/orders/{id}` | Bearer Token | Fetch the status of a specific order. |
| `GET` | `/health` | Public | System health check. |

---

## 🚀 Running Locally

The entire ecosystem is containerized. You do not need to install Postgres, RabbitMQ, or Redis manually on your machine.

1. Clone the repository and navigate to the root directory.
2. Build and spin up the Docker network:
```bash
./mvnw clean package -DskipTests
docker compose build
docker compose up -d
---

## 🚦 Load Testing

To prove the Redis rate limiter and asynchronous architecture hold up under pressure, a custom multithreaded Java load test is included. 

Run the test suite against a live instance to simulate concurrent user checkouts:

    javac src/test/java/com/payflow/payflow/LoadTest.java
    java -cp src/test/java com.payflow.payflow.LoadTest

*Expected output: A mix of `Status: 200` (successful orders) and `Status: 429` (blocked by rate limiter).*

---

## 🗺️ Roadmap

- [x] Idempotency key enforcement
- [x] JWT authentication + role-based access control
- [x] Async payment processing via RabbitMQ
- [x] Saga-style compensating transactions on payment failure
- [x] Circuit breaker + retry on the simulated payment gateway
- [x] Redis caching + rate limiting
- [x] Integration with Notification Queue for order lifecycle notifications
- [x] Load testing + deployment

---

## 👨‍💻 Author

Built from scratch by **[Jishnu (rdjishnu)](https://github.com/rdjishnu)** as a comprehensive backend engineering portfolio project.