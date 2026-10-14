# E-Commerce Frontend

React and TypeScript frontend for the E-Commerce Product Management System. It provides product browsing, user registration and sign-in, role-based dashboards, cart management, checkout, and an admin view for adding products and monitoring background jobs.

## Contents

- [Features](#features)
- [Technology](#technology)
- [Requirements](#requirements)
- [Run locally](#run-locally)
- [Available scripts](#available-scripts)
- [Application structure](#application-structure)
- [Backend connection](#backend-connection)
- [Main user flows](#main-user-flows)
- [Development notes](#development-notes)
- [Troubleshooting](#troubleshooting)

## Features

- **Product catalog:** Loads products from the backend, filters by category, and searches product names.
- **Accounts:** Provides signup and signin forms. The app uses the signed-in user's role to show the corresponding dashboard.
- **Admin dashboard:** Submits new product details and displays the product catalog and background-job monitor.
- **User dashboard:** Displays the product catalog, shopping cart, and checkout form.
- **Shopping cart:** Adds products, adjusts quantities, calculates subtotals and the total, and clears the cart.
- **Checkout:** Validates customer details and integrates with the Razorpay checkout script. The backend creates the order and verifies the payment response.
- **Background jobs:** Loads job records and listens for live updates over a STOMP WebSocket connection.
- **Welcome screen:** Shows a short animated welcome screen before revealing the application.

## Technology

- React 19 and TypeScript
- Vite 8
- Axios for authentication and product HTTP requests
- Zustand and Jotai for client state
- React Hook Form and Zod for checkout form handling and validation
- TanStack Query provider configured at the application entry point
- STOMP.js for background-job updates
- Razorpay Checkout.js loaded from Razorpay's hosted script

## Requirements

- Node.js **20.19+** or **22.12+** (Node.js 22.12+ is recommended for Vite 8)
- npm
- The E-Commerce backend running on `http://localhost:8081`
- PostgreSQL and the required backend environment configuration when running the backend locally
- Razorpay test credentials for exercising payment flows

## Run locally

From the repository root, install and start the frontend:

```bash
cd frontend
npm ci
npm run dev
```

Vite prints the local URL when the development server starts (normally `http://localhost:5173`). Open that URL in a browser.

The frontend expects the backend to be available at `http://localhost:8081`. Start/configure the backend before testing sign-in, products, jobs, or checkout. See the repository-level README for the full-stack setup overview and backend details.

### Production build

```bash
npm run build
npm run preview
```

The build runs the TypeScript project checks and generates static assets in `frontend/dist`. `npm run preview` serves that built output locally; it is not a production hosting service.

## Available scripts

| Command | Description |
| --- | --- |
| `npm run dev` | Start the Vite development server with hot-module replacement. |
| `npm run build` | Type-check the application and create the production build in `dist/`. |
| `npm run lint` | Run ESLint over the frontend project. |
| `npm run preview` | Preview the most recently generated production build. |

## Application structure

```text
frontend/
├── public/                 # Public static assets
├── src/
│   ├── api/                # Authentication and product API calls
│   ├── assets/             # Imported images and SVG assets
│   ├── components/         # Navbar, product list/cards, cart, checkout, job monitor
│   ├── data/               # Product-related local data
│   ├── hooks/              # Reusable React hooks
│   ├── pages/              # Signup, signin, user, and admin screens
│   ├── schemas/             # Zod validation schemas
│   ├── store/               # Authentication and cart state
│   ├── types/               # Shared TypeScript types
│   ├── App.tsx              # Page selection and top-level layout
│   ├── App.css              # Application component styles
│   ├── index.css            # Global styles
│   └── main.tsx             # React entry point and providers
├── index.html               # HTML entry point; includes Razorpay Checkout.js
├── package.json             # Scripts and dependencies
└── vite.config.ts           # Vite configuration
```

## Backend connection

The frontend currently uses local development URLs directly in its source. The backend should be reachable at `http://localhost:8081`, and its CORS configuration should allow the Vite origin `http://localhost:5173`.

| Purpose | Method | Backend path |
| --- | --- | --- |
| Sign up | `POST` | `/api/auth/signup` |
| Sign in | `POST` | `/api/auth/signin` |
| List products | `GET` | `/api/products` |
| Add a product | `POST` | `/api/products` |
| List background jobs | `GET` | `/api/jobs` |
| Create a payment order | `POST` | `/api/payment/create-order` |
| Verify a payment | `POST` | `/api/payment/verify` |
| Live background-job updates | STOMP over WebSocket | `/ws-smartcart` (subscribes to `/topic/jobs`) |

Authentication and product requests are implemented in `src/api/`. Checkout and background-job calls are made by their respective components. If the backend host or port changes, update the corresponding frontend URLs; they are not currently read from a Vite environment variable.

The browser checkout flow uses Razorpay Checkout.js from `index.html`. Keep Razorpay secret credentials on the backend only; the frontend should use only the public/test checkout key provided for the intended environment.

## Main user flows

### Browse products

1. Open the Products view.
2. The frontend requests products from `GET /api/products`.
3. Search by product name or select a category to filter the displayed results.

### Sign up and sign in

1. Use **Signup** to submit a name, email, password, and selected role to `POST /api/auth/signup`.
2. Use **Sign In** to submit an email and password to `POST /api/auth/signin`.
3. After sign-in, the returned user and role determine whether the app opens the user or admin dashboard.

### User shopping and checkout

1. Sign in and open the User Dashboard.
2. Add products, change quantities, or remove items by reducing their quantity to zero.
3. Enter checkout contact and address details. The form validates the name, email, phone number, and address.
4. The frontend requests a payment order from the backend, opens Razorpay Checkout, then sends the payment response to the backend for verification.

### Admin product and job management

1. Sign in with an account whose backend role is `ADMIN`.
2. Submit product details in the Admin Dashboard to add a product.
3. Review the product list and background jobs. Job updates are also received live through STOMP.

## Development notes

- The application uses in-component state to switch views; it does not currently use a URL-based router.
- Authentication and cart state are held in client memory. Reloading the page resets that state.
- The API origin and Razorpay script are configured for the current implementation; configure environment-specific values before deploying.
- Role selection in the signup form is not a substitute for backend authorization. The backend must enforce admin access to protected operations.
- Do not put database passwords, Razorpay secrets, mail credentials, or other server-side secrets in frontend code or Vite-exposed environment variables.

## Troubleshooting

### Product or account requests fail

- Confirm the backend is running on port `8081`.
- Confirm its database and required backend settings are configured.
- Check the browser developer console and network panel for failed requests or CORS errors.
- The current backend CORS origin is `http://localhost:5173`; use the configured Vite URL or update the backend CORS settings.

### Checkout does not open

- Confirm the Razorpay Checkout.js script can load in the browser.
- Confirm the backend payment-order endpoint is reachable and the backend has valid Razorpay test credentials.
- Use Razorpay test credentials and test payment methods; never put the Razorpay secret key in this frontend.

### Live job updates do not appear

- Confirm the backend WebSocket endpoint `/ws-smartcart` is available.
- Check that the browser can connect to `ws://localhost:8081/ws-smartcart` and that the backend publishes to `/topic/jobs`.

### Lint reports errors

Run `npm run lint` to see the current ESLint diagnostics. Lint status is separate from the production build and type-check performed by `npm run build`.
