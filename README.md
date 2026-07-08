# Omnichannel Retail & Customer Loyalty Management System

A monorepo split into a decoupled **backend** (Spring Boot REST API) and **frontend** (static app).
Each of the 5 feature modules has its own folder in **both** layers so the team can work in parallel
with minimal merge conflicts.

## Repository layout

```
backend/                          # Spring Boot application (Java 17, Maven)
  pom.xml
  mvnw / mvnw.cmd / .mvn/         # Maven wrapper (CI + local use)
  src/main/java/com/genc/retailapp/
    common/config/                # cross-cutting config (CORS, etc.)
    customer/                     # customer-loyalty module
    order/                        # order-management module
    product/                      # product-catalog module
    promotion/                    # promotion-coupon module
    returns/                      # return-refund module
  src/main/resources/             # application.properties, schema.sql, dummyData.sql
  src/test/java/com/genc/retailapp/

frontend/                         # static app (HTML/CSS/JS), served separately
  index.html  login.html  dashboard.html
  public/assets/{images,icons}/
  src/shared/{css,js,components}/  # shared across modules
  src/modules/
    customer-loyalty/{pages,css,js,data}/
    order-management/{pages,css,js,data}/
    product-catalog/{pages,css,js,data}/
    promotion-coupon/{pages,css,js,data}/
    return-refund/{pages,css,js,data}/

docs/                             # SRS, slides, diagrams
.github/                          # CODEOWNERS + CI workflow
```

## Module ownership

| Module            | Backend package                  | Frontend folder                         |
| ----------------- | -------------------------------- | --------------------------------------- |
| customer-loyalty  | `com.genc.retailapp.customer`    | `frontend/src/modules/customer-loyalty` |
| order-management  | `com.genc.retailapp.order`       | `frontend/src/modules/order-management` |
| product-catalog   | `com.genc.retailapp.product`     | `frontend/src/modules/product-catalog`  |
| promotion-coupon  | `com.genc.retailapp.promotion`   | `frontend/src/modules/promotion-coupon` |
| return-refund     | `com.genc.retailapp.returns`     | `frontend/src/modules/return-refund`    |

Shared/cross-cutting code is owned by the lead: `com.genc.retailapp.common` and `frontend/src/shared`.

## Running the backend (API on :8080)

```bash
cd backend
./mvnw spring-boot:run
```

The API is served at `http://localhost:8080`. REST endpoints live under `/api/**` and are
CORS-enabled for the frontend dev server (see `common/config/WebCorsConfig`).
An in-memory **H2** database is configured for development; the H2 web console is available at
`http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:retaildb`, user `sa`, no password).

## Running the frontend (static server on :5500)

The frontend is a plain static app. Serve it with any static server, for example the
VS Code **Live Server** extension (default port 5500) or:

```bash
cd frontend
npx serve -l 5500        # or: python -m http.server 5500
```

Open `http://localhost:5500`. The frontend calls the backend at `http://localhost:8080/api/...`.

## Branch naming

Use one branch per change, scoped to a module:

```
feat/<module>/<short-desc>     e.g. feat/order-management/checkout-validation
```

Other common prefixes: `fix/<module>/<short-desc>`, `chore/<short-desc>`.
