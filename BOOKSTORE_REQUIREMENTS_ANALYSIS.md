# Architecture & Requirements Analysis: "Book Worm" eCommerce Platform

**Role**: Senior Solution Architect  
**Document Analyzed**: `AI Specialist - Cloud FullStack - Capstone instructions.pptx.pptx` (along with accompanying prompt specs and architecture blueprints)  
**Target Architecture**: Domain-Driven Design (DDD), Spring Boot 3.x (Java 21), PostgreSQL, RESTful API (OpenAPI 3.1)

---

## 1. Business Workflows

The "Book Worm" e-bookstore encompasses six end-to-end operational workflows:

```
                  +----------------------------------------------+
                  | 1. User Lifecycle (Guest / Registered)       |
                  +----------------------+-+---------------------+
                                         |
                                         v
+------------------------------------------------------------------------+
| 2. Catalog & Discovery (Search, Facets, Recs, Author & Book Details)  |
+-------------------+----------------------------------+-----------------+
                    |                                  |
                    v                                  v
+-------------------------------+     +----------------------------------+
| 3. Wishlist Workflow          |     | 4. Cart & Checkout Preparation   |
+-------------------------------+     |    (Cart, Address, Coupons, Tax) |
                                      +----------------+-----------------+
                                                       |
                                                       v
                                      +----------------------------------+
                                      | 5. Payment & Order Placement     |
                                      |    (Card/UPI/Wallet, Transact)   |
                                      +----------------+-----------------+
                                                       |
                                                       v
                                      +----------------------------------+
                                      | 6. Post-Purchase & Fulfillment   |
                                      |    (48h Cancel, Reorder, Review) |
                                      +----------------------------------+
```

### 1.1 User Lifecycle & Session Workflow
- **Guest Browsing**: Unauthenticated users can browse categories, search books, view book/author details, view reviews, and maintain an ephemeral session cart.
- **Authentication & RBAC**: Users register, login (JWT issuance), and obtain role-based access (`GUEST`, `CUSTOMER`, `ADMIN`).
- **Profile & Address Management**: Registered customers can manage personal profiles and save multiple delivery addresses with default selection capabilities.

### 1.2 Catalog Discovery & Exploration Workflow
- **Category Browsing**: Hierarchical navigation through 20+ genres (Fiction, Self-help, Science, etc.).
- **Multi-Faceted Search**: Query text search across title/author with facet filters:
  - *Format*: Paperback, Hardcover, eBook.
  - *Language*: English, regional/international languages.
  - *Price Range*: Min/max price buckets.
  - *Sort Criteria*: Relevance, price ascending/descending, publication date, customer rating.
- **Merchandising & Recommendations**:
  - *Home Page*: Dynamic widgets for "Recommended for You" (personalized based on order history), "Bestsellers this Month", and "New Launches".
  - *Product Detail*: Cross-sell/upsell widgets for "Related Reads".
- **Detail View**: Display dual cover imagery (front/back), metadata, publisher, author bio, stock availability, and calculated delivery estimate.

### 1.3 Wishlist Workflow
- Registered users can save items to "My Wishlist" directly from the book detail page.
- Direct conversion of wishlist items into cart items with automatic wishlist removal or retention.

### 1.4 Cart & Checkout Preparation Workflow
- **Cart Manipulation**: Add to cart, adjust quantities with real-time subtotal recalculation, and remove items.
- **Address Selection**: Choose an existing saved address or dynamically input a new delivery address (First/Last Name, Street, City, State, PIN, Phone, Country).
- **Promotion & Financial Totals Calculation**:
  - Calculate gross item total.
  - Validate and apply promotional coupons (fixed or percentage deduction).
  - Redeem available loyalty/gift points.
  - Compute applicable sales tax.
  - Determine shipping fee (e.g., Free shipping threshold).
  - Output immutable Checkout Summary.

