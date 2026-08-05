# Book Catalog Service

> **EyeBuyDirect Interview Code Challenge** — A Spring Boot microservice for managing a book catalog with full CRUD operations, design patterns, and AI-powered search.

---

## Assignment Requirements

| # | Requirement | Status |
|---|-------------|--------|
| 1 | RESTful CRUD API (No UI needed) | ✅ 10 endpoints |
| 2 | Apply at least 2 design patterns | ✅ 3 patterns (Strategy, Template Method, Observer) |
| 3 | Unit tests + API tests, >= 80% coverage | ✅ 102 tests, 84% line coverage |
| 4 | Build with AI tools | ✅ Trae AI IDE |
| 5 | Submit all AI prompts/skills/inputs | ✅ See [docs/AI_PROMPTS.md](docs/AI_PROMPTS.md) |

---

## Tech Stack

| Category | Technology | Version |
|----------|-----------|---------|
| Language | Java | 17 |
| Framework | Spring Boot | 3.2.5 |
| ORM | Spring Data JPA (Hibernate) | 6.1.6 |
| Database | H2 (dev) / MySQL (prod) | 8.x |
| Cache | Spring Data Redis | 3.2.5 |
| API Docs | SpringDoc OpenAPI (Swagger UI) | 2.5.0 |
| Testing | JUnit 5 + Mockito | 5.10.x |
| Coverage | JaCoCo | 0.8.11 |

---

## Architecture

```
┌─────────────────────────────────────────────────────┐
│                   Book Catalog Service               │
├─────────────────────────────────────────────────────┤
│  Controller Layer  │  BookController (REST API)      │
│                    │  GlobalExceptionHandler         │
├─────────────────────────────────────────────────────┤
│  Service Layer     │  BookServiceImpl               │
│  ┌───────────────┐│  Strategy Pattern: Search       │
│  │  Design       ││  Template Pattern: Export      │
│  │  Patterns     ││  Observer Pattern: Events      │
│  └───────────────┘│                                 │
├─────────────────────────────────────────────────────┤
│  Repository Layer  │  BookRepository (JPA)           │
├─────────────────────────────────────────────────────┤
│  Infrastructure   │  RedisCache  │  OpenAPI  │  AOP │
├─────────────────────────────────────────────────────┤
│  Database          │  H2 / MySQL                     │
└─────────────────────────────────────────────────────┘
```

---

## Design Patterns (3)

