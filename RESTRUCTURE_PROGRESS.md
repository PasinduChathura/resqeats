# Resqeats Backend Restructure - Progress Report

## Overview

Full backend restructure to comply with SRS Version 2.0 (January 2026).

## ✅ Completed Tasks

### 1. New Entity Layer (SRS Section 7 - Data Model)

All entities use UUID primary keys and follow SRS-mandated hierarchy:

| Entity          | Package               | Description                                               |
| --------------- | --------------------- | --------------------------------------------------------- |
| `User`          | `user/entity`         | User with RBAC roles (ADMIN, MERCHANT, OUTLET_USER, USER) |
| `Merchant`      | `merchant/entity`     | Merchant with approval workflow                           |
| `Outlet`        | `outlet/entity`       | Outlet with geo coordinates                               |
| `OutletHours`   | `outlet/entity`       | Daily operating hours                                     |
| `Item`          | `item/entity`         | Item/SecretBox with pricing                               |
| `OutletItem`    | `item/entity`         | Item-Outlet junction with stock                           |
| `Order`         | `order/entity`        | Order with state machine                                  |
| `OrderItem`     | `order/entity`        | Order line items                                          |
| `Payment`       | `payment/entity`      | Payment with pre-auth/capture                             |
| `PaymentMethod` | `payment/entity`      | Tokenized payment methods                                 |
| `RefreshToken`  | `auth/entity`         | Refresh token storage                                     |
| `OtpCode`       | `auth/entity`         | OTP verification codes                                    |
| `Notification`  | `notification/entity` | Push notifications                                        |

### 2. Enums (SRS Compliant)

| Enum                  | Description                                     |
| --------------------- | ----------------------------------------------- |
| `UserRole`            | ADMIN, MERCHANT, OUTLET_USER, USER              |
| `UserStatus`          | ACTIVE, INACTIVE, SUSPENDED                     |
| `MerchantCategory`    | RESTAURANT, BAKERY, CAFE, GROCERY, etc.         |
| `MerchantStatus`      | PENDING, ACTIVE, SUSPENDED, REJECTED            |
| `OutletStatus`        | ACTIVE, INACTIVE, TEMPORARILY_CLOSED            |
| `ItemCategory`        | MEALS, BAKERY, PRODUCE, DAIRY, etc.             |
| `ItemType`            | ITEM, SECRET_BOX                                |
| `ItemStatus`          | ACTIVE, INACTIVE, SOLD_OUT                      |
| `OrderStatus`         | Full state machine with transitions             |
| `PaymentStatus`       | PENDING → AUTHORIZED → CAPTURED/VOIDED/REFUNDED |
| `NotificationType`    | ORDER*\*, PAYMENT*\*, PROMO, SYSTEM             |
| `NotificationChannel` | PUSH, EMAIL, SMS, IN_APP                        |
| `NotificationStatus`  | PENDING, SENT, READ, FAILED                     |

### 3. Repositories

All repositories created with custom query methods:

- `UserRepository`, `MerchantRepository`, `OutletRepository`
- `ItemRepository`, `OutletItemRepository`
- `OrderRepository`, `OrderItemRepository`
- `PaymentRepository`, `PaymentMethodRepository`
- `RefreshTokenRepository`, `OtpCodeRepository`
- `NotificationRepository`, `OutletHoursRepository`

### 4. Services Layer (SRS Section 6)

| Service               | Business Rules Implemented                     |
| --------------------- | ---------------------------------------------- |
| `AuthService`         | JWT auth, OTP, refresh tokens (BR-001)         |
| `OrderService`        | Full state machine (BR-001 to BR-005)          |
| `PaymentService`      | Pre-auth/capture flow (BR-004, BR-005, BR-006) |
| `InventoryService`    | Redis + DB consistency (BR-007 to BR-009)      |
| `CartService`         | Soft-state with TTL (BR-010 to BR-013)         |
| `MerchantService`     | Approval workflow (BR-017 to BR-019)           |
| `OutletService`       | Geo-indexed, operating hours (BR-020, BR-021)  |
| `ItemService`         | Pricing validation (BR-022 to BR-024)          |
| `NotificationService` | Multi-channel push                             |
| `GeoService`          | Redis GEO for nearby queries                   |
| `WebSocketService`    | Real-time updates                              |
| `UserService`         | Profile management                             |

