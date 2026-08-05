# API Documentation

> Book Catalog Service — RESTful API Reference

---

## Base URL

```
http://localhost:8080/api/books
```

## API Endpoints Overview

| Method | Endpoint | Description |
|--------|----------|-------------|
| **POST** | `/api/books` | Create a new book |
| **GET** | `/api/books/{id}` | Get book by ID |
| **PUT** | `/api/books/{id}` | Update book information |
| **DELETE** | `/api/books/{id}` | Delete a book |
| **GET** | `/api/books` | Search books (paginated) |
| **GET** | `/api/books/all` | Get all books |
| **PATCH** | `/api/books/{id}/stock` | Adjust stock (delta) |
| **PATCH** | `/api/books/{id}/toggle` | Toggle active status |
| **GET** | `/api/books/export` | Export books (CSV/JSON) |
| **GET** | `/api/books/ai-search` | AI-powered smart search |

---

## 1. Create Book

```http
POST /api/books
Content-Type: application/json
```

### Request Body

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| `title` | String | Yes | max 500 chars | Book title |
| `author` | String | Yes | max 200 chars | Author name |
| `isbn` | String | No | max 20 chars | ISBN number |
| `genre` | String | No | max 50 chars | Book genre |
| `price` | BigDecimal | No | — | Price |
| `description` | String | No | max 2000 chars | Description |
| `stock` | Integer | No | default 0 | Stock quantity |
| `pages` | Integer | No | default 0 | Page count |
| `publisher` | String | No | max 50 chars | Publisher |
| `publishDate` | String | No | yyyy-MM-dd | Publication date |

### Example

```bash
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Clean Code",
    "author": "Robert C. Martin",
    "isbn": "978-0132350884",
    "genre": "TECHNOLOGY",
    "price": 39.99,
    "stock": 50,
    "pages": 464,
    "publisher": "Prentice Hall",
    "publishDate": "2008-08-01"
  }'
```

### Response (200 OK)

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "title": "Clean Code",
    "author": "Robert C. Martin",
    "isbn": "978-0132350884",
    "genre": "TECHNOLOGY",
    "price": 39.99,
    "stock": 50,
    "active": true,
    "createTime": "2026-08-05T10:00:00",
    "updateTime": "2026-08-05T10:00:00"
  },
  "timestamp": 1722823200000
}
```

### Error Responses

| Status | Condition |
|--------|-----------|
| 400 | Validation error (missing title/author) |
| 409 | Duplicate ISBN |

---

## 2. Get Book by ID

```http
GET /api/books/{id}
```

### Example

```bash
curl http://localhost:8080/api/books/1
```

### Error Responses

| Status | Condition |
|--------|-----------|
| 404 | Book not found |

---

## 3. Update Book

```http
PUT /api/books/{id}
Content-Type: application/json
```

### Request Body (all fields optional)

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `title` | String | max 500 chars | Book title |
| `author` | String | max 200 chars | Author name |
| `isbn` | String | max 20 chars | ISBN number |
| `genre` | String | max 50 chars | Book genre |
| `price` | BigDecimal | — | Price |
| `description` | String | max 2000 chars | Description |
| `stock` | Integer | — | Stock quantity |
| `pages` | Integer | — | Page count |
| `publisher` | String | max 50 chars | Publisher |
| `publishDate` | String | yyyy-MM-dd | Publication date |
| `active` | Boolean | — | Active status |

### Example

```bash
curl -X PUT http://localhost:8080/api/books/1 \
  -H "Content-Type: application/json" \
  -d '{ "title": "Clean Code (2nd Edition)", "price": 44.99 }'