### 1.5 Payment Processing & Order Placement Workflow
- **Payment Method Selection**: Credit Card, Debit Card, UPI, or Wallet.
- **Transaction Processing**:
  - Initiate idempotent payment intent.
  - Verify card metadata (PAN, Cardholder Name, Expiry MM/YYYY, CVV) or tokenized wallet/UPI handle.
  - Authorize transaction with payment gateway.
- **Atomic Order Creation**:
  - On successful authorization: transition Order state from `PENDING_PAYMENT` to `CONFIRMED`/`PAID`.
  - Decrement book stock inventory.
  - Create shipping fulfillment order with estimated delivery dates.
  - Clear customer cart and render Order Confirmation screen.

### 1.6 Post-Purchase, Fulfillment & Review Workflow
- **Order History & Reorder ("Buy In Again")**: Customers inspect past orders and re-add previously purchased book sets into the active cart with a single click.
- **48-Hour Order Cancellation**: Orders remain eligible for cancellation within 48 hours of placement if they have not entered the dispatched/shipped stage. Triggers automated refund processing.
- **Customer Ratings & Reviews**: Verified buyers submit a 1–5 star rating and text feedback for books, updating the book's aggregate rating score and sales counters.

---

## 2. Core Entities

Organized across six Bounded Contexts aligned with the architecture diagram:

| Bounded Context | Entity | Purpose |
| :--- | :--- | :--- |
| **Identity & Member** | `User` | Core account entity (credentials, contact info, status, role). |
| | `Role` | Security entitlements (`ROLE_CUSTOMER`, `ROLE_ADMIN`, `ROLE_GUEST`). |
| | `Address` | Customer shipping and billing address records. |
| **Catalog & Store** | `Book` | The primary sellable product (title, ISBN, description, price, stock, ratings). |
| | `BookCover` | Dual cover image assets (front and back cover URLs/metadata). |
| | `Author` | Book creator details, biography, avatar, and portfolio. |
| | `Publisher` | Publishing house metadata. |
| | `Category` | Genre classification hierarchy. |
| | `BookFormat` | Physical/digital edition definition (Paperback, Hardcover, eBook). |
| **Cart & Wishlist** | `Cart` | Customer shopping basket session. |
| | `CartItem` | Specific book format instance and quantity inside a cart. |
| | `Wishlist` | Saved-for-later collection for authenticated customers. |
| | `WishlistItem` | Reference to a book saved in the wishlist. |
| **Order & Sales** | `Order` | Finalized commercial contract containing state, totals, and timestamps. |
| | `OrderItem` | Snapshot of book, historical purchase price, and quantity at time of purchase. |
| | `Coupon` | Discount codes, validation rules, expiration, and discount values. |
| | `GiftPointLedger`| Loyalty points balance and redemption log. |
| **Payment** | `Payment` | Payment record bound to an order. |
| | `PaymentTransaction` | Individual transaction attempt, gateway reference, and status. |
| | `Refund` | Reversal transaction record (triggered on 48h cancellation). |
| **Shipping & Fulfillment** | `Shipment` | Delivery tracking, carrier details, and estimated delivery dates. |
| | `ShippingAddress` | Immutable snapshot of delivery address captured at order time. |

---

## 3. Relationships Between Entities

### 3.1 Entity Relationship Cardinality Table

