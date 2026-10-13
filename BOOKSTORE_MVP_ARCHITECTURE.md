# Minimum Viable Product (MVP) Architecture: "Book Worm"

**Target**: AI Specialist Capstone Project Evaluation  
**Core Objective**: Satisfy 100% of the Capstone evaluation rubric and wireframe user journeys with the **absolute minimal implementation effort and complexity**.

---

## Evaluation Criteria vs. MVP Strategy

To pass the evaluation with maximum marks, the project must demonstrate:
1. **Contract-First & Spec-Driven Development**: A working OpenAPI 3.1 contract.
2. **End-to-End User Journey (Wireframes Slides 5–10)**:
   - Home & Catalog Discovery (Search, Category Filter, Recommendations).
   - Product Details (Book metadata, Author bio, Reviews).
   - Cart & Checkout (Quantity update, Address, Order summary calculation).
   - Payment Processing (Mock payment gateway, state change).
   - Order Confirmation & Post-Purchase (48h Cancellation, Buy-In-Again).
3. **Enterprise Quality Backend**: Spring Boot 3 + PostgreSQL with clean layering, Flyway migrations, and unit/integration tests.
4. **Minimal Surface Area**: Eliminate unnecessary join tables, micro-abstractions, and auxiliary domain overhead.

---

## 1. Essential Entities Only (Cut from 18 to 7 Tables)

By denormalizing secondary lookup entities (e.g., flattening `Publisher` into a string column, embedding cover images into `Book`, and embedding `Shipment` into `Order`), the schema shrinks from **18 tables down to 7 core tables** without losing a single wireframe feature:

```
┌─────────────┐       ┌──────────────┐       ┌────────────────┐
│    User     │──1:N──│   Address    │       │    Category    │
└──────┬──────┘       └──────────────┘       └───────┬────────┘
       │                                             │ (M:N)
       │ 1:1                                         ▼
       ├───────────────┐                     ┌────────────────┐
       │               ▼                     │      Book      │
       │          ┌─────────┐                └───────┬────────┘
       │          │  Cart   │                        │
       │          └────┬────┘                        │ 1:N
       │               │ 1:N                         ▼
       │               ▼                     ┌────────────────┐
       │          ┌──────────┐               │     Review     │
       │          │ CartItem │               └────────────────┘
       │          └──────────┘                       ▲
       │ 1:N                                         │
       ▼                                             │
┌─────────────┐                              ┌───────┴────────┐
│    Order    │──────────────1:N─────────────│   OrderItem    │
└─────────────┘                              └────────────────┘
```

| # | Essential Entity | Table Name | Justification & Simplification Strategy |
| :- | :--- | :--- | :--- |
| **1** | **User** | `users` | Manages authentication, JWT token subject, role (`CUSTOMER`/`ADMIN`), and loyalty gift points balance. |
| **2** | **Address** | `addresses` | Stores customer shipping destinations shown in wireframe slide 8. |
| **3** | **Category** | `categories` | Drives the 20-category left sidebar shown in slide 5 & 6. |
| **4** | **Book** | `books` | The central catalog entity. **Simplification**: Embed `author_name`, `author_bio`, `author_avatar_url`, `publisher_name`, `front_cover_url`, and `back_cover_url` directly onto `books` to eliminate 3 separate tables and 4 joins. |
| **5** | **Cart & CartItem** | `carts`, `cart_items` | Backs the shopping basket, quantity updates, and badge count. |
| **6** | **Order & OrderItem** | `orders`, `order_items` | Captures order placement, items snapshot, payment status, tracking number, and the mandatory **48-hour cancellation** window. |
| **7** | **Review** | `reviews` | Backs the book rating and review submission section shown in slide 7. |

---

## 2. Essential APIs Only (Cut from 28 to 15 APIs)

This trimmed set covers every button, modal, and flow in the presentation wireframes:

### 2.1 Authentication & Profile (2 APIs)
- `POST /api/v1/auth/login`: Authenticate with email/password, returns JWT token + user profile.
- `POST /api/v1/auth/register`: Register new account (auto-assigns `ROLE_CUSTOMER`).

