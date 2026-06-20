# DineLock

Checklist (what this README delivers)

- [x] Project overview and goals
- [x] High-level architecture and sequence flows
- [x] Technology stack and components
- [x] Step-by-step local setup (PowerShell and Docker)
- [x] Database schema and migration guidance
- [x] Concurrency & locking strategies (MySQL InnoDB)
- [x] Gemini AI sentiment analysis integration guidance
- [x] Example REST endpoints, security, testing and deployment notes

---

## Project Overview

DineLock is a high-concurrency restaurant reservation engine implemented as a Spring Boot REST API. Its core objective is to prevent double-bookings and race conditions by leveraging MySQL row-level locking (InnoDB) and disciplined transaction handling. The system also optionally enriches restaurant reviews with AI-driven sentiment analysis using the Gemini API.

Primary goals:

- Prevent overlapping reservations for the same table/time through transactional locking
- Provide a resilient retry and backoff strategy under contention
- Offload non-critical AI analysis (Gemini) to asynchronous workers when appropriate
- Offer observability, health checks and configurable deployment options

## Architecture

- REST API: Spring Boot controllers handle incoming HTTP requests
- Service layer: business logic, transactions and retry/backoff
- Persistence: Spring Data JPA (Hibernate) + MySQL (InnoDB)
- Optional queueing: Redis or Kafka for smoothing burst traffic and serializing work
- AI layer: Gemini client (secure server-side calls, rate limit handling)
- Observability: Actuator, Prometheus metrics, Grafana dashboards

Sequence (simplified):

1. Client submits reservation request
2. Controller validates payload and forwards to Service
3. Service opens a transaction and performs a row-level lock (SELECT ... FOR UPDATE) on the target resource
4. Service checks availability, creates reservation, commits transaction
5. Review submissions optionally trigger asynchronous Gemini sentiment analysis

## Tech Stack

- Java 17 or 21
- Spring Boot 3.x
- Spring Data JPA (Hibernate)
- MySQL 8.x (InnoDB)
- Maven
- Flyway or Liquibase for DB migrations
- Docker & docker-compose for local development
- JUnit 5, Mockito for tests
- Prometheus + Grafana for monitoring

## Features

- Transactional reservation creation with MySQL row-level locking
- Retry/backoff strategies and deadlock handling guidance
- Asynchronous Gemini sentiment analysis for reviews
- Swagger / OpenAPI documentation
- Health checks and metrics via Spring Actuator

## Requirements

- Java SDK 17 or 21
- Maven 3.6+
- MySQL 8.x (or Docker)
- Internet access for Gemini API calls (or a local stub in development)

## Environment Variables

Set these environment variables before running in local/CI. Replace placeholders with real values.

- `SPRING_DATASOURCE_URL` (e.g. jdbc:mysql://localhost:3306/dinelock?useSSL=false&serverTimezone=UTC)
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `GEMINI_API_KEY` (store securely)
- `GEMINI_API_BASE_URL` (optional override)
- `APP_PROFILE` (dev, prod)

## Quick start (Windows PowerShell)

1) Build the project (skip tests for faster local iteration):

```powershell
mvn -f "C:\Users\hp\IdeaProjects\DineLock\pom.xml" clean package -DskipTests
```

2) Export environment variables (example):

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:mysql://localhost:3306/dinelock?useSSL=false&serverTimezone=UTC"
$env:SPRING_DATASOURCE_USERNAME = "dine_user"
$env:SPRING_DATASOURCE_PASSWORD = "super-secret"
$env:GEMINI_API_KEY = "your_gemini_api_key"
```

3) Run the packaged JAR:

```powershell
java -jar "C:\Users\hp\IdeaProjects\DineLock\target\DineLock-0.0.1-SNAPSHOT.jar"
```

Or run directly from your IDE using `com.dinelock.DineLockApplication`.

## Docker Compose (development)

Use the following `docker-compose.yml` locally for a simple dev environment. Do not use this as-is in production.

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: dinelock
      MYSQL_USER: dine_user
      MYSQL_PASSWORD: super-secret
    ports:
      - "3306:3306"
    command: --default-authentication-plugin=mysql_native_password
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  app:
    build: .
    depends_on:
      - mysql
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/dinelock?useSSL=false&serverTimezone=UTC
      - SPRING_DATASOURCE_USERNAME=dine_user
      - SPRING_DATASOURCE_PASSWORD=super-secret
      - GEMINI_API_KEY=${GEMINI_API_KEY}
    ports:
      - "8080:8080"
```

Start with PowerShell:

```powershell
docker-compose up --build
```

## Configuration Examples

Example `src/main/resources/application.properties` entries:

```properties
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# Gemini
gemini.api.key=${GEMINI_API_KEY}
gemini.api.base-url=${GEMINI_API_BASE_URL:https://api.gemini.example}

# Actuator
management.endpoints.web.exposure.include=health,info,prometheus
```

## Database Schema (examples) & Migrations

Suggested tables (simplified):

- `restaurants` (id, name, timezone, created_at)
- `tables` (id, restaurant_id, name, seats, created_at)
- `reservations` (id, restaurant_id, table_id, user_id, start_time, end_time, status, created_at)
- `reviews` (id, restaurant_id, user_id, text, sentiment_score, sentiment_label, created_at)

