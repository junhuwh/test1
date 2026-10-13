# Capstone Project Implementation Review & Fast-Track Plan

**Objective**: Pass the AI Specialist Capstone Project evaluation with maximum marks while keeping coding and debugging effort strictly minimal.

---

## 1. API Implementation Classification

Based on the evaluation requirements from the slides (specifically the demo video, database persistence checks in Step 8, and the signature wireframe features):

### 1.1 APIs That MUST Be Fully Implemented (Real DB Persistence)
These 10 APIs represent the critical user journey that an evaluator or manager will test in the browser/Swagger and verify in PostgreSQL.

| # | Endpoint | Why Real DB Persistence Is Mandatory |
| :--- | :--- | :--- |
| 1 | `POST /api/v1/auth/register` | Verifies user creation and password hashing in the `users` table. |
| 2 | `POST /api/v1/auth/login` | Issues real JWT tokens required to unlock all subsequent authenticated APIs. |
| 3 | `GET /api/v1/categories` | Demonstrates reading seeded Flyway data (`categories` table) for sidebar navigation. |
| 4 | `GET /api/v1/books` | Demonstrates database search, genre filtering, and Spring Data pagination. |
| 5 | `GET /api/v1/books/{id}` | Loads complete product information and dual cover URLs from the database. |
| 6 | `GET /api/v1/cart` | Shows persisted basket state for the logged-in user. |
| 7 | `POST /api/v1/cart/items` | Adds items to `cart_items` in PostgreSQL and updates cart quantity badge. |
| 8 | `PUT /api/v1/cart/items/{id}` | Updates quantity or removes item (quantity = 0) in PostgreSQL. |
| 9 | `POST /api/v1/orders` | **Core Business Transaction**: Creates order & order items, saves shipping address snapshot, and clears the cart. |
| 10 | `GET /api/v1/orders` | Retrieves user's historical orders to display on the "My Orders" screen. |

---

### 1.2 APIs That Can Use Mock / Algorithmic Logic (No Heavy External Systems)
These APIs satisfy the wireframe behavior without requiring third-party integrations or dedicated micro-tables.

| # | Endpoint | Mock / Algorithmic Simplification Strategy |
| :--- | :--- | :--- |
| 11 | `POST /api/v1/checkout/quote` | **In-Memory Calculation**: Compute `subtotal` from active cart, apply `tax = subtotal * 0.12`, `shipping = (subtotal >= 499 ? 0 : 50)`, and `couponDiscount = (code.equals("SAVE100") ? 100 : 0)`. No need for external tax engine or coupon CRUD tables. |
| 12 | `GET /api/v1/books/featured` | **Database Query with Seed Data**: Implement by querying `findByIsBestsellerTrue()`, `findByIsNewLaunchTrue()`, and `findTop4ByOrderByRatingDesc()`. No machine learning model or external recommendation engine needed. |
| — | *Payment Gateway* (Inside `POST /api/v1/orders`) | **Mock Gateway**: Validate card number format, simulate a 200ms delay, and automatically mark `order.status = PAID`. Do not integrate real Stripe/Razorpay. |
| — | *Shipping Tracking* (Inside `POST /api/v1/orders`) | **Deterministic Calculation**: Auto-generate `trackingNumber = "BW-TRK-" + UUID` and set `estimatedDelivery = LocalDate.now().plusDays(5)`. |

---

### 1.3 Signature Capstone Differentiators (Must Implement for Top Marks)
These two features are explicitly highlighted in the Capstone requirements (Slides 3, 6, 10, 12). Implementing them takes under 20 lines of code each but guarantees full marks on business logic:

| # | Endpoint | Minimal Implementation Strategy |
| :--- | :--- | :--- |
| 13 | `POST /api/v1/orders/{id}/cancel` | **48-Hour Cancellation Rule**: Check `if (Duration.between(order.getOrderDate(), Instant.now()).toHours() < 48)`. If eligible, update status to `CANCELLED` and return refund amount. Otherwise throw `BusinessRuleViolationException` (HTTP 422). |
| 14 | `POST /api/v1/orders/{id}/buy-again` | **Re-Purchase Flow**: Query the historical order's `order_items`, iterate through them, and add each `book_id` with its quantity back into the user's active cart. |

---

### 1.4 Optional APIs (Lowest Priority)
| # | Endpoint | Priority & Recommendation |
| :--- | :--- | :--- |
| 15 | `POST /api/v1/books/{id}/reviews` | **Optional / Polish**: The detail page already renders seeded reviews. Implement this only after the core checkout flow is verified. |

---

## 2. Recommended Implementation Order (Fast-Track)

```
┌────────────────────────────────┐
│ Phase 1: Catalog & Data Read   │ ──> Seeded data instantly visible in Swagger
└───────────────┬────────────────┘
                ▼
┌────────────────────────────────┐
│ Phase 2: Auth & Token Flow     │ ──> Generates JWT token for testing
└───────────────┬────────────────┘
                ▼
┌────────────────────────────────┐
│ Phase 3: Cart Lifecycle        │ ──> Add to cart, view cart, adjust quantity
└───────────────┬────────────────┘
                ▼
┌────────────────────────────────┐
│ Phase 4: Checkout & Order Flow │ ──> Quote calculation & Atomic order placement
└───────────────┬────────────────┘
                ▼
┌────────────────────────────────┐
│ Phase 5: 48h Cancel & Re-Order │ ──> Signature Capstone requirements fulfilled
└───────────────┬────────────────┘
                ▼
┌────────────────────────────────┐
│ Phase 6: End-to-End Demo Video │ ──> Record Swagger/API flow for Manager review
└────────────────────────────────┘
```

