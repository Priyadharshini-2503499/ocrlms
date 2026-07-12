# ORCLMS — Night‑Before CHEAT SHEET

> Rapid revision for an internal, code‑focused interview. Three parts:
> **(A)** rapid‑fire flashcards, **(B)** ports/endpoints/key‑classes tables, **(C)** 30‑second
> elevator answers per service. Full detail lives in `INTERVIEW_MASTER_GUIDE.md`.

---

## A. Rapid‑Fire Flashcards (cover the right column)

| Question | Answer |
|---|---|
| What is ORCLMS? | A Spring Boot/Spring Cloud **microservices** retail platform (orders, products, loyalty, promotions, returns). |
| Single entry point? | **API Gateway** on port **8765**. Frontend talks ONLY to it. |
| Service discovery? | **Eureka** (8761) — services register/find each other by name. |
| Central config? | **Config Server** (8888) — serves per‑service `.properties` + shared `application.properties`. |
| Who issues JWTs? | **auth-service** (signs with shared secret, HS256). |
| Who verifies JWTs? | **api-gateway** (`JwtAuthenticationGatewayFilter`, a `GlobalFilter`). |
| JWT contents? | `sub`=username, `role` claim, `iat`, `exp` (1 hour). |
| JWT structure? | `header.payload.signature` (Base64URL). Signed, not encrypted. |
| Why secret in config‑server? | HS256 is symmetric — auth (sign) & gateway (verify) must share the SAME secret. |
| Password storage? | **BCrypt** hash (salted, slow). Never plaintext. |
| How do services call each other? | **OpenFeign** declarative clients (`@FeignClient`). |
| Resilience mechanism? | **Resilience4j** circuit breakers + fallback classes. |
| Breaker tuning? | sliding‑window=10, failure‑rate=50% → opens after 5/10 failures. |
| DB pattern? | **Database‑per‑Service** (each owns its MySQL DB). |
| Are customerId/productId FKs? | No — cross‑service references resolved via Feign. |
| Order statuses? | PLACED → CONFIRMED → SHIPPED → DELIVERED; (PLACED/CONFIRMED → CANCELLED). |
| Loyalty tiers? | SILVER (<1000), GOLD (≥1000), PLATINUM (≥2000) points. |
| Return statuses? | REQUESTED → APPROVED → REFUNDED; REQUESTED → REJECTED. |
| Coupon statuses? | ACTIVE, EXPIRED, REDEEMED. |
| Roles? | ADMIN, MERCHANDISER, STORE_MANAGER, CUSTOMER_SERVICE, MARKETING_MANAGER. |
| Who can create users? | **ADMIN only** (`hasRole("ADMIN")`). No self‑registration. |
| Default admin? | `admin` / `admin123` (seeded by `DataInitializer`). |
| Why TWO JWT filters? | Gateway = edge auth (reactive `GlobalFilter`); auth‑service = role enforcement (`OncePerRequestFilter`). |
| Do order/product/loyalty validate JWT? | No — they **trust the gateway** (`X-Auth-User`/`X-Auth-Role`). |
| Gateway tech? | Spring Cloud Gateway on **WebFlux (reactive, non‑blocking)**. |
| Public paths (no JWT)? | `/api/auth/login`, `/actuator`, `/eureka`, CORS `OPTIONS`. |
| Token on the client? | `localStorage` key `auriaToken` (see `auth.js`). |
| What on 401 (token present)? | `api.js` clears session + redirects to login. |
| Coupon failure at checkout? | Best‑effort — order completes at **full price**. |
| Where's the gateway URL set? | `frontend/assets/js/config.js` → `BASE_URL`. |
| Boot order? | config → eureka → gateway → business services. |

---

## B. Quick Tables

### B1. Ports
| Service | Port |
|---|---|
| config-server | 8888 |
| eureka-server | 8761 |
| api-gateway | 8765 |
| auth-service | 8095 |
| order-service | 8081 |
| productcatalog / loyalty / promotions / returns | assigned via Eureka |

### B2. Gateway routes (`api-gateway.properties`)
| Path predicate | → Service |
|---|---|
| `/api/auth/**` | auth-service |
| `/api/orders/**` | order-service |
| `/products/**` | productcatalog-service |
| `/api/customers/**` | loyalty-service |
| `/api/promotions/**` | promotions-service |
| `/api/returns/**` | returns-service |

### B3. Key endpoints by service
| Service | Endpoints |
|---|---|
| auth | `POST /api/auth/login` (public); `POST/GET /api/auth/users` (ADMIN) |
| order | `GET /api/orders(?customerId=)`, `GET /api/orders/{id}`, `POST /api/orders`, `POST /api/orders/{id}/cancel`, `PATCH /api/orders/{id}/status`, `GET /api/orders/{id}/allowed-statuses` |
| product | `GET /products`, `GET /products/{id}`, `POST /products`, `PUT /products/{id}/price`, `PUT /products/{id}/deactivate`, `PUT /products/{id}/discontinue` |
| loyalty | `GET/POST /api/customers`, `GET /api/customers/{id}`, `POST /api/customers/{id}/redeem`, `/accrue`, `/add-points` |
| promotions | `GET/POST /api/promotions/coupons`, `POST /api/promotions/apply`, `/coupons/{code}/apply-once`, `/redeem`, `/validate` |
| returns | `GET/POST /api/returns`, `GET /api/returns/{id}`, `POST /api/returns/{id}/approve`, `/reject`, `/refund` |

