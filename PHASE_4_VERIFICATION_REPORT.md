# Phase 4 Verification Report

**Project**: Book Worm eCommerce Backend  
**Phase**: Phase 4 - Checkout & Order Placement  
**Date**: 2026-10-13  
**Status**: ✅ **PASSED**

---

## 1. Executive Summary

Phase 4 (Checkout & Order Placement) has been implemented, tested, and validated against the local PostgreSQL database instance and live Spring Boot runtime.

### Key Deliverables Verified:
- `POST /api/v1/checkout/quote`: Real-time checkout quote calculation based on the user's active shopping cart, with dynamic subtotal aggregation, 12% GST computation, shipping cost evaluation, and `SAVE100` discount deduction.
- `POST /api/v1/orders`: End-to-end atomic order creation generating unique `BW-{timestamp}` order numbers, `BW-TRK-{random}` tracking numbers, calculating 5-day delivery estimates, snapshotting order items and shipping addresses, and automatically purging the user's cart upon success.
- `GET /api/v1/orders`: Historical order inspection retrieving paginated/ordered past purchases with line items and delivery status.
- **Checkout Calculation**: Verified dynamically against active cart items.
- **Tax Calculation**: Verified at exactly 12% of subtotal (`RoundingMode.HALF_UP`).
- **Shipping Rule**: Verified; free shipping applied for subtotal $\ge 499$, flat 50.00 charged otherwise.
- **Coupon Rule**: Verified; `SAVE100` applies 100.00 discount.
- **Order Persistence**: Verified; records saved to PostgreSQL `orders` table.
- **Order Item Persistence**: Verified; item pricing and quantities snapshotted to `order_items` table.
- **Cart Cleanup**: Verified; `cart_items` table physically cleared post-purchase.
- **Unit Tests Passed**: 35 / 35 tests passed across all modules (0 failures, 0 errors, 0 skipped).
- **Live API Verification Passed**: Verified via live HTTP calls against `http://localhost:8080`.

**Overall Result**: ✅ **PASSED**

---

## 2. Scope Implemented

### 2.1 Checkout Quote (`POST /api/v1/checkout/quote`)
- **Dynamic Subtotal Calculation**: Reads active cart from PostgreSQL and calculates `subtotal = sum(unit_price * quantity)`.
- **12% Tax Calculation**: Computes sales tax via `subtotal * 0.12` rounded to 2 decimal places.
- **Free Shipping Threshold ($\ge 499$)**: Free delivery applied when subtotal $\ge 499.00$; standard `50.00` fee applied when below threshold.
- **`SAVE100` Coupon Support**: Deducts `100.00` from gross payable total (capped at subtotal).
- **Total Payable Amount**: Computes $\max(0, 	ext{subtotal} + 	ext{tax} + 	ext{shipping} - 	ext{discount})$.

### 2.2 Create Order (`POST /api/v1/orders`)
- **Cart Validation**: Enforces that the cart contains items; rejects empty carts with `BusinessRuleViolationException` (HTTP 422).
- **Order Number Generation**: Deterministically generates `"BW-" + System.currentTimeMillis()`.
- **Tracking Number Generation**: Generates standard `"BW-TRK-" + random(100000, 999999)`.
- **Estimated Delivery Calculation**: Sets estimated arrival to `LocalDate.now().plusDays(5)`.
- **48-Hour Cancellation Window**: Precomputes `cancelEligibleUntil = Instant.now().plus(48, ChronoUnit.HOURS)`.
- **Order Persistence**: Commits order header with JSONB address snapshot to `orders` table.
- **Order Item Snapshot Persistence**: Deep-copies cart line items into `order_items` rows to freeze historical purchase prices.
- **Cart Cleanup**: Automatically invokes `cartItemRepository.deleteByCartId()` to reset the user's cart.