Example `reservations` SQL (Flyway/Liquibase migration file recommended):

```sql
CREATE TABLE reservations (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  restaurant_id BIGINT NOT NULL,
  table_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  start_time DATETIME NOT NULL,
  end_time DATETIME NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_table FOREIGN KEY (table_id) REFERENCES tables(id)
) ENGINE=InnoDB;
```

Add composite indexes for queries used during availability checks, e.g. `(restaurant_id, table_id, start_time)`.

## Concurrency & Locking Strategy

This is the core of DineLock. Two main approaches with trade-offs:

1) Pessimistic Locking (recommended for heavy contention)

- Use `SELECT ... FOR UPDATE` inside a single transaction to lock the relevant rows (table availability or table resource). This prevents other transactions from reading/modifying the same row until commit/rollback.
- Example flow:
  - Begin transaction
  - `SELECT` target row `FOR UPDATE`
  - Check for overlapping reservations
  - Insert reservation and commit

Example using `EntityManager` (native query):

```text
// inside a @Transactional service method
Query q = entityManager.createNativeQuery("SELECT * FROM tables WHERE id = ? FOR UPDATE", Table.class);
q.setParameter(1, tableId);
Table locked = (Table) q.getSingleResult();
// check availability and persist reservation
```

2) Optimistic Locking (for low contention)

- Use `@Version` on entities. On update, Hibernate checks version and throws `OptimisticLockException` on conflicts. Implement retry/backoff on exceptions.

Other considerations:

- Transaction isolation: InnoDB default is REPEATABLE READ; `FOR UPDATE` is typically sufficient. Consider `READ COMMITTED` if you need different semantics.
- Implement exponential backoff and a finite retry limit for lock wait timeouts or optimistic lock failures.
- If DB locking becomes a bottleneck, adopt asynchronous queuing (Kafka/Redis) and worker processes to serialize reservation attempts per restaurant or table shard.

## Gemini API: Sentiment Analysis

Design notes:

- Keep the Gemini API key server-side and never commit it.
- Perform sentiment analysis asynchronously if latency matters to the user. Persist review text immediately and enqueue analysis.
- Handle rate limits, retries, and fallbacks (e.g., mark analysis as failed and retry offline).

Simple pseudo-code for synchronous analysis:

```text
String prompt = "Analyze sentiment: " + reviewText;
GeminiResponse resp = geminiClient.analyze(prompt);
review.setSentimentScore(resp.getScore());
review.setSentimentLabel(resp.getLabel());
reviewRepository.save(review);
```

For production, use a resilient HTTP client, circuit breaker, and background worker for analysis.

## REST API - Example Endpoints

- POST /api/v1/reservations — create reservation
- GET /api/v1/reservations/{id} — get reservation
- GET /api/v1/restaurants — list restaurants
- POST /api/v1/restaurants/{id}/reviews — submit review (triggers Gemini analysis)
- GET /actuator/health
- GET /v3/api-docs (OpenAPI)

Document full request/response schemas with OpenAPI (Swagger) and include error codes and concurrency failure responses (e.g., 409 Conflict for double-booking).

## Security

- Require TLS in production
- Authenticate requests (JWT/OAuth) and authorize resource access
- Protect Gemini API key and other secrets using vaults or environment variables
- Input validation and parameterized queries (JPA) to prevent injection
- Apply rate limiting per user/API key/IP

## Testing & CI

- Unit tests: JUnit 5 + Mockito
- Integration tests: SpringBootTest with Testcontainers MySQL or an embedded MySQL-compatible DB
- Contract tests: OpenAPI-based

Example commands (PowerShell):

```powershell
# Run tests
mvn -f "C:\Users\hp\IdeaProjects\DineLock\pom.xml" test

# Build package
mvn -f "C:\Users\hp\IdeaProjects\DineLock\pom.xml" clean package
```

CI pipeline recommendations (GitHub Actions/GitLab CI):

- Run `mvn test` on PRs
- Build and publish Docker image on merge
- Run integration tests using Testcontainers in CI

## Observability & Logging

- Expose health and metrics via Spring Actuator
- Push Prometheus metrics and visualize with Grafana
- Centralize logs with ELK/EFK
- Add distributed tracing (Jaeger/Zipkin) for transaction tracing

## Deployment Recommendations

- Containerize with Docker and provide Kubernetes manifests
- Use readiness/liveness probes and resource limits
- Use connection pool sizing appropriate for DB and replicas
- Prefer rolling, canary or blue/green deployments

## Troubleshooting & FAQ

- Lock wait timeout (ERROR 1205): shorten transactions, add proper indexes, implement retry/backoff
- Deadlocks: analyze MySQL deadlock logs, minimize transaction scope, standardize lock acquisition order
- Gemini API errors: monitor rate limits, validate API key, implement retries and fallback logic

## Contributing

1. Fork the repository
2. Create a feature branch (e.g. `feature/locking-improvement`)
3. Add tests for new behavior
4. Open a pull request and reference related issues

Follow project Java style and API compatibility rules.

## License

Add a `LICENSE` file (e.g. MIT) to the repository and reference it here.

---
