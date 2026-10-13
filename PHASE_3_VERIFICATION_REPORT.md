# Phase 3 Verification Report

**Project**: Book Worm eCommerce Backend  
**Phase**: Phase 3 - Shopping Cart Lifecycle  
**Date**: 2026-10-13  
**Status**: ✅ **PASSED**

---

## 1. Executive Summary

Phase 3 (Shopping Cart Lifecycle) has been implemented, tested, and validated against the local PostgreSQL database instance and live Spring Boot runtime.

### Key Deliverables Verified:
- `GET /api/v1/cart`: Seamless retrieval of the authenticated user's active shopping cart, including dynamically computed subtotal, item counts, and line item previews.
- `POST /api/v1/cart/items`: Adding products to the basket. If an item already exists in the cart, its quantity is automatically incremented without creating duplicate rows.
- `PUT /api/v1/cart/items/{id}`: Updating item quantities with instantaneous total recalculations; setting quantity to `0` cleanly removes the item from database storage.
- **JWT Authentication Protection**: Verified; unauthenticated calls are rejected with HTTP 403/401.
- **PostgreSQL Persistence**: Verified with direct SQL queries on the `cart_items` and `carts` tables.
- **Cart Creation and Retrieval**: Verified; users are guaranteed an active cart session.
- **Quantity Accumulation**: Verified; adding an existing item increments quantity cleanly.
- **Quantity Update & Removal**: Verified; quantity adjustments and zero-quantity deletion work accurately.
- **Unit Tests Passed**: 27 / 27 tests passed across all modules (0 failures, 0 errors, 0 skipped).
- **Live API Verification Passed**: Verified via live HTTP requests on `http://localhost:8080`.

**Overall Result**: ✅ **PASSED**

---

## 2. Scope Implemented

### 2.1 Get Cart (`GET /api/v1/cart`)
- **User Context Extraction**: Dynamically resolves the authenticated user's UUID from the JWT SecurityContext.
- **Active Cart Resolution**: Loads existing cart or safely provisions an empty cart if missing.
- **Dynamic Calculation**: Dynamically computes line item totals (`unit_price * quantity`), cumulative item counts, and grand subtotal (`RoundingMode.HALF_UP`).
- **OpenAPI Schema Compliance**: Formats and returns standard `CartDto` containing `cartId`, `totalItems`, `subtotal`, and `items`.

### 2.2 Add Item To Cart (`POST /api/v1/cart/items`)
- **New Item Addition**: Binds book reference, quantity, and current cart into a new `CartItem` entity.
- **Quantity Accumulation**: Automatically detects if the target `book_id` already exists within the cart and adds the requested quantity to the existing tally.
- **Data Persistence**: Immediately commits the new item or updated quantity to the PostgreSQL `cart_items` table.
- **Dynamic Recalculation**: Refreshes and returns the updated `CartDto`.

### 2.3 Update Cart Item (`PUT /api/v1/cart/items/{id}`)
- **Quantity Modification**: Updates line item quantity to any requested value between 1 and 10.
- **Zero-Quantity Removal**: If `quantity <= 0`, triggers `cartItemRepository.delete()` to purge the record from the database.
- **Subtotal Synchronization**: Recalculates cart-wide totals and updates `carts.updated_at` timestamp.

---

## 3. Components Implemented

### 3.1 `CartServiceImpl`
- **Location**: `src/main/java/com/bookworm/service/impl/CartServiceImpl.java`
- **Responsibilities**:
  - `getCart(UUID currentUserId)`: Retrieves active user cart with live pricing aggregation.
  - `addItemToCart(UUID currentUserId, AddToCartRequest request)`: Handles add-to-cart operations, entity lookups, and quantity accumulation.
  - `updateCartItemQuantity(UUID currentUserId, UUID cartItemId, UpdateCartItemRequest request)`: Enforces ownership validation, updates item count, or executes item deletion when quantity is zero.
  - `getOrCreateCartEntity(UUID userId)`: Safeguards session integrity by provisioning an empty cart if none is attached.
  - `toCartDto(Cart cart)`: Maps database entities to OpenAPI response DTOs.

### 3.2 `CartControllerImpl`
- **Location**: `src/main/java/com/bookworm/controller/impl/CartControllerImpl.java`
- **Responsibilities**:
  - Implements `CartController` interface secured under Spring Security (`@SecurityRequirement(name = "bearerAuth")`).
  - Extracts principal user UUID from `SecurityContextHolder.getContext().getAuthentication()`.
  - Rejects unauthenticated callers with `UnauthorizedException`.
  - Exposes RESTful endpoints returning `ResponseEntity<CartDto>`.

