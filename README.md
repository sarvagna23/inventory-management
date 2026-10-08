# Inventory Management System 📦

> Full-stack inventory management: React TypeScript frontend, Spring Boot (Java 21) backend with REST and GraphQL APIs, a Node.js JWT gateway, PostgreSQL, and Supabase real-time low-stock alerts.

## Architecture

```
React TypeScript (port 3000)
        ↓ JWT token
Node.js middleware (port 4000): JWT auth gateway
        ↓ proxy
Spring Boot Java (port 8080): REST + GraphQL
        ↓
PostgreSQL (port 5434): primary database
        ↓ sync on stock update
Supabase: real-time WebSocket broadcasting
        ↓
React frontend: instant low-stock alert popup
```

Spring Boot is designed to be reached through the gateway. The local setup also exposes port 8080 so GraphiQL can be opened directly.

## Screenshots

### Dashboard with Real-Time Alert
![Dashboard](screenshot/dashboard-realtime-alert.png)

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React TypeScript, Recharts, Supabase JS client |
| Auth gateway | Node.js, Express, JWT |
| Backend | Spring Boot (Java 21), Hibernate JPA |
| API | REST endpoints and GraphQL (10 queries, 1 mutation) |
| Database | PostgreSQL, 4 tables auto-created by Hibernate |
| Real-time | Supabase WebSocket subscriptions |
| Containers | Docker (PostgreSQL via docker-compose) |

## Features

**Dashboard**
- KPI cards: total products, low-stock alerts, orders, revenue
- Products by category pie chart
- Orders by status bar chart
- Live low-stock alerts table

**Products**
- Full CRUD with supplier relationship
- Inline stock update with instant feedback
- Low-stock detection: red highlight when stock is at or below the threshold
- Search by name, SKU, or category

**Real-time alerts**
- Supabase WebSocket subscription on the products table
- Instant popup when stock drops below the reorder threshold
- Connection status indicator (green dot means connected)
- Dismissable alert cards with timestamp

**Security**
- JWT authentication through the Node.js gateway
- 3 roles: ADMIN, MANAGER, VIEWER
- API and GraphQL routes sit behind the gateway

**GraphQL**
- 10 queries: products, product, productBySku, productsByCategory, lowStockProducts, searchProducts, suppliers, supplier, orders, order
- 1 mutation: updateStock
- GraphiQL UI at localhost:8080/graphiql

## Run Locally

Prerequisites: Docker, Java 21, Maven, Node.js, and a Supabase project (for real-time alerts).

Docker runs only PostgreSQL. The backend, middleware and frontend run directly on your machine.

```bash
git clone https://github.com/sarvagna23/inventory-management
cd inventory-management

# 1. PostgreSQL
docker-compose up -d

# 2. Spring Boot backend (port 8080)
cd backend
mvn spring-boot:run

# 3. Node.js middleware (port 4000)
cd ../middleware
node src/server.js

# 4. React frontend (port 3000)
cd ../frontend
npm install
npm start
```

**`frontend/.env`**
```
REACT_APP_SUPABASE_URL=your-supabase-url
REACT_APP_SUPABASE_ANON_KEY=your-anon-key
```

**`backend/src/main/resources/application.properties`**
```
supabase.url=your-supabase-url
supabase.anon-key=your-anon-key
```

## Demo Credentials

For local demo use only. Change them for any real deployment.

| Username | Password | Role |
|---|---|---|
| admin | admin123 | ADMIN |
| manager | manager123 | MANAGER |
| viewer | viewer123 | VIEWER |

 UNCOMMENT AFTER THE TESTS EXIST AND PASS, AND FILL IN THE VALUES

## Testing

End-to-end tests use Playwright and run against the local stack.

| Suite | What it covers |
|---|---|
| auth | Login for each role, wrong-password error, role-based UI limits |
| stock | Product list loads, search filters rows, quantity update |
| alerts | Low-stock event shows an alert in the UI (WebSocket mocked) |

    cd frontend
    npx playwright install
    npx playwright test

Result: <N> tests passing in <seconds> s.

The alerts test mocks the Supabase realtime WebSocket. It verifies that the UI handles an alert event correctly, not that Supabase delivers events.

## Performance

Measured with Lighthouse on a production build (npm run build, served locally), same machine and settings before and after.

| Metric | Before | After |
|---|---|---|
| Performance score | <before> | <after> |
| Largest Contentful Paint | <before> s | <after> s |
| Initial JS bundle | <before> KB | <after> KB |

Changes made: <list only what you actually changed>.



## Key Technical Decisions

**Why Node.js middleware instead of Spring Security?**
The Node.js gateway validates JWT tokens before requests reach Spring Boot. This keeps authentication separate from business logic, a common API gateway pattern for microservices.

**Why GraphQL alongside REST?**
REST handles writes, where order processing needs transactions and validation. GraphQL handles reads, so the frontend requests exactly the fields it needs and avoids over-fetching.

**Why Supabase for real-time?**
Local PostgreSQL has no WebSocket broadcasting. Supabase adds a real-time layer with no extra infrastructure: stock updates sync to Supabase, which broadcasts them to all connected React clients.

**Why `@Transactional` on createOrder?**
Order processing is atomic. Stock is validated for all items before any is deducted, and if one item is out of stock the whole order rolls back, so there is no partial data.

## Author

**Sai Sarvagna Beeram**
MS Computer Science, Georgia State University (Dec 2026)
GitHub: [sarvagna23](https://github.com/sarvagna23)
LinkedIn: [saisarvagna023](https://linkedin.com/in/saisarvagna023)