### 5. Controllers (SRS Section 6.2 API Endpoints)

| Controller               | Endpoints                                          |
| ------------------------ | -------------------------------------------------- |
| `AuthController`         | `/api/v1/auth/*`                                   |
| `CartController`         | `/api/v1/cart/*`                                   |
| `OrderController`        | `/api/v1/orders/*`, `/api/v1/outlet/orders/*`      |
| `MerchantController`     | `/api/v1/merchants/*`, `/api/v1/admin/merchants/*` |
| `OutletController`       | `/api/v1/outlets/*`                                |
| `ItemController`         | `/api/v1/items/*`, `/api/v1/outlets/*/items`       |
| `PaymentController`      | `/api/v1/payments/*`                               |
| `NotificationController` | `/api/v1/notifications/*`                          |
| `UserController`         | `/api/v1/users/*`, `/api/v1/admin/users/*`         |

### 6. DTOs Created

- Auth: `LoginRequest`, `AuthResponse`, `RegisterRequest`, `OtpRequest`, etc.
- Cart: `CartDto`, `CartItemDto`
- Order: `CreateOrderRequest`, `OrderDto`
- Merchant: `CreateMerchantRequest`, `UpdateMerchantRequest`, `MerchantDto`
- Outlet: `CreateOutletRequest`, `UpdateOutletRequest`, `OutletDto`
- Item: `CreateItemRequest`, `UpdateItemRequest`, `ItemDto`, `OutletItemDto`
- User: `UserDto`, `UpdateUserRequest`
- Common: `PageResponse`

### 7. Configuration

- `RedisConfig` - Redis template configuration
- `JwtConfig` - JWT properties
- Updated `application.properties` for PostgreSQL + Redis

### 8. Exception Handling

- `BusinessException` - Error code support
- `GlobalExceptionHandler` - Maps error codes to HTTP status

### 9. Security

- `CurrentUser` annotation
- `UserPrincipal` - Spring Security user details
- Role-based access with `@PreAuthorize`

## ❌ Pending Tasks

### 1. Delete Old Files (Non-SRS Compliant)

The following old directories/packages need to be removed:

#### Old Models (use new entities instead):

- `models/shop/` → Replaced by `outlet/entity/`
- `models/food/` → Replaced by `item/entity/`
- `models/customer/` → Replaced by `user/entity/`
- `models/master/` → Not in SRS, delete
- `models/usermgt/` → Replaced by `user/` with enum roles
- `models/merchant/` → Replaced by `merchant/entity/`
- `models/cart/` → Cart is now soft-state in Redis
- `models/order/` → Replaced by `order/entity/`
- `models/payment/` → Replaced by `payment/entity/`
- `models/notification/` → Replaced by `notification/entity/`
- `models/security/` → Replaced by `auth/entity/`

#### Old Controllers:

- `controller/shop/` → Replaced by `outlet/controller/`
- `controller/food/` → Replaced by `item/controller/`
- `controller/customer/` → Replaced by `user/controller/`
- `controller/master/` → Not in SRS, delete

#### Old Services:

- `service/shop/` → Replaced by `outlet/service/`
- `service/food/` → Replaced by `item/service/`
- `service/customer/` → Replaced by `user/service/`
- `service/master/` → Not in SRS, delete

#### Old DTOs:

- `dto/shop/` → Replaced by `outlet/dto/`
- `dto/food/` → Replaced by `item/dto/`
- `dto/customer/` → Replaced by `user/dto/`
- `dto/master/` → Not in SRS, delete

### 2. Update Existing Files

- `SecurityConfig.java` - Update to use new UserPrincipal
- `CustomUserDetailsService.java` - Already exists, may need update
- `AuthTokenFilter.java` - Verify compatibility
- `JwtUtils.java` - Verify compatibility

### 3. Database Migration

- Create migration scripts for PostgreSQL
- Update schema for UUID primary keys
- Create indexes for geo queries

### 4. Testing

- Update/create unit tests for new services
- Integration tests for order flow
- API tests for endpoints

## New Package Structure

