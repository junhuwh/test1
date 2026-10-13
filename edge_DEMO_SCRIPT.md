# Book Worm AI Specialist Capstone Project
## Edge Narration Script

Hello everyone.

Welcome to the demonstration of my AI Specialist Capstone Project called Book Worm E Commerce Backend.

The goal of this project was to build a complete bookstore backend using an AI assisted development workflow.

Instead of manually designing and implementing everything from scratch, I used AI coding assistants to analyze requirements, design APIs, generate code, create tests, and verify functionality.

In this presentation I will walk through the development process and demonstrate the working application.

---

The project started from a bookstore wireframe provided in the Capstone requirements.

My first step was to use AI to analyze the business requirements and identify the core domains of the system.

The analysis produced a simplified architecture containing five key business areas.

Catalog Management.

User Authentication.

Shopping Cart.

Checkout.

And Order Management.

Based on this analysis, I created a Minimum Viable Product architecture that covered all required business journeys while keeping the implementation simple and maintainable.

---

The next step was API design.

I used AI to generate an OpenAPI contract for the entire backend.

The specification defines all business operations required by the bookstore system.

These include browsing books, user registration, login, shopping cart management, checkout, order creation, order cancellation, and buy again functionality.

Using a contract first approach ensured that the implementation remained consistent throughout the project.

---

From the API specification, I generated a Spring Boot backend using Java twenty one and PostgreSQL.

The generated project includes controllers, services, repositories, entities, security configuration, database migrations, Swagger documentation, and automated testing.

The application follows a clean layered architecture and uses JWT based authentication.

---

Now I will demonstrate the running application.

First, I retrieve the available book categories.

The data is loaded directly from PostgreSQL.

Next, I search for books and display book details.

The response includes pricing information, descriptions, ratings, and related books.

This verifies the catalog functionality.

---

Next, I demonstrate user registration and login.

A new user account is created.

The password is automatically encrypted using BCrypt before being stored.

The system also creates an empty shopping cart for the new user.

After registration, a JWT access token is issued.

The same JWT mechanism is used during login.

Once authenticated, the user can access secured business functions.

---

Now I move to the shopping cart.

I add a book into the cart.

The subtotal is calculated automatically.

When I add the same book again, the system increases the quantity instead of creating duplicate rows.

Additional books can be added and the totals are recalculated dynamically.

This verifies the cart lifecycle functionality.

---

Before creating an order, I request a checkout quote.

The system calculates taxes, discounts, and shipping charges.

A promotional coupon can also be applied.

The total payable amount is generated dynamically based on the contents of the shopping cart.

---

Next, I create an order.

The application generates a unique order number and tracking number.

Order items are stored as immutable snapshots.

An estimated delivery date is calculated automatically.

After the order is successfully created, the shopping cart is automatically cleared.

This confirms that the complete purchase workflow is functioning correctly.

---

The system also supports order history.

Previously created orders can be retrieved together with pricing information, delivery information, and purchased items.

This provides a complete record of customer purchases.

---

One of the key requirements of this project is the forty eight hour cancellation policy.

When an order is cancelled within the allowed period, the order status changes to cancelled and a refund is processed.

If the same order is cancelled again, the system correctly blocks the operation and returns a business validation error.

---

The application also supports the Buy Again feature.

Historical order items can be added back into the shopping cart with a single action.

The quantities are restored automatically and the cart totals are recalculated.

This significantly improves the user experience for repeat purchases.

---

To verify persistence, I query PostgreSQL directly.

The database contains real user records, shopping cart data, orders, and order items created during the demonstration.

This confirms that the application uses real database persistence rather than in memory mock data.

---

In summary, this project demonstrates a complete AI assisted software development workflow.

The solution includes requirement analysis, architecture design, API specification generation, Spring Boot implementation, automated testing, PostgreSQL integration, and end to end business verification.

All core bookstore business journeys have been implemented and successfully validated.

Thank you for reviewing my AI Specialist Capstone Project.