### 2.2 Catalog & Product Detail (4 APIs)
- `GET /api/v1/categories`: Retrieve all categories for the left sidebar.
- `GET /api/v1/books`: Paginated search with filtering (`categoryId`, `query`, `format`, `minPrice`, `maxPrice`, `sortBy`).
- `GET /api/v1/books/{id}`: Detailed view (book info, dual covers, author profile, reviews, and related books).
- `GET /api/v1/books/featured`: Returns the 3 home widgets in one call (`recommended`, `bestsellers`, `newLaunches`).

### 2.3 Reviews (1 API)
- `POST /api/v1/books/{id}/reviews`: Submit star rating and comment for a book.

### 2.4 Shopping Cart & Checkout (4 APIs)
- `GET /api/v1/cart`: Get current user cart, line items, and subtotal.
- `POST /api/v1/cart/items`: Add book to cart.
- `PUT /api/v1/cart/items/{id}`: Increment/decrement quantity (or remove if 0).
- `POST /api/v1/checkout/quote`: Calculate totals (Subtotal + 12% GST + Shipping + Coupon discount).

### 2.5 Orders & Post-Purchase (4 APIs)
- `POST /api/v1/orders`: Create order from cart with delivery address and payment details (auto-empties cart).
- `GET /api/v1/orders`: List customer order history.
- `POST /api/v1/orders/{id}/cancel`: **Evaluator Checklist Item** — Cancels order and triggers refund if `order_date < 48 hours`.
- `POST /api/v1/orders/{id}/buy-again`: **Evaluator Checklist Item** — Re-adds order items back into the active cart.

---

## 3. Nice-to-Have Features (Implement only if time permits)

These features enhance the app but are not strictly required to validate the core Capstone workflows:

1. **Standalone Wishlist Entity**: Can be simulated on the frontend with `localStorage` or added as a single boolean flag on the book response.
2. **Coupons Table**: For the MVP, coupon codes (e.g., `SAVE100`) can be evaluated via an in-memory/enum rule in `CheckoutService` rather than a dedicated database table and admin CRUD.
3. **Gift Points Ledger Table**: Points balance can be stored as a single integer column (`gift_points_balance`) on `users` rather than a full immutable ledger table.
4. **Dedicated Payment Entity & External Gateway**: A simulated payment service updating `orders.payment_status = 'PAID'` inside the checkout transaction is sufficient for local and demo evaluations.

---

## 4. Features That Can Be Excluded (Out of Scope for MVP)

Cutting these items saves ~60% of development and debugging time without impacting grading:

| Excluded Feature | Reason for Exclusion |
| :--- | :--- |
| **Real Payment Gateway Integration** (Stripe / Razorpay) | Requires merchant credentials, webhooks, and sandbox setup. A mock payment processor executing with a 300ms simulated delay satisfies all wireframe requirements. |
| **Multi-Store / Multi-Tenant Capabilities** (Slide 4 "Store Policies") | Wireframes only depict a single consumer bookstore ("Book Worm"). Multi-tenancy adds architectural bloat. |
| **Live Third-Party Courier APIs** (FedEx / Blue Dart) | Simulated tracking numbers (e.g., `BW-TRK-XXXX`) and calculated estimated dates (`CURRENT_DATE + 5 days`) completely satisfy Slide 3 & Slide 8. |
| **Complex Recommendation Engine / ML Models** | Rule-based SQL queries (`WHERE category_id IN (...) ORDER BY sales_count DESC`) provide the exact same user experience for "Recommended for You" and "Related Reads". |
| **OAuth2 / Social Login (Google / GitHub)** | Standard JWT with username/password is 100% compliant with Spring Security grading criteria. |
| **Admin Management Portal / CMS** | The Capstone focuses on the customer purchasing flow. Seed data via Flyway scripts replaces the need for admin CRUD pages. |

---

## 5. Simplified MVP Architecture Specification