```
com.ffms.resqeats/
├── auth/
│   ├── controller/AuthController.java
│   ├── dto/
│   ├── entity/
│   ├── repository/
│   └── service/AuthService.java
├── cart/
│   ├── controller/CartController.java
│   ├── dto/
│   └── service/CartService.java
├── common/
│   ├── dto/ApiResponse.java, PageResponse.java
│   ├── entity/BaseEntity.java
│   └── exception/BusinessException.java, GlobalExceptionHandler.java
├── config/
│   ├── JwtConfig.java
│   ├── RedisConfig.java
│   ├── SecurityConfig.java (needs update)
│   └── WebSocketConfig.java (exists)
├── geo/
│   └── service/GeoService.java
├── inventory/
│   └── service/InventoryService.java
├── item/
│   ├── controller/ItemController.java
│   ├── dto/
│   ├── entity/
│   ├── enums/
│   ├── repository/
│   └── service/ItemService.java
├── merchant/
│   ├── controller/MerchantController.java
│   ├── dto/
│   ├── entity/
│   ├── enums/
│   ├── repository/
│   └── service/MerchantService.java
├── notification/
│   ├── controller/NotificationController.java
│   ├── entity/
│   ├── enums/
│   ├── repository/
│   └── service/NotificationService.java
├── order/
│   ├── controller/OrderController.java
│   ├── dto/
│   ├── entity/
│   ├── enums/
│   ├── repository/
│   └── service/OrderService.java
├── outlet/
│   ├── controller/OutletController.java
│   ├── dto/
│   ├── entity/
│   ├── enums/
│   ├── repository/
│   └── service/OutletService.java
├── payment/
│   ├── controller/PaymentController.java
│   ├── entity/
│   ├── enums/
│   ├── repository/
│   └── service/PaymentService.java
├── security/
│   ├── CurrentUser.java
│   ├── CustomUserDetailsService.java (exists)
│   └── UserPrincipal.java
├── user/
│   ├── controller/UserController.java
│   ├── dto/
│   ├── entity/
│   ├── enums/
│   ├── repository/
│   └── service/UserService.java
└── websocket/
    └── service/WebSocketService.java
```

## Business Rules Implemented

| Code   | Rule                                                         | Status |
| ------ | ------------------------------------------------------------ | ------ |
| BR-001 | Orders cannot be placed without successful pre-authorization | ✅     |
| BR-002 | Order state transitions must follow state machine            | ✅     |
| BR-003 | Cancellation allowed only in PENDING/ACCEPTED states         | ✅     |
| BR-004 | Payment captured only upon outlet acceptance                 | ✅     |
| BR-005 | Pre-authorization voided if outlet declines/times out        | ✅     |
| BR-006 | Cash-on-delivery NOT supported                               | ✅     |
| BR-007 | Inventory cannot go below zero                               | ✅     |
| BR-008 | Reserved stock expires after cart timeout                    | ✅     |
| BR-009 | Stock decremented atomically                                 | ✅     |
| BR-010 | Cart expires after 10 minutes                                | ✅     |
| BR-011 | Cannot mix items from different outlets                      | ✅     |
| BR-012 | Item removed if out of stock                                 | ✅     |
| BR-013 | Price locked when item added to cart                         | ✅     |
| BR-014 | Default search radius 5km                                    | ✅     |
| BR-015 | Maximum search radius 50km                                   | ✅     |
| BR-016 | Only active outlets shown                                    | ✅     |
| BR-017 | Only ADMIN can approve/reject merchants                      | ✅     |
| BR-018 | Merchant must be approved before creating outlets            | ✅     |
| BR-019 | Merchant status changes require audit trail                  | ✅     |
| BR-020 | Outlet managed by owner or staff only                        | ✅     |
| BR-021 | Operating hours required before activation                   | ✅     |
| BR-022 | Discounted price ≤ original price                            | ✅     |
| BR-023 | Secret boxes require min 30% discount                        | ✅     |
| BR-024 | Items auto-marked sold out when qty=0                        | ✅     |

## Next Steps

1. **Run clean-up script** to remove old non-SRS files
2. **Update pom.xml** to add PostgreSQL driver, remove MySQL
3. **Update existing security classes** to use new entities
4. **Create database migration** scripts
5. **Write unit tests** for new services
6. **Test API endpoints** with Postman

---

_Generated: Backend Restructure Phase 1 Complete_
