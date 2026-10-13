# 🎬 Book Worm Capstone Project — Manager & Reviewer Demo Script

**Project**: Book Worm eCommerce Cloud FullStack Backend  
**Target Duration**: 3 – 5 Minutes  
**Audience**: AI Specialist Capstone Review Board / Engineering Manager  
**Presenter**: Cloud FullStack AI Specialist Candidate  
**Live Demo URL**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## ⏱️ Timeline & Presentation Flow Overview

| Section | Topic | Estimated Time | Key Screen Action |
| :---: | :--- | :---: | :--- |
| **1** | Project Introduction | 00:00 – 00:25 | Show PowerPoint Wireframe Slide 5 & `README.md` |
| **2** | Agentic Development Workflow | 00:25 – 00:50 | Show Prompt Flow & Architecture Diagram |
| **3** | Requirement Analysis | 00:50 – 01:10 | Show `BOOKSTORE_REQUIREMENTS_ANALYSIS.md` |
| **4** | OpenAPI Contract Generation | 01:10 – 01:30 | Show `openapi.yaml` & Swagger UI Overview |
| **5** | Spring Boot 3.3 Architecture | 01:30 – 01:50 | Show Project Package Tree & Flyway Migrations |
| **6** | Catalog & Discovery Demo | 01:50 – 02:20 | Execute `GET /categories` & `GET /books` |
| **7** | Authentication & JWT Demo | 02:20 – 02:50 | Execute `POST /register`, `POST /login` & Authorize |
| **8** | Shopping Cart Demo | 02:50 – 03:20 | Execute `POST /cart/items` (accumulate) & `PUT` (remove) |
| **9** | Checkout Quote Demo | 03:20 – 03:45 | Execute `POST /checkout/quote` with `SAVE100` |
| **10** | Order Placement Demo | 03:45 – 04:10 | Execute `POST /orders` & verify cart emptied |
| **11** | 48-Hour Order Cancellation Demo | 04:10 – 04:35 | Execute `POST /orders/{id}/cancel` & show 422 guard |
| **12** | Buy In Again Demo | 04:35 – 04:55 | Execute `POST /orders/{id}/buy-again` & show cart refill |
| **13** | PostgreSQL Verification | 04:55 – 05:15 | Show `psql` console table records |
| **14** | Conclusion & Wrap-Up | 05:15 – 05:30 | Concluding remarks on AI engineering impact |

---

## 🗣️ Spoken Script & Step-by-Step Walkthrough

### 1. Project Introduction (00:00 – 00:25)
- **🖥️ Screen Action**: Display PowerPoint Slide 3 & Slide 5 ("Book Worm" eCommerce wireframe) and project `README.md`.
- **🎙️ Spoken Script**:
  > *"Hello, everyone. Welcome to the demonstration of my AI Specialist Capstone Project: **Book Worm eCommerce Backend**.
  >
  > Rather than writing traditional boilerplate code by hand, I utilized an **Agentic AI Engineering workflow** to transform complex UI wireframes into a fully working, production-ready Spring Boot 3.3 and PostgreSQL backend.
  >
  > Over the next few minutes, I will walk you through the spec-driven development process and demonstrate the live running application supporting the complete customer journey."*

---

### 2. Agentic Development Workflow (00:25 – 00:50)
- **🖥️ Screen Action**: Display the Mermaid diagram in `README.md` or IDE terminal showing the phased roadmap.
- **🎙️ Spoken Script**:
  > *"Our development methodology followed a strict contract-first principle across five structured phases:
  >
  > First, reverse-engineering the UI wireframes into business requirements; second, generating a standard OpenAPI 3.1 contract; third, generating a clean layered Spring Boot skeleton; and finally, implementing and verifying the functionality in iterative phases with unit tests and live PostgreSQL integration.
  >
  > The agentic IDE wasn’t just a code completion tool—it acted as an architect, generating compile-ready code, configuring Flyway migrations, and automating regression tests."*

---

### 3. Requirement Analysis (00:50 – 01:10)
- **🖥️ Screen Action**: Open and scroll through `BOOKSTORE_REQUIREMENTS_ANALYSIS.md` and `BOOKSTORE_MVP_ARCHITECTURE.md`.
- **🎙️ Spoken Script**:
  > *"Here in `BOOKSTORE_REQUIREMENTS_ANALYSIS.md`, we identified the core domains: Catalog, Members, Cart, Checkout, and Orders.
  >
  > To ensure high delivery velocity while meeting 100% of the evaluation criteria, we streamlined the physical schema into 7 core tables in `BOOKSTORE_MVP_ARCHITECTURE.md`—flattening secondary attributes like author bios and covers directly into the Book entity, which eliminated complex joins while retaining full fidelity to the wireframes."*