| Parent Entity | Child Entity | Cardinality | Cascade / Lifecycle Behavior | Notes |
| :--- | :--- | :--- | :--- | :--- |
| `User` | `Role` | M:N | Shared Reference | Joint table `user_roles`. |
| `User` | `Address` | 1:N | Cascade ALL / Orphan Removal | One user can store multiple addresses; one marked default. |
| `User` | `Cart` | 1:1 | Cascade ALL | One active cart per user. |
| `User` | `Wishlist` | 1:1 | Cascade ALL | One wishlist per user. |
| `User` | `Order` | 1:N | Restrict Delete | Orders must persist even if user account is deactivated. |
| `User` | `Review` | 1:N | Restrict Delete | Customer reviews. |
| `Author` | `Book` | 1:N (or M:N) | Restrict Delete | Books linked to primary author (or co-authors). |
| `Publisher` | `Book` | 1:N | Restrict Delete | One publisher releases multiple books. |
| `Category` | `Book` | M:N | Shared Reference | Books can belong to multiple categories (e.g. Non-Fiction, Self-Help). |
| `Book` | `BookCover` | 1:N | Cascade ALL | Exactly 2 records standard: `FRONT` and `BACK`. |
| `Book` | `Review` | 1:N | Cascade ALL | All reviews belong to a target book. |
| `Cart` | `CartItem` | 1:N | Cascade ALL / Orphan Removal | Cart contains 0..N item lines. |
| `Wishlist` | `WishlistItem` | 1:N | Cascade ALL / Orphan Removal | Wishlist contains 0..N items. |
| `Order` | `OrderItem` | 1:N | Cascade ALL | Immutable snapshot of items upon checkout. |
| `Order` | `ShippingAddress`| 1:1 | Cascade ALL | Copied from `Address` to preserve historical delivery destination. |
| `Order` | `Payment` | 1:1 (or 1:N)| Cascade ALL | Supports payment retries if first attempt fails. |
| `Order` | `Shipment` | 1:1 | Cascade ALL | Tracks delivery milestone and carrier tracking code. |
| `Payment` | `Refund` | 1:N | Cascade ALL | Created when cancellation occurs within 48h. |
| `Coupon` | `Order` | 1:N | Restrict Delete | Optional applied coupon per order. |

---

## 4. Required Backend APIs

All APIs adhere to RESTful conventions, JSON payloads, and standard HTTP status codes (`200 OK`, `201 Created`, `204 No Content`, `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found`, `409 Conflict`, `422 Unprocessable Entity`).

### 4.1 Authentication & Member APIs (`/api/v1/auth`, `/api/v1/users`)
- `POST /api/v1/auth/register`: Register new user with name, email, password.
- `POST /api/v1/auth/login`: Authenticate and return JWT access/refresh tokens.
- `POST /api/v1/auth/logout`: Invalidate current user session token.
- `GET /api/v1/users/me`: Retrieve current profile, roles, and loyalty points.
- `GET /api/v1/users/me/addresses`: List saved delivery addresses.
- `POST /api/v1/users/me/addresses`: Create a new shipping address.
- `PUT /api/v1/users/me/addresses/{addressId}`: Update existing address.
- `DELETE /api/v1/users/me/addresses/{addressId}`: Soft-delete/remove address.
- `PATCH /api/v1/users/me/addresses/{addressId}/default`: Set default delivery address.

### 4.2 Catalog, Books & Author APIs (`/api/v1/books`, `/api/v1/authors`, `/api/v1/categories`)
- `GET /api/v1/categories`: List all categories/genres.
- `GET /api/v1/books`: Paginated search & filter books:
  - *Query parameters*: `query`, `categoryId`, `format`, `language`, `minPrice`, `maxPrice`, `sortBy`, `sortDirection`, `page`, `size`.
- `GET /api/v1/books/{bookId}`: Retrieve detailed book view (metadata, front/back cover, publisher, author preview, stock, aggregate rating).
- `GET /api/v1/books/recommendations`: Retrieve dynamic home widgets (`type=RECOMMENDED | BESTSELLER | NEW_LAUNCH`).
- `GET /api/v1/books/{bookId}/related`: Retrieve related books (up-sell & cross-sell recommendations).
- `GET /api/v1/authors/{authorId}`: Retrieve author profile, bio, image, and bibliography.
- `GET /api/v1/publishers`: Retrieve list of publishers.

### 4.3 Review & Rating APIs (`/api/v1/books/{bookId}/reviews`)
- `GET /api/v1/books/{bookId}/reviews`: Paginated list of customer reviews and ratings.
- `POST /api/v1/books/{bookId}/reviews`: Submit rating (1–5) and review comment (Authenticated customer only).