### 2.3 Order History (`GET /api/v1/orders`)
- **Retrieve Historical Orders**: Queries `orderRepository.findByUserIdOrderByOrderDateDesc(userId)`.
- **Return Order Items**: Attaches snapshotted `OrderItemDto` records.
- **Return Shipping Information**: Deserializes JSONB shipping address into `OrderAddressDto`.
- **Return Order Status**: Exposes lifecycle state (`PAID`, `PROCESSING`, etc.).

---

## 3. Components Implemented

### 3.1 `CheckoutServiceImpl`
- **Location**: `src/main/java/com/bookworm/service/impl/CheckoutServiceImpl.java`
- **Responsibilities**:
  - Aggregates cart item values from `CartRepository`.
  - Calculates dynamic tax (`12%`), delivery tiers (`0.00` vs `50.00`), and promotional coupon deductions (`SAVE100`).
  - Returns `CheckoutQuoteResponse`.

### 3.2 `OrderServiceImpl`
- **Location**: `src/main/java/com/bookworm/service/impl/OrderServiceImpl.java`
- **Responsibilities**:
  - Validates cart contents and user existence.
  - Generates unique order numbers, tracking numbers, and delivery dates.
  - Persists `Order` and `OrderItem` records within an atomic `@Transactional` boundary.
  - Clears `cart_items` entries.
  - Queries historical orders and reconstructs `OrderDto` lists.

### 3.3 `CheckoutControllerImpl`
- **Location**: `src/main/java/com/bookworm/controller/impl/CheckoutControllerImpl.java`
- **Responsibilities**:
  - Exposes secured `POST /api/v1/checkout/quote`.
  - Resolves user identity from JWT `SecurityContextHolder`.

### 3.4 `OrderControllerImpl`
- **Location**: `src/main/java/com/bookworm/controller/impl/OrderControllerImpl.java`
- **Responsibilities**:
  - Exposes secured `POST /api/v1/orders` (returns HTTP `201 Created`).
  - Exposes secured `GET /api/v1/orders` (returns HTTP `200 OK`).

---

## 4. Unit Test Evidence

### 4.1 Test Suites Executed
- **`CheckoutServiceTest` (2 tests)**:
  - `testCalculateQuote_FreeShippingAndCoupon`: Validates subtotal $\ge 499$ trigger, 12% tax, and 100 coupon deduction.
  - `testCalculateQuote_StandardShipping`: Validates standard 50 shipping fee when subtotal $< 499$.
- **`OrderServiceTest` (3 tests)**:
  - `testCreateOrder_Success`: Verifies order creation, `BW-` order number, `BW-TRK-` tracking number, delivery date, item snapshots, and cart cleanup.
  - `testCreateOrder_EmptyCart_ThrowsException`: Verifies empty cart rejection (HTTP 422).
  - `testGetOrderHistory_Success`: Verifies order history retrieval.
- **`CheckoutControllerTest` (1 test)**:
  - `testCalculateQuote_Success`: Confirms HTTP 200 response.
- **`OrderControllerTest` (2 tests)**:
  - `testCreateOrder_Success`: Confirms HTTP 201 response.
  - `testGetOrderHistory_Success`: Confirms HTTP 200 history response.
- **Regression Suites**: `AuthControllerTest` (3), `CartControllerTest` (4), `CatalogControllerTest` (4), `AuthServiceTest` (5), `CartServiceTest` (6), `CatalogServiceTest` (5).

### 4.2 Maven Test Output

```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.bookworm.controller.AuthControllerTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.182 s
[INFO] Running com.bookworm.controller.CartControllerTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.087 s
[INFO] Running com.bookworm.controller.CatalogControllerTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.072 s
[INFO] Running com.bookworm.controller.CheckoutControllerTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.032 s
[INFO] Running com.bookworm.controller.OrderControllerTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.037 s
[INFO] Running com.bookworm.service.AuthServiceTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.856 s
[INFO] Running com.bookworm.service.CartServiceTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.187 s
[INFO] Running com.bookworm.service.CatalogServiceTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.164 s
[INFO] Running com.bookworm.service.CheckoutServiceTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.009 s
[INFO] Running com.bookworm.service.OrderServiceTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.191 s
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 35, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

---

## 5. Live API Verification

Live API testing was conducted against the local server (`http://localhost:8080`) backed by PostgreSQL, authenticated as `charlie@bookworm.example.com`.