---

## 4. Unit Test Evidence

### 4.1 Test Suites Executed
- **`CartServiceTest` (6 tests)**:
  - `testGetCart_EmptyCart_Success`: Confirms empty cart returns `0` total items and `0.00` subtotal.
  - `testAddItemToCart_NewItem_Success`: Verifies adding a new book accurately updates line items and subtotal.
  - `testAddItemToCart_ExistingItem_IncreasesQuantity`: Verifies adding an existing book increments quantity (2 + 3 = 5) and subtotal (5 * 149.00 = 745.00).
  - `testUpdateCartItemQuantity_PositiveQuantity_Success`: Verifies setting a positive quantity recalculates totals.
  - `testUpdateCartItemQuantity_ZeroQuantity_RemovesItem`: Verifies setting quantity to 0 deletes the item and empties the cart.
  - `testAddItemToCart_BookNotFound_ThrowsException`: Verifies 404 exception when target book does not exist.
- **`CartControllerTest` (4 tests)**:
  - `testGetCart_Success`: Confirms HTTP 200 and DTO mapping for authenticated requests.
  - `testAddItemToCart_Success`: Confirms HTTP 200 upon adding item.
  - `testUpdateCartItemQuantity_Success`: Confirms HTTP 200 upon quantity modification.
  - `testGetCart_Unauthenticated_ThrowsException`: Confirms rejection when authentication is missing.
- **Regression Suites**: `AuthControllerTest` (3 tests), `CatalogControllerTest` (4 tests), `AuthServiceTest` (5 tests), `CatalogServiceTest` (5 tests).

### 4.2 Maven Test Output

```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.bookworm.controller.AuthControllerTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.096 s
[INFO] Running com.bookworm.controller.CartControllerTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.070 s
[INFO] Running com.bookworm.controller.CatalogControllerTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.137 s
[INFO] Running com.bookworm.service.AuthServiceTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.687 s
[INFO] Running com.bookworm.service.CartServiceTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.153 s
[INFO] Running com.bookworm.service.CatalogServiceTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.164 s
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 27, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

---

## 5. Live API Verification

Live API testing was conducted against the local server (`http://localhost:8080`) backed by PostgreSQL, authenticated as `charlie@bookworm.example.com`.

### 5.1 Retrieve Empty Cart
- **Endpoint**: `GET /api/v1/cart`
- **Request Headers**: `Authorization: Bearer <valid_jwt>`
- **Observed Response**: `HTTP 200 OK`
  ```json
  {
    "cartId": "fa83db06-f99e-4f01-a7c2-ba5468076032",
    "totalItems": 0,
    "subtotal": 0.00,
    "items": []
  }
  ```
- **Status**: ✅ **PASSED**

---

### 5.2 Add Item To Cart
- **Endpoint**: `POST /api/v1/cart/items`
- **Payload**: `{"bookId": "c2c2c2c2-2222-4222-8222-222222222222", "quantity": 1}` (*Joy of Minimalism*, Price: 149.00)
- **Observed Response**: `HTTP 200 OK`
  - `totalItems`: 1
  - `subtotal`: 149.00
  - `items`: 1 item (`quantity`: 1, `totalPrice`: 149.00)
- **Status**: ✅ **PASSED**

---

### 5.3 Add Same Item Again (Quantity Accumulation)
- **Endpoint**: `POST /api/v1/cart/items`
- **Payload**: `{"bookId": "c2c2c2c2-2222-4222-8222-222222222222", "quantity": 2}`
- **Observed Response**: `HTTP 200 OK`
  - `totalItems`: 3 (1 + 2 = 3)
  - `subtotal`: 447.00 (3 * 149.00 = 447.00)
  - `items`: Exactly 1 line item with accumulated `quantity: 3`
- **Status**: ✅ **PASSED**

---

### 5.4 Add Multiple Books
- **Endpoint**: `POST /api/v1/cart/items`
- **Payload**: `{"bookId": "c4c4c4c4-4444-4444-8444-444444444444", "quantity": 1}` (*The Path to Success*, Price: 359.00)
- **Observed Response**: `HTTP 200 OK`
  - `totalItems`: 4 (3 + 1 = 4)
  - `subtotal`: 806.00 (447.00 + 359.00 = 806.00)
  - `items`: 2 distinct line items