### 4.4 Wishlist APIs (`/api/v1/wishlist`)
- `GET /api/v1/wishlist`: Retrieve current customer's wishlist items.
- `POST /api/v1/wishlist/items`: Add book to wishlist.
- `DELETE /api/v1/wishlist/items/{bookId}`: Remove book from wishlist.
- `POST /api/v1/wishlist/items/{bookId}/move-to-cart`: Move item directly to cart.

### 4.5 Cart APIs (`/api/v1/cart`)
- `GET /api/v1/cart`: Retrieve current user's active cart with subtotal, tax, and item count.
- `POST /api/v1/cart/items`: Add book to cart (specifying book ID, format, quantity).
- `PUT /api/v1/cart/items/{itemId}`: Update item quantity.
- `DELETE /api/v1/cart/items/{itemId}`: Remove item from cart.
- `DELETE /api/v1/cart`: Clear all items in cart.

### 4.6 Checkout & Promotion APIs (`/api/v1/checkout`, `/api/v1/coupons`)
- `POST /api/v1/checkout/quote`: Compute order preview (items, address, shipping fee, tax, coupon discount, final grand total).
- `POST /api/v1/coupons/validate`: Validate coupon code against current cart value and return discount amount.

### 4.7 Order Management APIs (`/api/v1/orders`)
- `POST /api/v1/orders`: Create order from active cart, selected address, and coupon code.
- `GET /api/v1/orders`: Retrieve user's order history with pagination and status filters.
- `GET /api/v1/orders/{orderId}`: Get detailed order status, items, shipment tracking, and receipt.
- `POST /api/v1/orders/{orderId}/cancel`: Cancel order if placed within <= 48 hours (initiates refund).
- `POST /api/v1/orders/{orderId}/buy-again`: Re-add all items from historical order into current cart.

### 4.8 Payment APIs (`/api/v1/payments`)
- `POST /api/v1/payments/process`: Execute payment for a pending order (Method: `CREDIT_CARD`, `DEBIT_CARD`, `UPI`, `WALLET`).
- `GET /api/v1/payments/{paymentId}/status`: Query payment gateway transaction status.

### 4.9 Shipping & Tracking APIs (`/api/v1/shipments`)
- `GET /api/v1/shipments/estimate`: Calculate estimated delivery date given destination PIN/postal code.
- `GET /api/v1/orders/{orderId}/tracking`: Retrieve tracking milestones (Order Placed -> Packed -> Shipped -> Delivered).

---

## 5. Recommended Database Schema (PostgreSQL 3NF)

### 5.1 Global Design Standards
- **Primary Keys**: `UUID` generated via `gen_random_uuid()` (PostgreSQL 14+).
- **Audit Columns on Every Table**:
  - `created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP`
  - `created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM'`
  - `updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP`
  - `updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM'`
  - `version BIGINT NOT NULL DEFAULT 0` (for JPA `@Version` optimistic locking)
  - `is_deleted BOOLEAN NOT NULL DEFAULT FALSE` (for soft-deletes)
- **Monetary Values**: `NUMERIC(12, 2)` (never floating point).

### 5.2 Table Definitions & Specifications

#### 1. `users`
- `id`: `UUID PRIMARY KEY`
- `email`: `VARCHAR(255) NOT NULL UNIQUE`
- `password_hash`: `VARCHAR(255) NOT NULL`
- `first_name`: `VARCHAR(100) NOT NULL`
- `last_name`: `VARCHAR(100) NOT NULL`
- `phone_number`: `VARCHAR(20)`
- `status`: `VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'` (`ACTIVE`, `SUSPENDED`, `LOCKED`)
- `gift_points_balance`: `INT NOT NULL DEFAULT 0`
- *Standard Audit Columns*
- *Indexes*: `idx_users_email (email)`, `idx_users_status (status)`