### 5.1 Checkout Quote Verification (`POST /api/v1/checkout/quote`)

**Cart Content**: 1 *Joy of Minimalism* (149.00) + 2 *The Path to Success* (359.00 * 2 = 718.00) = Subtotal 867.00  
**Request Payload**: `{"couponCode": "SAVE100"}`  
**Actual Response**: `HTTP 200 OK`
```json
{
  "itemCount": 3,
  "subtotal": 867.0,
  "tax": 104.04,
  "deliveryCharges": 0.0,
  "couponDiscount": 100.0,
  "giftPointsDiscount": 0.0,
  "totalAmount": 871.04
}
```
- Subtotal: `867.00`
- Tax (12% of 867.00): `104.04`
- Shipping (Subtotal $\ge 499$): `0.00` (Free Shipping)
- Coupon Discount (`SAVE100`): `-100.00`
- Payable Total: `871.04`  
**Status**: ✅ **PASSED**

---

### 5.2 Create Order Verification (`POST /api/v1/orders`)

**Request Payload**:
```json
{
  "shippingAddress": {
    "firstName": "Charlie",
    "lastName": "Brown",
    "addressLine": "123 Main Street, Apt 4B",
    "city": "Mumbai",
    "state": "Maharashtra",
    "postalCode": "400001",
    "phone": "+91 9876543210"
  },
  "couponCode": "SAVE100",
  "paymentMethod": "CREDIT_CARD",
  "paymentDetails": {
    "cardNumber": "4111222233334444",
    "nameOnCard": "Charlie Brown",
    "expiryDate": "12/28",
    "cvv": "123"
  }
}
```

**Actual Response**: `HTTP 201 Created`
```json
{
  "id": "5b653292-d2aa-4e7d-8ae6-842c719c5f80",
  "orderNumber": "BW-1791844635976",
  "status": "PAID",
  "subtotal": 867.0,
  "tax": 104.04,
  "shippingFee": 0.0,
  "discount": 100.0,
  "totalAmount": 871.04,
  "shippingAddress": {
    "firstName": "Charlie",
    "lastName": "Brown",
    "addressLine": "123 Main Street, Apt 4B",
    "city": "Mumbai",
    "state": "Maharashtra",
    "postalCode": "400001",
    "phone": "+91 9876543210"
  },
  "paymentMethod": "CREDIT_CARD",
  "trackingNumber": "BW-TRK-958920",
  "estimatedDelivery": "2026-10-18",
  "orderDate": "2026-10-12T22:37:15.976044800Z",
  "cancelEligibleUntil": "2026-10-14T22:37:15.976044800Z",
  "items": [
    {
      "bookId": "c2c2c2c2-2222-4222-8222-222222222222",
      "title": "Joy of Minimalism",
      "price": 149.0,
      "quantity": 1
    },
    {
      "bookId": "c4c4c4c4-4444-4444-8444-444444444444",
      "title": "The Path to Success",
      "price": 359.0,
      "quantity": 2
    }
  ]
}
```
**Status**: ✅ **PASSED**

---

### 5.3 Cart Cleanup Verification (`GET /api/v1/cart`)

**Call executed immediately following order placement**:
- **Actual Response**: `HTTP 200 OK`
  ```json
  {
    "cartId": "fa83db06-f99e-4f01-a7c2-ba5468076032",
    "totalItems": 0,
    "subtotal": 0.0,
    "items": []
  }
  ```
- **Finding**: Cart line items were completely cleared upon checkout completion.  
**Status**: ✅ **PASSED**

---

### 5.4 Order History Verification (`GET /api/v1/orders`)

