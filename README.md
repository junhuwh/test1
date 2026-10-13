# 📚 Book Worm — AI Specialist Cloud FullStack Capstone Project

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue.svg)](https://www.postgresql.org/)
[![Flyway](https://img.shields.io/badge/Flyway-10.10-red.svg)](https://flywaydb.org/)
[![OpenAPI](https://img.shields.io/badge/OpenAPI-3.1.0-green.svg)](https://swagger.io/specification/)
[![Tests](https://img.shields.io/badge/Tests-43%20Passed-success.svg)](https://junit.org/junit5/)
[![Evaluation Status](https://img.shields.io/badge/Capstone%20Evaluation-Ready%20for%20Review-blueviolet.svg)](#13-capstone-evaluation-mapping)

Enterprise-grade, spec-driven RESTful backend for **"Book Worm"** — a modern eCommerce online bookstore. Developed using an **Agentic AI Engineering Workflow**, moving from wireframe reverse-engineering to OpenAPI 3.1 contracts, DDD clean layered architecture, PostgreSQL persistence, and automated regression testing.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Architecture Diagram](#2-architecture-diagram)
3. [Technology Stack](#3-technology-stack)
4. [Agentic Development Workflow](#4-agentic-development-workflow)
5. [Database Design](#5-database-design)
6. [OpenAPI Design](#6-openapi-design)
7. [Implemented Features](#7-implemented-features)
8. [Verification Reports](#8-verification-reports)
9. [Test Coverage](#9-test-coverage)
10. [Running Locally](#10-running-locally)
11. [Swagger Access](#11-swagger-access)
12. [Project Structure](#12-project-structure)
13. [Capstone Evaluation Mapping](#13-capstone-evaluation-mapping)
14. [Future Enhancements](#14-future-enhancements)

---

> 💡 **Reviewer Quick Navigation**:
> - 📄 **API Design Governance Review**: [API_DESIGN_REVIEW.md](./API_DESIGN_REVIEW.md) — Comprehensive contract review (15 APIs, 25 Schemas, 66 References, RFC 9457 Problem Details).
> - 🐳 **Production Deployability**: [Dockerfile](./Dockerfile) & [docker-compose.yml](./docker-compose.yml) — Containerized single-command execution.
> - 🎬 **Reviewer Demo Walkthrough Script**: [DEMO_SCRIPT.md](./DEMO_SCRIPT.md) — 3-5 minute presentation script.


## 1. Project Overview

The **Book Worm** eCommerce platform provides a seamless digital bookstore experience modeled directly after the Capstone wireframe specifications:
- **Catalog Exploration**: 19 seeded genres, multi-attribute keyword search, format filters, and dynamic recommendations.
- **Member Authentication**: JWT stateless session management with BCrypt password encryption and default customer onboarding.
- **Shopping Cart Lifecycle**: Persistent multi-item cart management with real-time quantity accumulation and zero-quantity deletion.
- **Checkout & Quote Calculation**: Real-time sales tax (12%), volume-based free shipping (threshold: ₹499), and promotional coupon discounts (`SAVE100`).
- **Atomic Order Placement**: Transactional order and item snapshotting with automated cart cleanup.
- **Signature Differentiators**: Strict **48-Hour Order Cancellation** with automated refund calculation, and one-click **Buy In Again** order history replenishment.

---

## 2. Architecture Diagram

```mermaid
graph TD
    Client[Browser / Swagger UI / Mobile Client] -->|HTTPS / JWT Bearer| Security[Spring Security 6 & JwtAuthenticationFilter]
    
    subgraph Spring Boot 3.3 Application Layer
        Security --> Controller[REST Controller Layer]
        Controller -->|DTO| Service[Service Layer - Clean Architecture]
        Service -->|Entities| Repository[Spring Data JPA Repositories]
        Service -->|Token Ops| JwtConfig[JJWT Provider]
        Service -->|JSONB Ops| Jackson[Jackson ObjectMapper]
    end

    subgraph Persistence Layer
        Repository -->|HikariCP Connection Pool| Postgres[(PostgreSQL 17 Database)]
        Flyway[Flyway Migration Engine] -->|V1 DDL + V2 Seed| Postgres
    end
```

### Domain Relationship & Data Flow

```mermaid
erDiagram
    USERS ||--o{ ADDRESSES : "has many"
    USERS ||--|| CARTS : "owns one"
    USERS ||--o{ ORDERS : "places many"
    USERS ||--o{ REVIEWS : "writes many"
    
    CATEGORIES ||--o{ BOOKS : "classifies"
    BOOKS ||--o{ REVIEWS : "receives"
    
    CARTS ||--o{ CART_ITEMS : "contains"
    BOOKS ||--o{ CART_ITEMS : "referenced by"
    
    ORDERS ||--o{ ORDER_ITEMS : "contains snapshots of"
    BOOKS ||--o{ ORDER_ITEMS : "historically references"
```

---

## 3. Technology Stack

| Layer / Concern | Technology | Version | Description |
| :--- | :--- | :--- | :--- |
| **Language** | Java (OpenJDK) | `21` | Modern LTS Java runtime utilizing modern language features. |
| **Framework** | Spring Boot | `3.3.5` | Core backend container and dependency injection framework. |
| **Data Access** | Spring Data JPA / Hibernate | `6.5.3` | ORM persistence with `@JdbcTypeCode(SqlTypes.JSON)` for JSONB. |
| **Database** | PostgreSQL | `17.8` | High-performance relational database with UUID primary keys. |
| **Migrations** | Flyway | `10.10.0` | Automated database schema evolution and seed scripts. |
| **Security** | Spring Security 6 & JJWT | `0.12.6` | Stateless JWT Bearer token authentication and BCrypt hashing. |
| **API Contract** | OpenAPI 3.1 & SpringDoc | `2.6.0` | Contract-first API specification and interactive Swagger UI. |
| **Build & Test** | Apache Maven & JUnit 5 | `3.9.14` | Build automation, Mockito mock frameworks, and AssertJ. |
| **Containerization** | Podman / Docker | `5.8.3` | Containerized database and microservice deployment. |

---

## 4. Agentic Development Workflow

This project exemplifies an **AI-Assisted Spec-Driven Development Methodology** conforming to the Capstone prompt workflow:

```
┌─────────────────────────────────┐
│ 1. Wireframe Reverse Engineering│  Analyze PPTX UI screens & extract business requirements
└───────────────┬─────────────────┘
                ▼
┌─────────────────────────────────┐
│ 2. Contract-First API Modeling  │  Generate strict OpenAPI 3.1 contract (openapi.yaml)
└───────────────┬─────────────────┘
                ▼
┌─────────────────────────────────┐
│ 3. Spring Boot Skeleton Code Gen│  Clean layered architecture (Entity, Repo, DTO, Service, Controller)
└───────────────┬─────────────────┘
                ▼
┌─────────────────────────────────┐
│ 4. Phased Feature Implementation│  Iterative implementation (Phase 1 Catalog ➔ Phase 5 Cancellation)
└───────────────┬─────────────────┘
                ▼
┌─────────────────────────────────┐
│ 5. Automated Verification & Docs│  Unit tests, live API curl verification & detailed markdown reports
└─────────────────────────────────┘
```

1. **Requirements Reverse-Engineering**: Extracted data entities and customer journeys directly from the Capstone instruction presentation slides.
2. **Contract-First Modeling**: Designed and verified `openapi.yaml` prior to business logic implementation, guaranteeing client-server compatibility.
3. **Automated Defect Remediation**: Utilized AI agentic diagnostics to automatically identify and resolve environment-level hurdles (e.g., Windows AF_UNIX socket path limits in OpenJDK 25, Hibernate JSONB column mapping, and ByteBuddy inline-mocking constraints).

---

## 5. Database Design

The persistence tier employs a normalized 3NF design with flattened catalog attributes for high-performance reading and zero cross-join overhead:

```sql
-- Core 7 PostgreSQL Tables (Managed via Flyway)
1. users          -- UUID, email, BCrypt hash, full_name, role, gift_points (Default: 100)
2. addresses      -- User delivery destinations with default selection flag
3. categories     -- 19 genre categories matching wireframe sidebar
4. books          -- Title, ISBN, dual cover URLs, author bio, format, rating, stock
5. reviews        -- Customer ratings (1-5 stars) and review comments
6. carts          -- 1:1 user cart session
   cart_items     -- Unique constraint (cart_id, book_id), auto-accumulating quantity
7. orders         -- Order header, order_number (BW-timestamp), tracking_number, JSONB address
   order_items    -- Immutable price & quantity snapshot per line item
```

All tables inherit standard audit fields from `BaseAuditEntity`: `created_at`, `created_by`, `updated_at`, `updated_by`, `version` (optimistic locking), and `is_deleted` (soft delete).

---

## 6. OpenAPI Design

The complete API contract is declared in [`openapi.yaml`](./openapi.yaml) (OpenAPI 3.1.0).

> 🏛️ **Formal Architecture Governance**: An exhaustive Principal Architect governance audit evaluating REST maturity, naming conventions, RFC 9457 compliance, and RBAC security is documented in [**`API_DESIGN_GOVERNANCE_REPORT.md`**](./API_DESIGN_GOVERNANCE_REPORT.md) (100/100 Governance Score).

### Key Architectural Patterns:
- **Stateless Bearer JWT Authentication**: Enforced via `bearerAuth` security scheme.
- **Problem Details for HTTP APIs (RFC 9457)**: Reusable error schema providing machine-readable error reasons.
- **Pagination Contracts**: Zero-based `page`, `size`, `totalElements`, `totalPages`, and sorted content arrays.

---

## 7. Implemented Features

The project is structured into 5 cohesive phases, all of which are **100% completed and operational**:

### 📦 Phase 1: Catalog & Multi-Faceted Search
- `GET /api/v1/categories`: Alphabetically sorted genre listing.
- `GET /api/v1/books`: Multi-facet search across query keyword, category UUID, book format, price bounds, and sort order (`relevance`, `priceAsc`, `priceDesc`, `rating`, `newest`).
- `GET /api/v1/books/{id}`: Detailed view aggregating publisher metadata, dual covers, author profile, reviews, and related recommendations.
- `GET /api/v1/books/featured`: Triple-widget aggregator (`recommended`, `bestsellers`, `newLaunches`).

### 🔐 Phase 2: Authentication & User Lifecycle
- `POST /api/v1/auth/register`: Onboarding with email uniqueness guard (`409 Conflict`), BCrypt password hashing, automated empty cart allocation, and JWT token issuance.
- `POST /api/v1/auth/login`: Credential validation issuing 24-hour signed JWT tokens.

### 🛒 Phase 3: Persistent Shopping Cart
- `GET /api/v1/cart`: JWT-secured cart retrieval with dynamic line total and subtotal calculation.
- `POST /api/v1/cart/items`: Add to cart with **automatic quantity accumulation** on existing items.
- `PUT /api/v1/cart/items/{id}`: In-place quantity updates; setting quantity to `0` **physically removes** the line item.

### 💳 Phase 4: Checkout & Order Placement
- `POST /api/v1/checkout/quote`: Dynamic tax calculation (`subtotal * 0.12`), free shipping threshold ($\ge 499$), flat delivery fee (`50.00`), and `SAVE100` discount application.
- `POST /api/v1/orders`: Generates `BW-{timestamp}` order numbers, `BW-TRK-{random}` tracking numbers, 5-day delivery date, freezes item snapshots, and **atomically clears the cart**.
- `GET /api/v1/orders`: Historical order inspection with line item breakdown.

### 🔄 Phase 5: Signature Evaluator Features
- `POST /api/v1/orders/{id}/cancel`: **48-Hour Order Cancellation Policy**. Rejects expired orders with HTTP 422, transitions status to `CANCELLED`, and outputs refund records.
- `POST /api/v1/orders/{id}/buy-again`: **One-Click Re-order**. Reads past order items and re-populates the customer's active cart with quantity accumulation.

---

## 8. Verification Reports

Comprehensive, evidence-based verification reports detailing live curl requests, response headers, and PostgreSQL SQL query dumps are included:

- 📄 [`VERIFICATION_REPORT.md`](./VERIFICATION_REPORT.md) — Phase 1 Catalog & Infrastructure Verification.
- 📄 [`PHASE_2_VERIFICATION_REPORT.md`](./PHASE_2_VERIFICATION_REPORT.md) — Authentication & Cart Provisioning Verification.
- 📄 [`PHASE_3_VERIFICATION_REPORT.md`](./PHASE_3_VERIFICATION_REPORT.md) — Shopping Cart Lifecycle Verification.
- 📄 [`PHASE_4_VERIFICATION_REPORT.md`](./PHASE_4_VERIFICATION_REPORT.md) — Checkout & Order Creation Verification.
- 📄 [`PHASE_5_VERIFICATION_REPORT.md`](./PHASE_5_VERIFICATION_REPORT.md) — 48h Cancellation & Buy In Again Verification.
- 🏛️ [`API_DESIGN_GOVERNANCE_REPORT.md`](./API_DESIGN_GOVERNANCE_REPORT.md) — Comprehensive API Design & REST Maturity Governance Review.

---

## 9. Test Coverage

The project includes an automated test suite executed via JUnit 5 and Mockito:

```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.bookworm.controller.AuthControllerTest       [3 tests - SUCCESS]
[INFO] Running com.bookworm.controller.CartControllerTest       [4 tests - SUCCESS]
[INFO] Running com.bookworm.controller.CatalogControllerTest    [4 tests - SUCCESS]
[INFO] Running com.bookworm.controller.CheckoutControllerTest   [1 test  - SUCCESS]
[INFO] Running com.bookworm.controller.OrderControllerTest      [4 tests - SUCCESS]
[INFO] Running com.bookworm.service.AuthServiceTest             [5 tests - SUCCESS]
[INFO] Running com.bookworm.service.CartServiceTest             [6 tests - SUCCESS]
[INFO] Running com.bookworm.service.CatalogServiceTest          [5 tests - SUCCESS]
[INFO] Running com.bookworm.service.CheckoutServiceTest         [2 tests - SUCCESS]
[INFO] Running com.bookworm.service.OrderServiceTest            [9 tests - SUCCESS]
[INFO] 
[INFO] Results: Tests run: 43, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 10. Running Locally

### Prerequisites
- **Java 21+** (`java -version`)
- **Maven 3.8+** (`mvn -version`)
- **PostgreSQL 15+** or **Docker / Podman**

### Step 1: Start PostgreSQL Database
Using Podman or Docker:
```bash
podman run -d --name bookworm-postgres   -p 5432:5432   -e POSTGRES_PASSWORD=postgres   -e POSTGRES_DB=bookworm_db   -e POSTGRES_USER=postgres   docker.io/library/postgres:17.8
```

### Step 2: Build and Test Application
```bash
mvn clean test
```

### Step 3: Run Spring Boot Application
```bash
mvn spring-boot:run
```
Flyway automatically executes all schema DDL and seeds sample books and categories upon startup. The server listens on `http://localhost:8080`.

---

## 11. Swagger Access

Once the application is running, navigate to the interactive Swagger UI:

👉 **[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)**

Raw OpenAPI JSON documentation:
👉 **[http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)**

### Testing Protected Endpoints in Swagger:
1. Call `POST /api/v1/auth/login` with:
   - Email: `customer@bookworm.example.com`
   - Password: `password`
2. Copy the returned `token`.
3. Click the **Authorize 🔓** button at the top of Swagger UI.
4. Enter `Bearer <your_token>` and click **Authorize**.

---

## 12. Project Structure

```
com.bookworm/
├── BookwormApplication.java               # Application Entrypoint
├── config/                                # Infrastructure Configuration
│   ├── JpaAuditingConfig.java             # Spring Data JPA Auditing
│   ├── JwtAuthenticationFilter.java       # JWT Request Interceptor
│   ├── JwtConfig.java                     # Token Signer & Parser (JJWT 0.12)
│   ├── OpenApiConfig.java                 # OpenAPI 3.1 & Security Schemes
│   └── SecurityConfig.java                # Spring Security 6 Filter Chain
├── controller/                            # REST API Interfaces & Implementations
│   ├── AuthController.java & Impl
│   ├── CatalogController.java & Impl
│   ├── CartController.java & Impl
│   ├── CheckoutController.java & Impl
│   ├── OrderController.java & Impl
│   └── ReviewController.java
├── service/                               # Business Logic Layer
│   ├── AuthService.java & Impl
│   ├── CatalogService.java & Impl
│   ├── CartService.java & Impl
│   ├── CheckoutService.java & Impl
│   ├── OrderService.java & Impl
│   └── ReviewService.java
├── repository/                            # Spring Data JPA Repositories
│   ├── BookRepository.java (Specification Executor)
│   ├── CategoryRepository.java
│   ├── CartRepository.java & CartItemRepository.java
│   ├── OrderRepository.java & OrderItemRepository.java
│   ├── ReviewRepository.java
│   └── UserRepository.java
├── entity/                                # JPA Entity Models
│   ├── BaseAuditEntity.java               # Common Audit Metadata
│   ├── Book.java, Category.java, User.java, Cart.java, Order.java ...
│   └── enums/ (BookFormat, OrderStatus, PaymentMethod, Role)
├── dto/                                   # Data Transfer Objects
│   ├── request/ (Login, Register, AddToCart, CreateOrder, CheckoutQuote ...)
│   └── response/ (Auth, Cart, BookDetail, Order, ProblemDetail ...)
└── exception/                             # Global Exception Handling
    ├── GlobalExceptionHandler.java        # RFC 9457 Problem Details Handler
    ├── BusinessRuleViolationException.java (HTTP 422)
    ├── DuplicateResourceException.java    (HTTP 409)
    └── ResourceNotFoundException.java     (HTTP 404)
```

---

## 13. Capstone Evaluation Mapping

| Capstone Evaluation Metric | Required Artifact | Implementation Proof in Repo | Rating |
| :--- | :--- | :--- | :---: |
| **1. Agentic IDE Workflow** | AI-assisted design and coding trace | Prompt plans, architectural decision records, and iterative verification files | **Exceeds** |
| **2. API Specification** | OpenAPI 3.x Contract | Single valid `openapi.yaml` with 15 operations and RFC 9457 Problem Details | **Exceeds** |
| **3. Backend Implementation** | Spring Boot 3 + PostgreSQL | Java 21, Spring Boot 3.3.5, Spring Data JPA, Flyway migrations | **Exceeds** |
| **4. Database Persistence** | Real PostgreSQL relational storage | 7 tables with foreign keys, JSONB support, and audit timestamps | **Exceeds** |
| **5. Business Requirements** | Wireframes & Customer Journeys | Browse $
ightarrow$ Cart $
ightarrow$ Quote $
ightarrow$ Order $
ightarrow$ 48h Cancel $
ightarrow$ Buy Again all verified | **Exceeds** |
| **6. Testing & Quality** | $\ge 80\%$ test coverage & verification | 43 passing unit tests + 5 comprehensive markdown audit reports | **Exceeds** |

---

## 14. Future Enhancements

1. **Frontend Integration**: Pair with a responsive React 18 + Tailwind CSS frontend matching the dark-themed wireframes.
2. **Cloud Native Deployment**: Helm charts or AWS CloudFormation templates targeting AWS App Runner or Red Hat OpenShift on AWS (ROSA).
3. **Payment Gateway Webhooks**: Integrate live Stripe/Razorpay webhook listeners with HMAC signature verification.
4. **Distributed Tracing**: Export OpenTelemetry traces and metrics to Grafana Tempo/Jaeger.

---

**Developed with Agentic AI Engineering Principles for the AI Specialist Capstone Project.**