#### 2. `roles` & `user_roles`
- `roles`: `id UUID PK`, `name VARCHAR(50) NOT NULL UNIQUE` (`ROLE_CUSTOMER`, `ROLE_ADMIN`, `ROLE_GUEST`)
- `user_roles`: `user_id UUID FK(users.id)`, `role_id UUID FK(roles.id)`, `PRIMARY KEY(user_id, role_id)`

#### 3. `addresses`
- `id`: `UUID PRIMARY KEY`
- `user_id`: `UUID NOT NULL FK(users.id)`
- `first_name`: `VARCHAR(100) NOT NULL`
- `last_name`: `VARCHAR(100) NOT NULL`
- `email`: `VARCHAR(255) NOT NULL`
- `phone_number`: `VARCHAR(20) NOT NULL`
- `address_line1`: `VARCHAR(255) NOT NULL`
- `address_line2`: `VARCHAR(255)`
- `city`: `VARCHAR(100) NOT NULL`
- `state`: `VARCHAR(100) NOT NULL`
- `postal_code`: `VARCHAR(20) NOT NULL`
- `country`: `VARCHAR(100) NOT NULL DEFAULT 'India'`
- `is_default`: `BOOLEAN NOT NULL DEFAULT FALSE`
- *Standard Audit Columns*
- *Indexes*: `idx_addresses_user_id (user_id)`

#### 4. `authors`
- `id`: `UUID PRIMARY KEY`
- `name`: `VARCHAR(255) NOT NULL`
- `biography`: `TEXT`
- `avatar_url`: `VARCHAR(1024)`
- *Standard Audit Columns*
- *Indexes*: `idx_authors_name (name)`

#### 5. `publishers`
- `id`: `UUID PRIMARY KEY`
- `name`: `VARCHAR(255) NOT NULL UNIQUE`
- `description`: `TEXT`
- *Standard Audit Columns*

#### 6. `categories`
- `id`: `UUID PRIMARY KEY`
- `name`: `VARCHAR(100) NOT NULL UNIQUE` (e.g. "Romance", "Self Help", "Science Fiction")
- `slug`: `VARCHAR(100) NOT NULL UNIQUE`
- `description`: `VARCHAR(500)`
- *Standard Audit Columns*
- *Indexes*: `idx_categories_slug (slug)`

#### 7. `books`
- `id`: `UUID PRIMARY KEY`
- `title`: `VARCHAR(255) NOT NULL`
- `isbn`: `VARCHAR(20) NOT NULL UNIQUE`
- `description`: `TEXT NOT NULL`
- `author_id`: `UUID NOT NULL FK(authors.id)`
- `publisher_id`: `UUID NOT NULL FK(publishers.id)`
- `language`: `VARCHAR(50) NOT NULL DEFAULT 'English'`
- `format`: `VARCHAR(50) NOT NULL` (`PAPERBACK`, `HARDCOVER`, `EBOOK`)
- `price`: `NUMERIC(12, 2) NOT NULL`
- `currency`: `VARCHAR(3) NOT NULL DEFAULT 'INR'`
- `stock_quantity`: `INT NOT NULL DEFAULT 0`
- `sales_count`: `INT NOT NULL DEFAULT 0`
- `average_rating`: `NUMERIC(3, 2) NOT NULL DEFAULT 0.00`
- `review_count`: `INT NOT NULL DEFAULT 0`
- `is_bestseller`: `BOOLEAN NOT NULL DEFAULT FALSE`
- `is_new_launch`: `BOOLEAN NOT NULL DEFAULT FALSE`
- `published_date`: `DATE NOT NULL`
- *Standard Audit Columns*
- *Indexes*: `idx_books_title (title)`, `idx_books_author (author_id)`, `idx_books_price (price)`, `idx_books_format (format)`, `idx_books_lang (language)`

#### 8. `book_categories`
- `book_id`: `UUID NOT NULL FK(books.id)`
- `category_id`: `UUID NOT NULL FK(categories.id)`
- `PRIMARY KEY (book_id, category_id)`