- **Actual Response**: `HTTP 200 OK`
  - Array count: `1`
  - `orderNumber`: `BW-1791844635976`
  - `totalAmount`: `871.04`
  - `trackingNumber`: `BW-TRK-958920`
  - `items`: 2 items preserved with snapshot pricing.  
**Status**: ✅ **PASSED**

---

## 6. PostgreSQL Persistence Evidence

Verified via direct query execution against the `bookworm_db` database in PostgreSQL:

### 6.1 `orders` Table Verification

```sql
SELECT id, order_number, user_id, status, subtotal, tax, shipping_fee, discount, total_amount, tracking_number, estimated_delivery 
FROM orders;
```

**Result**:
```text
                  id                  |   order_number   |               user_id                | status | subtotal |  tax   | shipping_fee | discount | total_amount | tracking_number | estimated_delivery 
--------------------------------------+------------------+--------------------------------------+--------+----------+--------+--------------+----------+--------------+-----------------+--------------------
 5b653292-d2aa-4e7d-8ae6-842c719c5f80 | BW-1791844635976 | d424ae36-4d65-4c3c-bcff-cf2cd08efb22 | PAID   |   867.00 | 104.04 |         0.00 |   100.00 |       871.04 | BW-TRK-958920   | 2026-10-18
(1 row)
```
- Order record persisted with complete pricing breakdown.
- Tracking number generated and saved.
- Delivery date saved as `2026-10-18`.

---

### 6.2 `order_items` Table Verification

```sql
SELECT id, order_id, title, price, quantity 
FROM order_items;
```

**Result**:
```text
                  id                  |               order_id               |        title        | price  | quantity 
--------------------------------------+--------------------------------------+---------------------+--------+----------
 6b9b2560-80f4-45b0-be05-db79b94c9fad | 5b653292-d2aa-4e7d-8ae6-842c719c5f80 | Joy of Minimalism   | 149.00 |        1
 107911f1-e0c0-4a38-932f-4a844cfc60bb | 5b653292-d2aa-4e7d-8ae6-842c719c5f80 | The Path to Success | 359.00 |        2
(2 rows)
```
- Item snapshots persisted with immutable prices and quantities.

---

### 6.3 `cart_items` Table Verification

```sql
SELECT count(*) as cart_items_count FROM cart_items;
```

**Result**:
```text
 cart_items_count 
------------------
                0
(1 row)
```
- Cart items table confirmed completely clean after purchase.

**Status**: ✅ **PASSED**

---

## 7. Key Business Rules Verified

### 7.1 Tax Calculation
- **Rule**: `tax = subtotal * 0.12`
- **Verification**: Subtotal 867.00 $	imes$ 0.12 = 104.04. Matches live calculation.
- **Status**: ✅ **VERIFIED**

### 7.2 Shipping Rule
- **Rule**: `subtotal >= 499 -> 0.00 (free shipping)`, otherwise `50.00`.
- **Verification**: Evaluated with both free tier ($\ge 499$) and standard tier ($< 499$) in tests.
- **Status**: ✅ **VERIFIED**

### 7.3 Coupon Rule
- **Rule**: `SAVE100 = 100 discount`.
- **Verification**: Coupon code successfully applied with exact 100.00 deduction.
- **Status**: ✅ **VERIFIED**

### 7.4 Cart Cleanup
- **Rule**: Cart automatically emptied after successful purchase.
- **Verification**: `cart_items` row count dropped to 0 immediately post-order.
- **Status**: ✅ **VERIFIED**

---

## 8. Conclusion

Phase 4 is fully completed and verified.

Successfully implemented:
- Dynamic checkout quote calculation (`POST /api/v1/checkout/quote`)
- Order creation and atomic snapshotting (`POST /api/v1/orders`)
- Order and order items persistence in PostgreSQL
- Order history retrieval (`GET /api/v1/orders`)
- Automated cart cleanup post-purchase

The application now supports the complete end-to-end customer purchase workflow.

**Overall Result**:  
✅ **PHASE 4 PASSED**  
✅ **READY FOR PHASE 5**