- **Status**: ✅ **PASSED**

---

### 5.5 Update Quantity
- **Endpoint**: `PUT /api/v1/cart/items/{book1_item_id}`
- **Payload**: `{"quantity": 1}`
- **Observed Response**: `HTTP 200 OK`
  - `totalItems`: 2 (1 Joy of Minimalism + 1 The Path to Success)
  - `subtotal`: 508.00 (149.00 + 359.00 = 508.00)
  *(Matches the exact shopping cart state and ₹508.00 subtotal depicted in Capstone Slide 8)*
- **Status**: ✅ **PASSED**

---

### 5.6 Zero Quantity Removal
- **Endpoint**: `PUT /api/v1/cart/items/{book2_item_id}`
- **Payload**: `{"quantity": 0}`
- **Observed Response**: `HTTP 200 OK`
  - `totalItems`: 1
  - `subtotal`: 149.00
  - `items`: Book 2 line item completely deleted; only Book 1 remains.
- **Status**: ✅ **PASSED**

---

## 6. PostgreSQL Persistence Evidence

The persistence of cart state was verified by querying PostgreSQL via `psql`:

### 6.1 Database Verification Query

```sql
SELECT ci.id,
       ci.cart_id,
       b.title,
       ci.quantity,
       b.price,
       (ci.quantity * b.price) AS total
FROM cart_items ci
JOIN books b ON ci.book_id = b.id
JOIN carts c ON ci.cart_id = c.id
JOIN users u ON c.user_id = u.id
WHERE u.email = 'charlie@bookworm.example.com';
```

### 6.2 PostgreSQL Execution Output

```text
                  id                  |               cart_id                |       title       | quantity | price  | total  
--------------------------------------+--------------------------------------+-------------------+----------+--------+--------
 cc9f597a-9861-4a35-b082-821698240024 | fa83db06-f99e-4f01-a7c2-ba5468076032 | Joy of Minimalism |        1 | 149.00 | 149.00
(1 row)
```

### 6.3 Findings:
- Cart record exists and is mapped to Charlie Brown's UUID.
- Removed item (*The Path to Success*) was deleted from `cart_items` table.
- Surviving item quantity (`1`) and total (`149.00`) match live API state.

**Status**: ✅ **PASSED**

---

## 7. Security Validation

### 7.1 JWT Authentication
- All `/api/v1/cart/**` routes are locked behind `SecurityConfig.anyRequest().authenticated()`.
- Valid Bearer token successfully identifies current user context via `SecurityContextHolder`.
- **Status**: ✅ **PASSED**

### 7.2 Unauthorized Access Protection
- Requests omitting the `Authorization` header receive immediate HTTP 403 Forbidden rejection.
- Requests carrying malformed or expired tokens are rejected.
- **Status**: ✅ **PASSED**

---

## 8. Key Business Rules Verified

### 8.1 Quantity Accumulation
- **Rule**: Adding an existing book increases quantity rather than creating duplicate rows.
- **Verification**: Second add-to-cart call for Book 1 incremented quantity from 1 to 3 under a single line item ID.
- **Status**: ✅ **VERIFIED**

### 8.2 Automatic Cart Creation
- **Rule**: Users always have an available active cart.
- **Verification**: `getOrCreateCartEntity()` ensures all registered and seeded users immediately possess an active cart session.
- **Status**: ✅ **VERIFIED**

### 8.3 Quantity Zero Removal
- **Rule**: Setting quantity to 0 removes the cart item.
- **Verification**: `PUT /api/v1/cart/items/{id}` with `{"quantity": 0}` deleted the row from PostgreSQL.
- **Status**: ✅ **VERIFIED**

---

## 9. Conclusion

Phase 3 is fully completed and verified.

Successfully implemented:
- Cart retrieval (`GET /api/v1/cart`)
- Add-to-cart (`POST /api/v1/cart/items`)
- Quantity updates (`PUT /api/v1/cart/items/{id}`)
- Quantity accumulation
- Quantity zero removal
- Dynamic subtotal calculation
- PostgreSQL persistence
- JWT security

The application is ready to proceed to:
**Phase 4 - Checkout & Order Placement**

**Overall Result**:  
✅ **PHASE 3 PASSED**  
✅ **READY FOR PHASE 4**