#### 9. `book_covers`
- `id`: `UUID PRIMARY KEY`
- `book_id`: `UUID NOT NULL FK(books.id)`
- `cover_type`: `VARCHAR(20) NOT NULL` (`FRONT`, `BACK`)
- `image_url`: `VARCHAR(1024) NOT NULL`
- *Standard Audit Columns*
- *Constraints*: `UNIQUE(book_id, cover_type)`

#### 10. `reviews`
- `id`: `UUID PRIMARY KEY`
- `book_id`: `UUID NOT NULL FK(books.id)`
- `user_id`: `UUID NOT NULL FK(users.id)`
- `rating`: `SMALLINT NOT NULL CHECK (rating >= 1 AND rating <= 5)`
- `title`: `VARCHAR(200)`
- `comment`: `TEXT NOT NULL`
- *Standard Audit Columns*
- *Indexes*: `idx_reviews_book_id (book_id)`, `idx_reviews_user_id (user_id)`

#### 11. `carts` & `cart_items`
- `carts`:
  - `id`: `UUID PRIMARY KEY`
  - `user_id`: `UUID UNIQUE FK(users.id)` (NULL for guest carts)
  - `session_token`: `VARCHAR(255) UNIQUE` (for guest identification)
  - *Standard Audit Columns*
- `cart_items`:
  - `id`: `UUID PRIMARY KEY`
  - `cart_id`: `UUID NOT NULL FK(carts.id) ON DELETE CASCADE`
  - `book_id`: `UUID NOT NULL FK(books.id)`
  - `quantity`: `INT NOT NULL CHECK (quantity > 0)`
  - `unit_price`: `NUMERIC(12, 2) NOT NULL`
  - *Standard Audit Columns*
  - *Constraints*: `UNIQUE(cart_id, book_id)`

#### 12. `wishlists` & `wishlist_items`
- `wishlists`: `id UUID PK`, `user_id UUID NOT NULL UNIQUE FK(users.id)`, *Standard Audit Columns*
- `wishlist_items`: `id UUID PK`, `wishlist_id UUID NOT NULL FK(wishlists.id)`, `book_id UUID NOT NULL FK(books.id)`, `PRIMARY KEY(wishlist_id, book_id)`

#### 13. `coupons`
- `id`: `UUID PRIMARY KEY`
- `code`: `VARCHAR(50) NOT NULL UNIQUE`
- `discount_type`: `VARCHAR(20) NOT NULL` (`FIXED_AMOUNT`, `PERCENTAGE`)
- `discount_value`: `NUMERIC(12, 2) NOT NULL`
- `min_order_amount`: `NUMERIC(12, 2) NOT NULL DEFAULT 0.00`
- `max_discount_amount`: `NUMERIC(12, 2)`
- `valid_from`: `TIMESTAMP WITH TIME ZONE NOT NULL`
- `valid_to`: `TIMESTAMP WITH TIME ZONE NOT NULL`
- `usage_limit`: `INT`
- `used_count`: `INT NOT NULL DEFAULT 0`
- *Standard Audit Columns*

#### 14. `orders`
- `id`: `UUID PRIMARY KEY`
- `order_number`: `VARCHAR(50) NOT NULL UNIQUE` (e.g. `BW-20261013-XXXX`)
- `user_id`: `UUID NOT NULL FK(users.id)`
- `status`: `VARCHAR(30) NOT NULL DEFAULT 'PENDING_PAYMENT'` (`PENDING_PAYMENT`, `PAID`, `PROCESSING`, `SHIPPED`, `DELIVERED`, `CANCELLED`, `REFUNDED`)
- `subtotal_amount`: `NUMERIC(12, 2) NOT NULL`
- `tax_amount`: `NUMERIC(12, 2) NOT NULL DEFAULT 0.00`
- `shipping_fee`: `NUMERIC(12, 2) NOT NULL DEFAULT 0.00`
- `discount_amount`: `NUMERIC(12, 2) NOT NULL DEFAULT 0.00`
- `gift_points_redeemed`: `INT NOT NULL DEFAULT 0`
- `gift_points_discount`: `NUMERIC(12, 2) NOT NULL DEFAULT 0.00`
- `total_amount`: `NUMERIC(12, 2) NOT NULL`
- `currency`: `VARCHAR(3) NOT NULL DEFAULT 'INR'`
- `coupon_id`: `UUID FK(coupons.id)`
- `order_date`: `TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP`
- `cancel_eligible_until`: `TIMESTAMP WITH TIME ZONE NOT NULL` (Computed as `order_date + INTERVAL '48 HOURS'`)
- *Standard Audit Columns*
- *Indexes*: `idx_orders_user_id (user_id)`, `idx_orders_status (status)`, `idx_orders_date (order_date)`