---

### 4. OpenAPI Generation (01:10 – 01:30)
- **🖥️ Screen Action**: Open `openapi.yaml` in VS Code / IDE, then switch to the in-app browser showing Swagger UI at `http://localhost:8080/swagger-ui/index.html`.
- **🎙️ Spoken Script**:
  > *"Next, we generated our contract: `openapi.yaml`. It defines all 15 MVP operations, 25 reusable schemas, and RFC 9457 Problem Details error handling.
  >
  > When we launch the Spring Boot service, SpringDoc immediately renders this exact specification into interactive Swagger UI, as you see right here in my browser. Every endpoint, request body constraint, and response example is strictly defined before implementation begins."*

---

### 5. Spring Boot Generation (01:30 – 01:50)
- **🖥️ Screen Action**: Expand the project tree in the IDE (`controller`, `service`, `repository`, `entity`, `db/migration`).
- **🎙️ Spoken Script**:
  > *"The generated Spring Boot 3.3 backend uses Java 21, Spring Data JPA, Spring Security 6 with stateless JWT Bearer tokens, and Flyway database migrations.
  >
  > All 70 initial source files were scaffolded with clean architecture interfaces. Flyway scripts `V1` and `V2` manage our PostgreSQL schema and seed data for immediate testing.
  >
  > Now, let’s see the live application in action."*

---

### 6. Catalog & Discovery Demo (01:50 – 02:20)
- **🖥️ Screen Action**: In Swagger UI, expand **Catalog**; execute `GET /api/v1/categories`, then execute `GET /api/v1/books` with `query=Minimalism`.
- **🎙️ Spoken Script**:
  > *"Starting with Catalog Discovery:
  >
  > When I execute `GET /api/v1/categories`, the service queries PostgreSQL and returns all 19 genre categories sorted alphabetically, matching our wireframe sidebar.
  >
  > Next, let's test `GET /api/v1/books`. I'll search with the keyword 'Minimalism'. It returns our paginated `PageBookDto` containing 'Joy of Minimalism' by Daniel Reed, complete with pricing, cover URLs, and dynamic delivery estimates calculated five days out."*

---

### 7. Authentication & JWT Demo (02:20 – 02:50)
- **🖥️ Screen Action**: In Swagger UI, expand **Authentication**; execute `POST /api/v1/auth/register`, then click the **Authorize 🔓** button and paste the token.
- **🎙️ Spoken Script**:
  > *"Now, let's register a new user: Charlie Brown.
  >
  > I execute `POST /api/v1/auth/register`. Notice we get HTTP 201 Created. The password has been hashed with BCrypt, 100 loyalty gift points are awarded, an empty shopping cart is automatically provisioned in PostgreSQL, and a signed JWT Bearer token is returned.
  >
  > If I try to register the same email again, our exception handler immediately rejects it with HTTP 409 Conflict.
  >
  > I'll copy the JWT token, click the Swagger Authorize button, and paste it. Now all protected endpoints are unlocked."*

---

### 8. Shopping Cart Demo (02:50 – 03:20)
- **🖥️ Screen Action**: In Swagger UI, execute `GET /api/v1/cart` (show empty), then execute `POST /api/v1/cart/items` with Book 1 (`qty=1`), then add it again (`qty=2`), then add Book 2.
- **🎙️ Spoken Script**:
  > *"Moving to the Shopping Cart:
  >
  > Calling `GET /api/v1/cart` shows our active cart is initially empty.
  >
  > Now I add 'Joy of Minimalism' for ₹149. The subtotal updates to ₹149.
  >
  > Now observe the business rule: when I add the same book again with quantity 2, the backend doesn't create duplicate rows—it accumulates the quantity to 3, and subtotal becomes ₹447.
  >
  > When I add our second book, 'The Path to Success' for ₹359 and adjust quantities, our subtotal becomes exactly ₹508.00, perfectly matching the wireframe in Slide 8."*

---

### 9. Checkout Quote Demo (03:20 – 03:45)
- **🖥️ Screen Action**: In Swagger UI, execute `POST /api/v1/checkout/quote` with `couponCode: "SAVE100"`.
- **🎙️ Spoken Script**:
  > *"Before placing the order, the checkout screen requests a price quote via `POST /api/v1/checkout/quote`.
  >
  > Notice the dynamic calculation:
  > - Subtotal is ₹508.00.
  > - Tax is exactly 12%, which is ₹60.96.
  > - Delivery charges are ₹0.00 because subtotal exceeds the ₹499 free-shipping threshold.
  > - And because I supplied coupon 'SAVE100', a ₹100 discount is deducted.
  > - Final payable total: ₹468.96. Everything is calculated dynamically in memory."*