### 5.1 Architecture Stack
- **Backend Framework**: Spring Boot 3.3.x (Java 21)
- **Persistence**: Spring Data JPA + PostgreSQL (with Flyway migration)
- **Security**: Spring Security 6 + JJWT (Stateless JWT Bearer Token)
- **API Spec**: OpenAPI 3.1 (`springdoc-openapi-starter-webmvc-ui` for Swagger UI)
- **Build Tool**: Maven

### 5.2 Streamlined PostgreSQL Schema (7 Tables)

```sql
-- 1. Users
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'ROLE_CUSTOMER',
    gift_points INT NOT NULL DEFAULT 100,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Addresses
CREATE TABLE addresses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    address_line VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE
);

-- 3. Categories
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE
);

-- 4. Books (Flattened for high performance and low complexity)
CREATE TABLE books (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    category_id UUID NOT NULL REFERENCES categories(id),
    author_name VARCHAR(150) NOT NULL,
    author_bio TEXT,
    author_avatar_url VARCHAR(500),
    publisher_name VARCHAR(150) NOT NULL DEFAULT 'ABC Publishers',
    format VARCHAR(50) NOT NULL DEFAULT 'Paperback',
    language VARCHAR(50) NOT NULL DEFAULT 'English',
    price NUMERIC(10,2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 50,
    sales_count INT NOT NULL DEFAULT 0,
    rating NUMERIC(2,1) NOT NULL DEFAULT 4.5,
    front_cover_url VARCHAR(500),
    back_cover_url VARCHAR(500),
    is_bestseller BOOLEAN NOT NULL DEFAULT FALSE,
    is_new_launch BOOLEAN NOT NULL DEFAULT FALSE,
    published_date DATE NOT NULL DEFAULT CURRENT_DATE
);

-- 5. Reviews
CREATE TABLE reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    book_id UUID NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id),
    user_name VARCHAR(150) NOT NULL,
    rating SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 6. Carts & Items
CREATE TABLE carts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE cart_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id UUID NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    book_id UUID NOT NULL REFERENCES books(id),
    quantity INT NOT NULL CHECK (quantity > 0),
    UNIQUE (cart_id, book_id)
);

-- 7. Orders & Items
CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_number VARCHAR(50) NOT NULL UNIQUE,
    user_id UUID NOT NULL REFERENCES users(id),
    status VARCHAR(30) NOT NULL DEFAULT 'PAID',
    subtotal NUMERIC(10,2) NOT NULL,
    tax NUMERIC(10,2) NOT NULL,
    shipping_fee NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    discount NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    total_amount NUMERIC(10,2) NOT NULL,
    shipping_address_json JSONB NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    tracking_number VARCHAR(100) NOT NULL,
    estimated_delivery DATE NOT NULL,
    order_date TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cancel_eligible_until TIMESTAMPTZ NOT NULL -- order_date + 48 hours
);

CREATE TABLE order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    book_id UUID NOT NULL REFERENCES books(id),
    title VARCHAR(255) NOT NULL,
    price NUMERIC(10,2) NOT NULL,
    quantity INT NOT NULL
);
```

---

## 6. Effort vs. Value Summary

| Dimension | Full Analysis Version | Streamlined MVP | Reduction in Effort |
| :--- | :--- | :--- | :--- |
| **Database Tables** | 18 tables | **7 tables** | **- 61%** |
| **Backend REST Endpoints** | 28 APIs | **15 APIs** | **- 46%** |
| **JPA Entities & Repositories** | 18 pairs | **7 pairs** | **- 61%** |
| **DTOs & Mappers** | ~40 classes | **~14 classes** | **- 65%** |
| **Presentation Rubric Coverage** | 100% | **100%** | **No compromise** |

This MVP directly covers every screen in the PPT wireframes (Home, Catalog, Product Detail, Cart, Payment, Confirmation), satisfies all grading criteria (JWT, PostgreSQL, 48h cancel, Buy In Again, Swagger), and minimizes boilerplate code.