#### 15. `order_items`
- `id`: `UUID PRIMARY KEY`
- `order_id`: `UUID NOT NULL FK(orders.id) ON DELETE CASCADE`
- `book_id`: `UUID NOT NULL FK(books.id)`
- `book_title`: `VARCHAR(255) NOT NULL` (Historical snapshot)
- `book_author`: `VARCHAR(255) NOT NULL` (Historical snapshot)
- `book_format`: `VARCHAR(50) NOT NULL`
- `unit_price`: `NUMERIC(12, 2) NOT NULL`
- `quantity`: `INT NOT NULL CHECK (quantity > 0)`
- `total_price`: `NUMERIC(12, 2) NOT NULL`
- *Standard Audit Columns*
- *Indexes*: `idx_order_items_order_id (order_id)`

#### 16. `shipping_addresses` (Immutable snapshot)
- `id`: `UUID PRIMARY KEY`
- `order_id`: `UUID NOT NULL UNIQUE FK(orders.id)`
- `first_name`: `VARCHAR(100) NOT NULL`
- `last_name`: `VARCHAR(100) NOT NULL`
- `email`: `VARCHAR(255) NOT NULL`
- `phone_number`: `VARCHAR(20) NOT NULL`
- `address_line1`: `VARCHAR(255) NOT NULL`
- `address_line2`: `VARCHAR(255)`
- `city`: `VARCHAR(100) NOT NULL`
- `state`: `VARCHAR(100) NOT NULL`
- `postal_code`: `VARCHAR(20) NOT NULL`
- `country`: `VARCHAR(100) NOT NULL`
- *Standard Audit Columns*

#### 17. `payments` & `refunds`
- `payments`:
  - `id`: `UUID PRIMARY KEY`
  - `order_id`: `UUID NOT NULL FK(orders.id)`
  - `payment_method`: `VARCHAR(30) NOT NULL` (`CREDIT_CARD`, `DEBIT_CARD`, `UPI`, `WALLET`)
  - `payment_gateway`: `VARCHAR(50) NOT NULL DEFAULT 'MOCK_GATEWAY'`
  - `gateway_transaction_id`: `VARCHAR(100)`
  - `amount`: `NUMERIC(12, 2) NOT NULL`
  - `currency`: `VARCHAR(3) NOT NULL DEFAULT 'INR'`
  - `status`: `VARCHAR(30) NOT NULL DEFAULT 'INITIATED'` (`INITIATED`, `SUCCESS`, `FAILED`)
  - `card_last_four`: `VARCHAR(4)` (Masked, PCI-DSS compliant)
  - `card_brand`: `VARCHAR(20)`
  - `paid_at`: `TIMESTAMP WITH TIME ZONE`
  - *Standard Audit Columns*
- `refunds`:
  - `id`: `UUID PRIMARY KEY`
  - `payment_id`: `UUID NOT NULL FK(payments.id)`
  - `order_id`: `UUID NOT NULL FK(orders.id)`
  - `amount`: `NUMERIC(12, 2) NOT NULL`
  - `reason`: `VARCHAR(255) NOT NULL DEFAULT 'CUSTOMER_CANCELLED_WITHIN_48H'`
  - `status`: `VARCHAR(30) NOT NULL DEFAULT 'COMPLETED'` (`PENDING`, `COMPLETED`, `FAILED`)
  - *Standard Audit Columns*