---

### 10. Order Placement Demo (03:45 – 04:10)
- **🖥️ Screen Action**: In Swagger UI, execute `POST /api/v1/orders` with shipping address and card details. Then execute `GET /api/v1/cart`.
- **🎙️ Spoken Script**:
  > *"Now let's place the order via `POST /api/v1/orders`.
  >
  > We submit our delivery address in Mumbai and credit card payment details.
  >
  > The API responds with HTTP 201 Created! It generated order number `BW-` followed by the timestamp, a tracking number `BW-TRK-`, froze our item prices into `order_items`, and set status to `PAID`.
  >
  > Crucially, if I call `GET /api/v1/cart` now, the cart has been automatically cleared, ensuring clean transactional lifecycle management."*

---

### 11. 48-Hour Order Cancellation Demo (04:10 – 04:35)
- **🖥️ Screen Action**: Copy the newly created `orderId`, execute `POST /api/v1/orders/{id}/cancel`, then execute it again to show HTTP 422.
- **🎙️ Spoken Script**:
  > *"Now for our signature differentiator: the **48-Hour Order Cancellation Policy** from Slide 3.
  >
  > I paste our order ID into `POST /api/v1/orders/{id}/cancel` and execute.
  >
  > It succeeds with HTTP 200 OK! The status is updated to `CANCELLED`, and a full refund of ₹871.04 is marked `PROCESSED`.
  >
  > Notice the defensive guard: if I try to cancel this order a second time, the system recognizes it is already cancelled and rejects it with HTTP 422 Unprocessable Entity. Similarly, any order placed more than 48 hours ago is strictly barred from cancellation."*

---

### 12. Buy In Again Demo (04:35 – 04:55)
- **🖥️ Screen Action**: In Swagger UI, execute `POST /api/v1/orders/{id}/buy-again`, then execute `GET /api/v1/cart`.
- **🎙️ Spoken Script**:
  > *"Next is the **Buy In Again** feature from Slide 6.
  >
  > Instead of manually searching for each book, the customer clicks 'Buy Again'. I call `POST /api/v1/orders/{id}/buy-again`.
  >
  > It immediately reads the historical order items and repopulates them into the active cart!
  >
  > When I call `GET /api/v1/cart`, both books with their exact quantities are back in the shopping basket, ready for checkout."*

---

### 13. PostgreSQL Verification (04:55 – 05:15)
- **🖥️ Screen Action**: Switch to the terminal window and run SQL queries against the containerized database:
  `podman exec bookworm-postgres psql -U postgres -d bookworm_db -c "SELECT id, order_number, status, total_amount FROM orders;"`
- **🎙️ Spoken Script**:
  > *"To prove that this is not just an in-memory mock, let's verify directly in PostgreSQL.
  >
  > Running our SQL query on the `orders` table shows our order record with order number `BW-...`, status `CANCELLED`, and total amount persisted.
  >
  > Querying `cart_items` confirms that our repopulated line items are physically stored in the database with their respective foreign keys."*

---

### 14. Conclusion & Wrap-Up (05:15 – 05:30)
- **🖥️ Screen Action**: Switch back to the `README.md` or Swagger UI overview page.
- **🎙️ Spoken Script**:
  > *"In summary:
  > - 100% of the Capstone MVP endpoints and business journeys are implemented and operating live.
  > - 43 automated unit tests pass with zero failures.
  > - All database tables, seed migrations, and security filters are running seamlessly.
  >
  > This project successfully proves how an Agentic AI engineering workflow enables developers to design, build, test, and document enterprise cloud solutions with speed, precision, and architectural discipline.
  >
  > Thank you for your time, and I look forward to your review!"*

---

## 📋 Quick Pre-Demo Checklist for the Presenter

Before pressing record on your screen capture software:

1. [ ] **Podman/Docker running**: Confirm `podman ps` displays `bookworm-postgres` running on port 5432.
2. [ ] **Spring Boot running**: Confirm `http://localhost:8080/swagger-ui/index.html` loads cleanly in your browser.
3. [ ] **Database state fresh**: If you want a fresh walkthrough, restart Spring Boot or use Charlie Brown's registered account.
4. [ ] **All tests passing**: Run `mvn test` in the terminal to show `43 Tests run, 0 failures, 0 errors, BUILD SUCCESS`.
5. [ ] **Screen resolution**: Set display to 1080p (1920x1080) for crisp Swagger UI and terminal font visibility.
