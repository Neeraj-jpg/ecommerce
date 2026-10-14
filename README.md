# E-Commerce Product Management System

Full-stack e-commerce application with a React/TypeScript frontend and a Spring Boot backend. The frontend provides product browsing and account flows; the backend exposes REST APIs, persists data in PostgreSQL, handles authentication and payments, and publishes background-job updates over WebSocket.

## Repository layout

```text
ecommerce/
├── backend/    # Spring Boot 4, Java 17, PostgreSQL, REST, STOMP/WebSocket
└── frontend/   # React 19, TypeScript, Vite 8
```

- [Frontend guide](./frontend/README.md) — detailed features, frontend structure, scripts, user flows, and troubleshooting.
- Backend configuration is in [`backend/src/main/resources/application.properties`](./backend/src/main/resources/application.properties).

## Features

- Browse products, search by product name, and filter by category.
- Sign up and sign in with user/admin dashboards.
- Add products through the admin dashboard.
- Manage a shopping cart and submit checkout through Razorpay.
- View background-job status and receive live status updates.
- Use PostgreSQL for application persistence.

## Requirements

- Node.js 20.19+ or 22.12+ and npm for the frontend.
- Java 17 or newer for the backend.
- PostgreSQL, with a database available to the backend.
- Razorpay test credentials to exercise payment operations.
- Mail provider settings for backend features that send email.

## Run locally

Start PostgreSQL and configure the backend first. The backend defaults to port `8081` and a local PostgreSQL URL for the `ecommerce_db` database. Review `backend/src/main/resources/application.properties` and supply the required database, Razorpay, and mail settings through environment configuration. Keep server credentials outside source control.

### Start the backend

On Windows:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

On macOS/Linux:

```bash
cd backend
./mvnw spring-boot:run
```

The backend API is expected at `http://localhost:8081`. The backend's current CORS configuration allows the Vite development origin `http://localhost:5173`.

### Start the frontend

In a second terminal, from the repository root:

```bash
cd frontend
npm ci
npm run dev
```

Open the local Vite URL printed in the terminal (normally `http://localhost:5173`). For frontend build, lint, preview, and endpoint details, see the [frontend README](./frontend/README.md).

## Backend configuration and integrations

The backend reads database, Razorpay, and mail configuration from `backend/src/main/resources/application.properties`. Configure the required values in your local environment or deployment secret store before starting it. Never commit real credentials, payment secrets, or mail passwords.

The frontend currently calls the backend at `http://localhost:8081`; this is a local-development assumption and must be updated/configured for other environments. The frontend also loads the Razorpay Checkout.js script from Razorpay.

## API overview

| Area | Base path |
| --- | --- |
| Authentication | `/api/auth` |
| Products | `/api/products` |
| Payments | `/api/payment` |
| Background jobs | `/api/jobs` |
| Background-job WebSocket | `/ws-smartcart` |

The browser-side routes and request details are listed in the [frontend backend-connection guide](./frontend/README.md#backend-connection).

## Build and test

Frontend:

```bash
cd frontend
npm ci
npm run build
npm run lint
```

Backend tests:

```bash
cd backend
./mvnw test
```

On Windows, use `.\mvnw.cmd test` instead of `./mvnw test`.

## Important development and deployment notes

- The frontend uses in-memory client state for login and cart data; reloading the page clears that state.
- Backend authorization must enforce access to admin and payment operations. Hiding dashboard buttons in the frontend is not an authorization boundary.
- Use environment-specific API origins and payment configuration before deployment; current frontend endpoints target localhost.
- Use HTTPS and secure secret storage in deployed environments.
- The frontend signup form includes a role field. Do not trust a client-provided role to grant administrative privileges; enforce role assignment and permissions on the backend.
