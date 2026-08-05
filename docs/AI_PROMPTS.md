# AI Tools & Prompts

> Book Catalog Service — AI Development Record (Requirement #4 & #5)

---

## AI Coding Tools Used

| Tool | Version | Purpose |
|------|---------|---------|
| **Trae (AI IDE)** | Latest | Code generation, refactoring, test generation, code review |
| **Built-in AI Assistant** | — | Architecture design, API design, bug detection |

## AI Skills / Capabilities Invoked

| # | Skill | Description |
|---|-------|-------------|
| 1 | **Code Generation** | Full project scaffolding, entity/DTO/VO/repository/service/controller generation |
| 2 | **Design Pattern Application** | Strategy, Template Method, Observer patterns implementation |
| 3 | **Test Generation** | Automatic generation of unit tests (Mockito) and integration tests (MockMvc) |
| 4 | **Code Review** | Automated detection of compilation errors, type mismatches, missing imports |
| 5 | **Documentation Generation** | API documentation (OpenAPI), README, Javadoc |
| 6 | **Bug Detection & Fixing** | Identifying Mockito ambiguity, JPA specification type erasure, dependency issues |
| 7 | **Experience Recall** | Leveraging prior coding experience to avoid known pitfalls |

---

## AI Prompts / Instructions

Below are all AI prompts and instructions used during development, submitted as required by the assignment.

### Prompt 1: Project Initialization

```
Build a "Book Catalog Service": a Spring Boot microservice that allows clients to
manage a collection of books (CRUD)

Requirements:
1. RESTful CRUD API (No UI needed)
2. Apply at least 2 design patterns in your code
3. Write unit tests and API tests. Aim for >= 80% code coverage
4. Should build it with AI tools
5. Should submit all the AI prompts, skills or any other AI related inputs used
```

### Prompt 2: Architecture Design

```
Design a clean, layered Spring Boot architecture:
- Controller -> Service -> Repository pattern
- Strategy pattern for search (multiple search dimensions)
- Template Method pattern for export (CSV/JSON formats)
- Observer pattern for audit events
- Redis caching for performance
- Global exception handling with unified response format
- OpenAPI/Swagger documentation
```

### Prompt 3: Entity & DTO Design

```
Create JPA entity for Book with fields:
- id, title, author, isbn, genre, price, description
- stock, pages, publisher, publishDate, active
- createTime, updateTime, version (optimistic locking)

Create DTOs:
- BookCreateRequest (for POST)
- BookUpdateRequest (for PUT, all fields optional)
- BookResponse (for GET responses)
- BookSearchRequest (for search filters)
- PageResponse<T> (for paginated results)
- ApiResponse<T> (unified response wrapper)
- ExportResult (for export responses)
```

### Prompt 4: Service Implementation

```
Implement BookService with:
- CRUD operations with duplicate ISBN checking
- Stock management with balance validation
- Status toggle (active/inactive)
- Multi-condition search with JPA Specifications
- Export to CSV and JSON using template pattern
- AI-powered smart search with relevance scoring
- Event publishing for audit trail
- Redis caching for read-heavy operations
```

### Prompt 5: Controller & API Design

```
Create REST controller with:
- Proper HTTP method semantics (GET/POST/PUT/DELETE/PATCH)
- Request validation with Jakarta Validation
- OpenAPI annotations for Swagger documentation
- Proper error responses with status codes
- File download support for export endpoint
```

### Prompt 6: Test Generation

```
Generate comprehensive test suite:
- Unit tests for each service method (Mockito)
- Integration tests for all API endpoints (MockMvc)
- Repository tests with @DataJpaTest
- Strategy/Event/Template tests
- Aim for >= 80% line coverage
- Test edge cases: empty inputs, invalid states, duplicates
```

### Prompt 7: AI Smart Search Enhancement

```
Implement AI-powered smart search:
- Multi-field token-based matching
- Relevance scoring with weighted fields (title > author > description)
- Exact match > prefix match > contains match
- Support multi-keyword queries with AND logic
- Sorted by relevance score descending
```

### Prompt 8: DDL & Documentation

```
根据试题要求，生成一份相关的README.md文件
提供相关db、table的DDL
补充进 README.md 文件中，另外README.md内容太多，考虑是否有必须拆分
```

---

## AI-Assisted Development Workflow

```
┌──────────────────────────────────────────────────────────┐
│                    AI Development Workflow                │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  1. Requirement Analysis                                 │
│     └─▶ AI parses assignment requirements                │
│                                                          │
│  2. Architecture Design                                  │
│     └─▶ AI proposes layered architecture + patterns      │
│                                                          │
│  3. Code Generation (iterative)                          │
│     ├─▶ Entity / DTO / Repository                        │
│     ├─▶ Service with Strategy + Template + Observer      │
│     ├─▶ Controller with validation + OpenAPI             │
│     └─▶ Configuration (Redis / Cache / Swagger)          │
│                                                          │
│  4. Test Generation                                      │
│     ├─▶ Unit tests (Mockito) for Service layer           │
│     ├─▶ Integration tests (MockMvc) for Controller       │
│     ├─▶ Repository tests (@DataJpaTest)                  │
│     └─▶ Pattern-specific tests (Strategy/Template/Event) │
│                                                          │
│  5. Bug Fixing (AI-assisted)                             │
│     ├─▶ Mockito ambiguity → ArgumentMatchers             │
│     ├─▶ JPA Specification type erasure → explicit typing │
│     └─▶ H2 unique constraint → index adjustment          │
│                                                          │
│  6. Verification                                         │
│     ├─▶ mvn clean test → 102 tests pass                  │
│     └─▶ JaCoCo coverage → 84% line coverage              │
│                                                          │
│  7. Documentation                                        │
│     ├─▶ README.md (main)                                 │
│     ├─▶ docs/API.md (API reference)                      │
│     ├─▶ docs/DESIGN.md (design patterns)                 │
│     ├─▶ docs/DATABASE.md (DDL + schema)                  │
│     └─▶ docs/AI_PROMPTS.md (this file)                   │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

---

## AI Experience Recall

During development, the AI leveraged prior coding experience to avoid known pitfalls:

| Experience | Applied Lesson |
|-----------|---------------|
| Java/Spring/MyBatis-Plus audit issues | Batch query + Map backfill to avoid N+1 |
| Spring Boot code repository | Actually write files via tools, not just output code blocks |
| Spring Boot + MongoDB vs MySQL | Align storage medium with user requirements |
| Java DTO/VO design | Separate VO from Entity to avoid coupling |
| Mockito ambiguity | Use `ArgumentMatchers.<Type>any()` for generic type inference |