#### 18. `shipments`
- `id`: `UUID PRIMARY KEY`
- `order_id`: `UUID NOT NULL UNIQUE FK(orders.id)`
- `tracking_number`: `VARCHAR(100) NOT NULL UNIQUE`
- `carrier`: `VARCHAR(100) NOT NULL DEFAULT 'Standard Delivery'`
- `status`: `VARCHAR(30) NOT NULL DEFAULT 'PROCESSING'` (`PROCESSING`, `DISPATCHED`, `IN_TRANSIT`, `DELIVERED`, `CANCELLED`)
- `estimated_delivery_date`: `DATE NOT NULL`
- `actual_delivery_date`: `DATE`
- *Standard Audit Columns*

---

## 6. Assumptions and Missing Requirements

### 6.1 Critical Business Logic Assumptions
1. **48-Hour Cancellation Boundary**:
   - *Assumption*: The 48-hour cancellation policy applies strictly from `orders.order_date` until `cancel_eligible_until`.
   - *Constraint*: If an order is already marked `SHIPPED` before 48 hours elapse, cancellation should either be disabled or converted into a Return Request workflow.
2. **"Buy In Again" Behavior**:
   - *Assumption*: When a user clicks "Buy In Again", the system checks current stock and active pricing for those items. If any book is out of stock, it should either skip that item or alert the user, rather than failing silently.
3. **Inventory Allocation Timing**:
   - *Assumption*: Inventory (`stock_quantity`) is reserved or deducted upon **payment authorization success**, not during cart additions, preventing denial-of-inventory attacks.
4. **Guest Cart to Authenticated Cart Merging**:
   - *Assumption*: When a guest adds items and logs in during checkout, the guest session cart is merged with any pre-existing user cart.

### 6.2 Missing Requirements & Technical Gaps Identified

| Area | Missing Item / Gap in Requirements | Architectural Recommendation |
| :--- | :--- | :--- |
| **Payment Gateway & PCI Compliance** | Wireframe shows direct card number/CVV inputs. Storing raw PAN/CVV violates PCI-DSS regulations. | Backend must **never** store PAN/CVV. Use tokenization/mock gateway handler that accepts tokenized payloads and persists only `card_last_four` and `card_brand`. |
| **Delivery Date Estimation** | Wireframes display explicit dates (e.g. "Delivery by Mon, 21 Jul"), but no calculation rule is provided. | Implement a deterministic business rule: `Estimated Date = CURRENT_DATE + 5 Business Days` (or configured per target postal code zone). |
| **Tax & Shipping Calculation** | Wireframe shows Tax = 62.00 on 508.00 (~12% GST) and Free Delivery. | Implement a configurable rule: GST rate = 12% on taxable subtotal; Free delivery when Subtotal >= 499, otherwise flat shipping fee of 50. |
| **Gift Points Valuation & Policy** | Slides mention "Redeem gift points" in the customer journey, but do not specify redemption ratio. | Assume `100 Gift Points = 10.00` discount, with maximum redemption capped at 20% of order subtotal. |
| **Idempotency & Concurrent Checkout** | High risk of duplicate charge if the user clicks "Pay Now" multiple times or under race conditions. | Introduce an `Idempotency-Key` header on `/api/v1/orders` and `/api/v1/payments/process` backed by database unique constraints or distributed locks. |
| **Refund Mechanics** | The deck states orders can be cancelled in 48h, but omits the financial refund path. | Automated refund event triggered immediately upon order cancellation; refund record linked directly to payment transaction. |
| **Multi-Tenancy / Store Policies** | Architecture Slide 4 mentions "Store: Create Stores, Create Catalogs, Create Store Policies", whereas wireframes depict a single bookstore ("Book Worm"). | Architect the data model with single-store defaults while keeping `store_id` extensible for multi-tenant support if needed later. |