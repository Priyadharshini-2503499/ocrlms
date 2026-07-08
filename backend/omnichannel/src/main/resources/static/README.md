# Aurelia Retail Console — Frontend

Pure **HTML + CSS + JavaScript + Bootstrap 5**. No build step, no framework.
This is the UI lifted out of the Spring Boot backend. All data comes from REST
APIs through a single API Gateway. Until the backend is ready, a small mock
layer serves sample data so every page works right now.

---

## How to run

1. Open the `frontend/` folder in VS Code.
2. Right-click `index.html` → **Open with Live Server**.
3. Pick a role on the landing page and explore.

> Pages use ES modules (`<script type="module">`), so they must be **served**
> (Live Server), not opened straight from the file system.

Everything works immediately because mock mode is ON by default.

---

## Switching from mock data to the real backend

Open `assets/js/config.js` and change two things:

```js
BASE_URL: "http://localhost:8080",  // your API Gateway address
USE_MOCK: false,                    // turn the mock OFF
```

If any real endpoint path differs from the guesses, fix it in **one file**:
`assets/js/endpoints.js`. No page code needs to change.

---

## Folder map

```
frontend/
  index.html                 role picker (landing)
  assets/
    css/  console.css, components.css     (design, copied from backend)
    js/
      config.js     BASE_URL, currency, USE_MOCK switch
      api.js        fetch helper (mock-aware)
      endpoints.js  every backend call, named once  <-- edit URLs here later
      layout.js     builds sidebar + topbar on every page
      ui.js         money/badge/toast/getParam helpers
      mock.js       in-memory fake backend (only used when USE_MOCK = true)
  productcatalog/   products, product-detail, product-form
  order-management/ orders, order-detail, place-order, update-status
  loyalty/          customers, customer-detail, customer-form
  returns/          returns, return-detail, return-form
  promotion/        promotions
```

Each `.html` page has a matching `.js` file of the same name.

---

## API contract (what the frontend expects)

All paths sit behind `BASE_URL`. IDs are passed in the page URL as a query
param (e.g. `product-detail.html?id=5`), and to the API in the path.

| Action | Method + path | Sends | Gets back |
|--------|---------------|-------|-----------|
| List products | `GET /products` | — | `Product[]` |
| Get product | `GET /products/{id}` | — | `Product` |
| Create product | `POST /products` | `{skuCode, category, productName, basePrice, productStatus}` | `Product` |
| Update price | `PUT /products/{id}/price` | `{basePrice}` | `Product` |
| Deactivate product | `PUT /products/{id}/deactivate` | — | `Product` |
| List orders | `GET /orders` (`?customerId=` optional) | — | `Order[]` |
| Get order | `GET /orders/{id}` | — | `Order` |
| Place order | `POST /orders` | `{customerId, orderChannel, items:[{productId, quantity}]}` | `Order` |
| Update status | `PUT /orders/{id}/status` | `{newStatus}` | `Order` |
| Cancel order | `PUT /orders/{id}/cancel` | — | `Order` |
| List customers | `GET /customers` | — | `Customer[]` |
| Get customer | `GET /customers/{id}` | — | `Customer` |
| Register customer | `POST /customers` | `{fullName, emailId, phoneNumber}` | `Customer` |
| Redeem points | `POST /customers/{id}/redeem` | `{points}` | `Customer` |
| Accrue points | `POST /customers/{id}/accrue` | `{points}` | `Customer` |
| List returns | `GET /returns` | — | `Return[]` |
| Get return | `GET /returns/{id}` | — | `Return` |
| Create return | `POST /returns` | `{orderId, returnReason, requestDate?}` | `Return` |
| Approve return | `PUT /returns/{id}/approve` | — | `Return` |
| Process refund | `PUT /returns/{id}/refund` | — | `Return` |
| List coupons | `GET /coupons` | — | `Coupon[]` |
| Create coupon | `POST /coupons` | `{couponCode, discountType, discountValue, validFrom, validTo}` | `Coupon` |
| Apply coupon | `POST /coupons/apply` | `{code, amount}` | `{message, finalAmount}` |

### Data shapes (field names)

- **Product:** `productId, skuCode, productName, category, basePrice, productStatus` (`ACTIVE`/`DISCONTINUED`)
- **Order:** `orderId, customerId, orderChannel` (`ONLINE`/`INSTORE`/`MOBILE`)`, orderDate, totalAmount, orderStatus` (`PLACED`/`CONFIRMED`/`SHIPPED`/`DELIVERED`/`CANCELLED`)`, items[]`
- **Order item:** `productId, productName, unitPrice, quantity, lineTotal`
- **Customer:** `customerId, fullName, emailId, phoneNumber, loyaltyPoints, loyaltyTier` (`SILVER`/`GOLD`/`PLATINUM`)
- **Return:** `returnId, orderId, returnReason, refundAmount, requestDate, returnStatus` (`REQUESTED`/`APPROVED`/`REFUNDED`/`REJECTED`)`, approvable, refundable`
- **Coupon:** `couponCode, discountType` (`PERCENTAGE`/`FLAT`)`, discountValue, validFrom, validTo, couponStatus` (`ACTIVE`/`EXPIRED`/`REDEEMED`)

> `approvable`/`refundable` are convenience booleans the UI uses to show the
> Approve / Process-refund buttons. If the backend does not send them, the UI
> can fall back to checking `returnStatus`.

---

## Notes for later

- **CORS:** when `USE_MOCK = false`, the gateway must allow the Live Server
  origin (e.g. `http://127.0.0.1:5500`).
- **Path-style URLs:** today the UI uses `?id=` query params so it runs on any
  static host. Only `getParam()` in `ui.js` reads the id, so switching to true
  `/products/5` paths later is a small, isolated change.
- **Auth:** only a role picker for now (role saved in `localStorage`). When JWT
  arrives, add the token header in `api.js` (one place).