### B4. Key classes to name‑drop
| Concept | Class |
|---|---|
| Gateway JWT check | `JwtAuthenticationGatewayFilter`, `JwtUtil` |
| Gateway CORS | `CorsConfig` (`CorsWebFilter`) |
| Auth login logic | `AuthService`, `AuthController` |
| JWT sign/parse | `JwtService` (auth-service) |
| Auth security chain | `SecurityConfig`, `JwtAuthenticationFilter`, `CustomUserDetailsService` |
| Seed admin | `DataInitializer` |
| Order logic | `OrderService` (`placeOrder`, `isValidTransition`) |
| Order Feign | `ProductClient`, `CustomerClient`, `PromotionClient` (+ `*Fallback`) |
| Loyalty logic | `CustomerService` (`accruePointsFromPurchase`, `updateLoyaltyTier`) |
| Promotions logic | `PromotionService` (`validateCoupon`, `applyDiscount`) |
| Returns logic | `ReturnsService` (`initiateReturn`, 30‑day window) |
| Infra app classes | `@EnableConfigServer`, `@EnableEurekaServer`, `@EnableFeignClients` |

### B5. Config files (`orclms-config-server-main/`)
| File | Holds |
|---|---|
| `application.properties` | Eureka URL, JPA, **`jwt.secret`**, `jwt.expiration` (shared) |
| `api-gateway.properties` | port 8765 + all routes |
| `order-service.properties` | port 8081, `order_db`, circuit‑breaker tuning |
| `auth-service.properties` | port 8095, `authservice_db`, admin bootstrap |
| `*-service.properties` | per‑service port + DB |

---

## C. 30‑Second Elevator Answers (say these out loud)

**config-server** — "Spring Cloud Config Server. On startup every service pulls its config from
here, including the shared JWT secret. One place to change settings; no rebuilds. If it's down,
services can't start correctly — which is why it boots first."

**eureka-server** — "The service registry. Services register by name and discover each other by
name, so the gateway routes to `lb://order-service` and Feign uses `@FeignClient(name=...)` — no
hard‑coded IPs. Without it, routing and inter‑service calls fail."

**api-gateway** — "The single secured front door on 8765, built on reactive WebFlux. It routes
paths to services and runs a `GlobalFilter` that validates the JWT on every request except public
ones, then forwards the identity as `X-Auth-User`/`X-Auth-Role` headers so downstream services can
trust it. Centralizing auth here means each service doesn't re‑implement it."

**auth-service** — "Owns users, login, and JWT issuing. Login verifies credentials via Spring
Security's `AuthenticationManager` + BCrypt, then `JwtService` signs a 1‑hour token with the
username and role. It also has its own security chain so `/api/auth/users` is ADMIN‑only — even a
valid non‑admin token is rejected with 403. The first admin is seeded at startup."

**order-service** — "The core. `placeOrder` is `@Transactional`: it validates the customer via
loyalty (Feign), validates each product and price via the catalog (Feign), sums the total,
optionally applies a coupon via promotions best‑effort, then saves. Status follows a strict state
machine, and on DELIVERED it accrues loyalty points. All Feign calls have Resilience4j fallbacks."

**productcatalog-service** — "Manages products — SKU, name, category, price, status. Merchandisers
use it; order‑service reads prices and the active flag from it at checkout so prices are never
trusted from the client."

**loyalty-service** — "Manages customers, points, and tiers. order‑service verifies a customer
here before placing an order and calls `/accrue` on delivery, which adds `floor(amount)` points and
recomputes the tier — SILVER/GOLD/PLATINUM. It also pulls a customer's order history from
order‑service via Feign."

**promotions-service** — "Manages coupons and computes discounts (percentage or flat, floored at
zero). order‑service calls `/apply` at checkout. If a coupon is invalid or this service is down,
the order just proceeds at full price — graceful degradation, the best example in the system."

**returns-service** — "Manages the return/refund lifecycle. It verifies via Feign that the order
exists and is DELIVERED, enforces a 30‑day window and one‑return‑per‑order, then moves through
REQUESTED → APPROVED/REJECTED → REFUNDED."

**frontend** — "A vanilla‑JS SPA. `config.js` has the gateway URL; `api.js` attaches the Bearer
token and handles 401 by logging out; `endpoints.js` names every URL once; `auth.js` stores the
session and routes each role to its landing page. Role menus are UX only — real security is the
gateway + auth‑service."

---

### The two answers you'll most likely be asked — memorize verbatim

**"What happens when a user clicks Login?"**
> "The browser posts username/password to the gateway at `/api/auth/login`. That path is public,
> so the gateway skips the JWT check and routes to auth‑service. auth‑service verifies the password
> with BCrypt via the `AuthenticationManager`, then `JwtService` signs a 1‑hour JWT containing the
> username and role and returns it. The frontend stores the token in `localStorage` and redirects
> to the role's landing page. After that, every request carries `Authorization: Bearer <token>`,
> which the gateway validates centrally and forwards identity headers downstream."

**"Why two JWT filters?"**
> "The gateway's `GlobalFilter` does edge authentication for the whole system — is the token valid?
> — and injects `X-Auth-*` headers. auth‑service still needs its own servlet filter to rebuild the
> security context so it can enforce `hasRole('ADMIN')` on `/api/auth/users`. They're different
> filter types because the gateway is reactive WebFlux and auth‑service is blocking MVC."