```

### Error Responses

| Status | Condition |
|--------|-----------|
| 404 | Book not found |
| 409 | Duplicate ISBN (when changing ISBN to an existing one) |

---

## 4. Delete Book

```http
DELETE /api/books/{id}
```

### Example

```bash
curl -X DELETE http://localhost:8080/api/books/1
```

### Error Responses

| Status | Condition |
|--------|-----------|
| 404 | Book not found |
| 400 | Book has remaining stock (> 0) |

---

## 5. Search Books (Paginated)

```http
GET /api/books
```

### Query Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `keyword` | String | — | Search in title/author/description |
| `author` | String | — | Filter by author (partial match) |
| `genre` | String | — | Filter by genre (exact match) |
| `isbn` | String | — | Filter by ISBN (partial match) |
| `minPrice` | BigDecimal | — | Minimum price |
| `maxPrice` | BigDecimal | — | Maximum price |
| `active` | Boolean | — | Filter by active status |
| `sortBy` | String | `updateTime` | Sort field: title/author/price/stock/createTime/updateTime |
| `sortDirection` | String | `desc` | Sort direction: asc/desc |
| `page` | Integer | 0 | Page number (0-based) |
| `size` | Integer | 10 | Page size |

### Example

```bash
curl "http://localhost:8080/api/books?keyword=clean&genre=TECHNOLOGY&minPrice=20&maxPrice=50&active=true&sortBy=price&sortDirection=asc&page=0&size=10"
```

### Response

```json
{
  "code": 200,
  "data": {
    "content": [ { "id": 1, "title": "Clean Code", ... } ],
    "totalElements": 1,
    "totalPages": 1,
    "number": 0,
    "size": 10,
    "first": true,
    "last": true
  }
}
```

---

## 6. Get All Books

```http
GET /api/books/all
```

### Example

```bash
curl http://localhost:8080/api/books/all
```

---

## 7. Update Stock

```http
PATCH /api/books/{id}/stock?delta={delta}
```

| Parameter | Type | Description |
|-----------|------|-------------|
| `delta` | Integer | Stock change (positive to add, negative to remove) |

### Example

```bash
curl -X PATCH "http://localhost:8080/api/books/1/stock?delta=-5"
```

### Error Responses

| Status | Condition |
|--------|-----------|
| 404 | Book not found |
| 400 | Insufficient stock (result would be negative) |

---

## 8. Toggle Active Status

```http
PATCH /api/books/{id}/toggle
```

### Example

```bash
curl -X PATCH http://localhost:8080/api/books/1/toggle
```

---

## 9. Export Books

```http
GET /api/books/export?format={format}
```

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `format` | String | `csv` | Export format: `csv` or `json` |
| `keyword` | String | — | Filter by keyword |
| `author` | String | — | Filter by author |
| `genre` | String | — | Filter by genre |

### Example

```bash
# Export as CSV
curl http://localhost:8080/api/books/export?format=csv -o books.csv

# Export as JSON
curl "http://localhost:8080/api/books/export?format=json&genre=TECHNOLOGY" -o books.json
```

### Response

Returns a file download with `Content-Disposition: attachment; filename="books_export_*.csv"`.

---

## 10. AI Smart Search

```http
GET /api/books/ai-search?query={query}
```

| Parameter | Type | Description |
|-----------|------|-------------|
| `query` | String | Natural language search query |

### Example

```bash
curl "http://localhost:8080/api/books/ai-search?query=best programming practices for beginners"
```

### How It Works

- **Multi-field matching**: title, author, description, genre
- **Token-based**: query is split into tokens, each matched independently
- **Relevance scoring**:
  - Title exact match: +100, prefix: +80, contains: +50
  - Author exact match: +70, prefix: +50, contains: +30
  - Description contains: +10
  - Per-token bonus: title +20, author +15, description +5
- **Sorted** by relevance score descending

---

## Error Response Format

All errors return a unified JSON structure:

```json
{
  "code": 404,
  "message": "Book not found with id: 99",
  "timestamp": 1722823200000
}
```

## HTTP Status Codes

| Status | Description |
|--------|-------------|
| 200 OK | Request successful |
| 400 Bad Request | Validation error / invalid state |
| 404 Not Found | Book not found |
| 409 Conflict | Duplicate ISBN |
| 500 Internal Server Error | Unexpected error |