| Pattern | Purpose | Details |
|---------|---------|---------|
| **Strategy** | Multi-dimensional search (title/author/genre) | [docs/DESIGN.md](docs/DESIGN.md#1-strategy-pattern--multi-dimensional-search) |
| **Template Method** | Data export (CSV/JSON) | [docs/DESIGN.md](docs/DESIGN.md#2-template-method-pattern--data-export) |
| **Observer** | Event-driven audit logging | [docs/DESIGN.md](docs/DESIGN.md#3-observer-pattern--event-driven-audit) |

---

## RESTful API

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/books` | Create a new book |
| GET | `/api/books/{id}` | Get book by ID |
| PUT | `/api/books/{id}` | Update book information |
| DELETE | `/api/books/{id}` | Delete a book |
| GET | `/api/books` | Search books (paginated) |
| GET | `/api/books/all` | Get all books |
| PATCH | `/api/books/{id}/stock` | Adjust stock (delta) |
| PATCH | `/api/books/{id}/toggle` | Toggle active status |
| GET | `/api/books/export` | Export books (CSV/JSON) |
| GET | `/api/books/ai-search` | AI-powered smart search |

Full API reference with request/response examples: [docs/API.md](docs/API.md)

---

## Database Schema

### Table: `t_book`

| Column | Type | Constraints |
|--------|------|------------|
| `id` | BIGINT | PK, AUTO_INCREMENT |
| `title` | VARCHAR(500) | NOT NULL |
| `author` | VARCHAR(200) | NOT NULL |
| `isbn` | VARCHAR(20) | — |
| `genre` | VARCHAR(50) | — |
| `price` | DECIMAL(10,2) | — |
| `description` | VARCHAR(2000) | — |
| `stock` | INT | NOT NULL, DEFAULT 0 |
| `pages` | INT | NOT NULL, DEFAULT 0 |
| `publisher` | VARCHAR(50) | — |
| `publish_date` | DATETIME | — |
| `active` | TINYINT(1) | NOT NULL, DEFAULT 1 |
| `create_time` | DATETIME | NOT NULL, DEFAULT NOW() |
| `update_time` | DATETIME | NOT NULL, ON UPDATE NOW() |
| `version` | BIGINT | DEFAULT 0 (optimistic lock) |

**Indexes**: `idx_book_title`, `idx_book_author`, `idx_book_genre`, `idx_book_isbn`

Full DDL script & sample data: [docs/DATABASE.md](docs/DATABASE.md) | [schema.sql](src/main/resources/db/schema.sql)

---

## Project Structure

```
book-catalog-service/
├── src/main/java/com/eyebuy/bookcatalog/
│   ├── BookCatalogApplication.java
│   ├── controller/
│   │   ├── BookController.java
│   │   └── advice/GlobalExceptionHandler.java
│   ├── service/
│   │   ├── BookService.java
│   │   ├── impl/BookServiceImpl.java
│   │   ├── strategy/          # Strategy Pattern
│   │   └── template/          # Template Method Pattern
│   ├── event/                 # Observer Pattern
│   ├── repository/BookRepository.java
│   ├── entity/Book.java
│   ├── dto/                   # 7 DTOs
│   ├── enums/BookGenre.java
│   ├── exception/             # 3 custom exceptions
│   └── config/                # Redis, OpenAPI, DataInitializer
├── src/main/resources/
│   ├── application.yml
│   ├── application-mysql.yml
│   └── db/schema.sql          # DDL script
├── src/test/                  # 102 test cases
├── docs/                      # Detailed documentation
│   ├── API.md
│   ├── DESIGN.md
│   ├── DATABASE.md
│   └── AI_PROMPTS.md
├── pom.xml
└── README.md
```

---

## Testing & Code Coverage

| Metric | Coverage | Status |
|--------|----------|--------|
| **Line Coverage** | **84.0%** | ✅ Exceeds 80% |
| Method Coverage | 73.4% | ✅ |
| **Test Cases** | **102** | ✅ All passing |

```bash
# Run tests
mvn test

# Coverage report
# target/site/jacoco/index.html
```

---

## AI Tools & Prompts

This project was built with AI-powered coding tools. All AI prompts, skills, and development workflow are documented in:

[docs/AI_PROMPTS.md](docs/AI_PROMPTS.md)

---

## Quick Start

### Prerequisites

- JDK 17+
- Maven 3.9+

### Run (H2 in-memory, zero config)

```bash
mvn spring-boot:run
```

### Access

| Service | URL |
|---------|-----|
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| H2 Console | http://localhost:8080/h2-console |

### Run with MySQL

```bash
mysql -u root -p < src/main/resources/db/schema.sql
mvn spring-boot:run -Dspring.profiles.active=mysql
```

### Sample API Calls

```bash
# Create a book
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{"title":"Clean Code","author":"Robert C. Martin","isbn":"978-0132350884","genre":"TECHNOLOGY","price":39.99,"stock":50}'

# Search books
curl "http://localhost:8080/api/books?keyword=code&genre=TECHNOLOGY&page=0&size=10"

# AI smart search
curl "http://localhost:8080/api/books/ai-search?query=best programming practices"

# Export as CSV
curl http://localhost:8080/api/books/export?format=csv -o books.csv
```

---

## Configuration

| Profile | Database | Cache |
|---------|----------|-------|
| `default` | H2 (in-memory) | Redis |
| `test` | H2 (in-memory) | Simple |
| `mysql` | MySQL | Redis |

---

## Documentation Index

| Document | Description |
|----------|-------------|
| [docs/API.md](docs/API.md) | Full RESTful API reference with examples |
| [docs/DESIGN.md](docs/DESIGN.md) | Design pattern implementation details |
| [docs/DATABASE.md](docs/DATABASE.md) | Database schema, DDL, and entity mapping |
| [docs/AI_PROMPTS.md](docs/AI_PROMPTS.md) | AI tools, prompts, and development workflow |

---

## Future Enhancements

### 1. AI-Powered Intelligent Recommendations
- Integrate LLM APIs (OpenAI / Tongyi Qianwen) for natural language book recommendations
- Build a vector embedding pipeline with Chroma/Milvus for semantic similarity search
- Implement personalized recommendation engine based on user browsing & purchase history
- Support multi-turn conversational search (e.g., "Find books similar to Clean Code but focused on architecture")

### 2. Full-Text Search with Elasticsearch
- Replace current JPA Specification-based search with Elasticsearch for fuzzy matching, typo tolerance, and relevance scoring
- Add Chinese/English analyzer support for multilingual book catalogs
- Implement autocomplete and search suggestions via completion suggester

### 3. Async Event Processing with Message Queue
- Migrate current Spring ApplicationEvent to Kafka/RabbitMQ for reliable, decoupled event-driven architecture
- Add event sourcing pattern for book inventory audit trail (stock changes, status toggles)
- Support outbox pattern for transactional consistency between database and message queue

### 4. Containerization & Cloud Deployment
- Dockerfile with multi-stage build (Maven build → JRE runtime, ~200MB image)
- Docker Compose for local dev (app + MySQL + Redis + Elasticsearch)
- Kubernetes manifests with HPA (Horizontal Pod Autoscaler) for auto-scaling
- CI/CD pipeline with GitHub Actions: build → test → docker push → k8s deploy

### 5. Observability & Monitoring
- Prometheus metrics (request latency, error rate, cache hit ratio, JVM metrics)
- Grafana dashboards for real-time visualization
- Distributed tracing with OpenTelemetry + Jaeger for cross-service call chains
- Structured JSON logging with ELK stack for centralized log aggregation

### 6. Security & Authentication
- Spring Security with JWT-based stateless authentication
- Role-based access control (ADMIN: full CRUD, USER: read-only)
- API rate limiting via Redis token bucket algorithm
- Input sanitization and SQL injection prevention audit

### 7. Performance Optimization
- Multi-level cache strategy: L1 (Caffeine local cache, 1min TTL) → L2 (Redis, 30min TTL)
- Database read-write splitting with ShardingSphere for high-traffic scenarios
- Batch import/export with async processing for large datasets (>10K records)
- GraphQL API support for flexible client-side field selection

---

**Author**: Zhu LeFei (朱乐飞)  
**Email**: leopold_zhu@hotmail.com  
**Date**: 2026-08-05