---

## 3. Step-by-Step Minimum Effort Implementation Plan

### Step 1: Catalog Service & Controller Implementation (Read-Only)
- **Goal**: Allow browsing categories and books immediately.
- **Classes to Implement**:
  - `CatalogServiceImpl implements CatalogService`
  - `CatalogControllerImpl implements CatalogController`
- **Actions**:
  - Inject `CategoryRepository` and `BookRepository`.
  - Implement `getCategories()` using `categoryRepository.findAll()`.
  - Implement `getBooks(...)` using Spring Data Specification (or simple `findAll(Pageable)` if filters are omitted).
  - Implement `getBookById(id)` with related books query (`findTop4ByCategoryIdAndIdNot`).
  - Implement `getFeaturedBooks()` assembling lists from existing repository methods.

---

### Step 2: Auth Service & Controller Implementation
- **Goal**: Support user login and registration to obtain JWT Bearer tokens.
- **Classes to Implement**:
  - `AuthServiceImpl implements AuthService`
  - `AuthControllerImpl implements AuthController`
- **Actions**:
  - In `register()`: check `existsByEmail()`, encode password via `PasswordEncoder`, save `User`, auto-create an empty `Cart`, return `AuthResponse` with JWT.
  - In `login()`: find `User` by email, verify password via `passwordEncoder.matches()`, return `AuthResponse` with JWT.

---

### Step 3: Cart Service & Controller Implementation
- **Goal**: Persist shopping basket additions and quantity updates.
- **Classes to Implement**:
  - `CartServiceImpl implements CartService`
  - `CartControllerImpl implements CartController`
- **Actions**:
  - Helper to get or create active cart for `userId`.
  - In `addItemToCart()`: if item exists in `cart_items`, increment quantity; otherwise create new `CartItem`.
  - In `updateCartItemQuantity()`: if `quantity == 0`, delete the item; otherwise update quantity.
  - Map entities to `CartDto` with computed subtotal.

---

### Step 4: Checkout & Order Placement Implementation
- **Goal**: Complete the core purchasing journey.
- **Classes to Implement**:
  - `CheckoutServiceImpl implements CheckoutService`
  - `OrderServiceImpl implements OrderService`
  - `CheckoutControllerImpl implements CheckoutController`
  - `OrderControllerImpl implements OrderController`
- **Actions**:
  - `calculateQuote()`: compute 12% tax, flat shipping (free if >= 499), apply coupon `SAVE100` (-100).
  - `createOrder()`:
    1. Read user's cart (fail if empty).
    2. Compute final totals.
    3. Generate `orderNumber` (`"BW-" + System.currentTimeMillis()`) and `trackingNumber`.
    4. Set `cancelEligibleUntil = Instant.now().plus(48, ChronoUnit.HOURS)`.
    5. Convert cart items to `OrderItem` snapshots and save `Order`.
    6. Clear user cart.
    7. Return `OrderDto`.

---

### Step 5: Signature Evaluator Features (48h Cancel & Buy In Again)
- **Goal**: Satisfy the exact criteria evaluated in Slide 3 and Slide 12.
- **Actions inside `OrderServiceImpl`**:
  - `cancelOrder(userId, orderId)`:
    - Check if order belongs to user and status is not already `CANCELLED`.
    - Check `Instant.now().isBefore(order.getCancelEligibleUntil())`.
    - If valid: update `order.status = CANCELLED`, return `CancelOrderResponse` with `refundAmount = order.getTotalAmount()`.
    - If expired: throw `BusinessRuleViolationException("48-hour cancellation period has expired")`.
  - `buyAgain(userId, orderId)`:
    - Query `orderItemRepository.findByOrderId(orderId)`.
    - Loop through items and insert each book into user's cart.
    - Return `BuyAgainResponse` with updated cart.

---

### Step 6: Review Feature (Final Polish)
- **Classes to Implement**:
  - `ReviewServiceImpl implements ReviewService`
  - `ReviewControllerImpl implements ReviewController`
- **Actions**:
  - Save new `Review` linked to `bookId` and `currentUserId`.
  - Update book's aggregate rating and review count.

---

### Step 7: Local Database Run & Swagger Verification
- **Actions**:
  1. Start local PostgreSQL (or Docker PostgreSQL container: `docker run --name bookworm-pg -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=bookworm_db -p 5432:5432 -d postgres:16`).
  2. Start Spring Boot: `mvn spring-boot:run`. Flyway automatically creates the 7 tables and seeds books.
  3. Open Swagger UI at `http://localhost:8080/swagger-ui.html`.
  4. Execute the golden path in order:
     - `POST /api/v1/auth/login` -> copy JWT token.
     - Click **Authorize** button in Swagger and paste `Bearer <token>`.
     - `GET /api/v1/books` -> select book ID.
     - `POST /api/v1/cart/items` -> add book.
     - `POST /api/v1/checkout/quote` -> inspect pricing.
     - `POST /api/v1/orders` -> place order.
     - `POST /api/v1/orders/{id}/cancel` -> verify 48h cancellation & refund.
     - `POST /api/v1/orders/{id}/buy-again` -> verify cart re-population.

---

## Summary of Minimum Effort Strategy

| Total Service Classes Needed | Total Lines of Business Logic | Third-Party Dependencies Required |
| :---: | :---: | :---: |
| **6 ServiceImpl classes** | **~350 lines total** | **0 (pure Spring Data + JJWT)** |

By focusing solely on these steps, the entire backend becomes fully testable in Swagger and ready for the Capstone demonstration video in a single sitting.