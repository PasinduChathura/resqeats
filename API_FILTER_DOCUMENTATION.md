# ResqEats API - Filter & List Endpoints Documentation

**Version:** 2.0  
**Last Updated:** January 7, 2026

This document provides comprehensive filtering capabilities for all major resources in the ResqEats platform. All endpoints support pagination and advanced filtering.

## 🎯 API Design - Unified Endpoints with Automatic Scope Filtering

The API uses a **unified endpoint pattern** where scope filtering is applied automatically at the repository level based on the authenticated user's role:

| Role                    | Access Scope                                             |
| ----------------------- | -------------------------------------------------------- |
| `ADMIN` / `SUPER_ADMIN` | Global access - sees all data                            |
| `MERCHANT`              | Sees only their merchant's data (orders, items, outlets) |
| `OUTLET_USER`           | Sees only their specific outlet's data                   |
| `USER`                  | Sees only their own data (orders)                        |

This means **the same endpoint returns different data based on who calls it**, eliminating the need for role-specific endpoints like `/admin/orders`, `/outlet/orders`, etc.

---

## 📋 Table of Contents

1. [User Management API](#1-user-management-api)
2. [Merchant Management API](#2-merchant-management-api)
3. [Outlet Management API](#3-outlet-management-api)
4. [Order Management API](#4-order-management-api)
5. [Item Management API](#5-item-management-api)
6. [Common Parameters](#common-parameters)
7. [Response Format](#response-format)

---

## Common Parameters

### Pagination Parameters (All Endpoints)

| Parameter | Type    | Description                                       | Default |
| --------- | ------- | ------------------------------------------------- | ------- |
| `page`    | integer | Page number (0-indexed)                           | 0       |
| `size`    | integer | Items per page                                    | 20      |
| `sort`    | string  | Sort field and direction (e.g., `createdAt,desc`) | -       |

### Date Format

All date parameters use ISO 8601 format: `2026-01-07T10:30:00`

---

## 1. User Management API

### List Users (Scoped by Role)

**Endpoint:** `GET /users`

> **Scope Filtering:**
>
> - ADMIN sees all users
> - MERCHANT sees users linked to their merchant
> - Other roles cannot access this endpoint

**Query Parameters:**
| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| `role` | string | Filter by user role | `ADMIN`, `MERCHANT`, `OUTLET_USER`, `USER` |
| `status` | string | Filter by user status | `ACTIVE`, `SUSPENDED`, `DELETED` |
| `search` | string | Search in email, phone, first/last name | `john@email.com` |
| `merchantId` | UUID | Filter by associated merchant | `550e8400-e29b-41d4-a716-446655440000` |
| `dateFrom` | datetime | Users created after this date | `2026-01-01T00:00:00` |
| `dateTo` | datetime | Users created before this date | `2026-01-31T23:59:59` |

**Example Request:**

```http
GET /users?role=MERCHANT&status=ACTIVE&page=0&size=20&sort=createdAt,desc
```

**Example Response:**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "email": "merchant@example.com",
        "phone": "+1234567890",
        "firstName": "John",
        "lastName": "Doe",
        "role": "MERCHANT",
        "status": "ACTIVE",
        "merchantId": "660e8400-e29b-41d4-a716-446655440000",
        "emailVerified": true,
        "phoneVerified": true,
        "createdAt": "2026-01-05T10:30:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 45,
    "totalPages": 3
  },
  "message": null,
  "timestamp": "2026-01-07T14:30:00"
}
```

---

## 2. Merchant Management API

### List Merchants (Scoped by Role)

**Endpoint:** `GET /api/v1/merchants/list`

> **Scope Filtering:**
>
> - ADMIN sees all merchants (any status)
> - MERCHANT sees only their own merchant
> - Public/USER sees only APPROVED merchants

**Query Parameters:**
| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| `status` | string | Filter by merchant status | `PENDING`, `APPROVED`, `SUSPENDED`, `REJECTED` |
| `category` | string | Filter by business category | `RESTAURANT`, `BAKERY`, `CAFE`, `GROCERY` |
| `search` | string | Search in name, legal name, description | `Pizza Palace` |
| `dateFrom` | datetime | Merchants created after | `2026-01-01T00:00:00` |
| `approvedFrom` | datetime | Approved after this date | `2026-01-01T00:00:00` |
| `city` | string | Filter by city (from outlets) | `New York` |

**Example Request:**

```http
GET /api/v1/merchants/list?status=APPROVED&category=RESTAURANT&page=0&size=20
```

**Example Response:**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "660e8400-e29b-41d4-a716-446655440000",
        "name": "Pizza Palace",
        "legalName": "Pizza Palace Inc.",
        "category": "RESTAURANT",
        "status": "APPROVED",
        "contactEmail": "info@pizzapalace.com",
        "contactPhone": "+1234567890",
        "logoUrl": "https://cdn.resqeats.com/logos/pizza-palace.png",
        "approvedAt": "2026-01-03T15:20:00",
        "createdAt": "2026-01-02T10:00:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 128,
    "totalPages": 7
  },
  "message": null,
  "timestamp": "2026-01-07T14:30:00"
}
```

---

## 3. Outlet Management API

### List/Search Outlets

**Endpoint:** `GET /outlets`

**Query Parameters:**
| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| `merchantId` | UUID | Filter by merchant | `660e8400-e29b-41d4-a716-446655440000` |
| `status` | string | Filter by status | `ACTIVE`, `INACTIVE`, `SUSPENDED` |
| `city` | string | Filter by city | `New York` |
| `search` | string | Search in name, address | `Downtown` |
| `latitude` | decimal | Center latitude for radius search | `40.7128` |
| `longitude` | decimal | Center longitude for radius search | `-74.0060` |
| `radiusKm` | decimal | Search radius in kilometers | `5.0` |

**Example Request (Location-based):**

```http
GET /outlets?latitude=40.7128&longitude=-74.0060&radiusKm=5&status=ACTIVE&page=0&size=10
```

**Example Request (Search):**

```http
GET /outlets?search=Downtown&city=New York&page=0&size=20
```

**Example Response:**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "770e8400-e29b-41d4-a716-446655440000",
        "merchantId": "660e8400-e29b-41d4-a716-446655440000",
        "name": "Pizza Palace Downtown",
        "address": "123 Main St, New York, NY 10001",
        "city": "New York",
        "latitude": 40.7128,
        "longitude": -74.006,
        "phone": "+1234567890",
        "status": "ACTIVE",
        "rating": 4.5,
        "createdAt": "2026-01-02T12:00:00"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 8,
    "totalPages": 1
  },
  "message": null,
  "timestamp": "2026-01-07T14:30:00"
}
```

---

## 4. Order Management API

### List Orders (Unified - Scoped by Role)

**Endpoint:** `GET /api/v1/orders`

> **Scope Filtering:**
>
> - ADMIN sees all orders across all outlets
> - MERCHANT sees orders for all their merchant's outlets
> - OUTLET_USER sees orders for their specific outlet only
> - USER sees only their own orders

**Query Parameters:**
| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| `status` | string | Filter by single status | `PAID`, `PREPARING`, `READY_FOR_PICKUP` |
| `outletId` | UUID | Filter by specific outlet | `770e8400-e29b-41d4-a716-446655440000` |
| `merchantId` | UUID | Filter by merchant | `660e8400-e29b-41d4-a716-446655440000` |
| `dateFrom` | datetime | Orders created after | `2026-01-01T00:00:00` |
| `dateTo` | datetime | Orders created before | `2026-01-07T23:59:59` |
| `minAmount` | decimal | Minimum order amount | `20.00` |
| `maxAmount` | decimal | Maximum order amount | `100.00` |
| `pickupTimeFrom` | datetime | Pickup time after | `2026-01-07T12:00:00` |
| `pickupTimeTo` | datetime | Pickup time before | `2026-01-07T18:00:00` |

**Example Request (Same for all roles!):**

```http
GET /api/v1/orders?status=PREPARING&dateFrom=2026-01-01T00:00:00&page=0&size=20
```

**Example Response:**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "880e8400-e29b-41d4-a716-446655440000",
        "orderNumber": "RQ-A1B2C3D4",
        "userId": "550e8400-e29b-41d4-a716-446655440000",
        "outletId": "770e8400-e29b-41d4-a716-446655440000",
        "status": "PREPARING",
        "totalAmount": 45.99,
        "pickupTime": "2026-01-07T18:00:00",
        "items": [
          {
            "itemName": "Secret Box - Mixed Items",
            "quantity": 2,
            "unitPrice": 19.99
          }
        ],
        "createdAt": "2026-01-07T14:30:00"
      }
    ],
    "page": 0,
    "size": 50,
    "totalElements": 12,
    "totalPages": 1
  },
  "message": null,
  "timestamp": "2026-01-07T14:30:00"
}
```

---

## 5. Item Management API

### List Items (Unified - Scoped by Role)

**Endpoint:** `GET /api/v1/items`

> **Scope Filtering:**
>
> - ADMIN sees all items across all merchants
> - MERCHANT sees only items belonging to their merchant
> - OUTLET_USER sees items for their outlet
> - Public/unauthenticated sees only ACTIVE items

### Search Items (Public)

**Endpoint:** `GET /api/v1/items/search`

**Query Parameters:**
| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| `search` | string | Search in name, description | `pizza` |
| `category` | string | Filter by category | `MEAL`, `BAKERY`, `BEVERAGE`, `GROCERY` |
| `itemType` | string | Filter by type | `SECRET_BOX`, `REGULAR_ITEM` |
| `status` | string | Filter by status | `ACTIVE`, `INACTIVE`, `OUT_OF_STOCK` |
| `minDiscountedPrice` | decimal | Minimum price | `10.00` |
| `maxDiscountedPrice` | decimal | Maximum price | `50.00` |

**Example Request:**

```http
GET /api/v1/items/search?search=pizza&category=MEAL&minDiscountedPrice=10&maxDiscountedPrice=30&page=0&size=20
```

### List Merchant Items (Merchant view)

**Endpoint:** `GET /api/v1/merchants/{merchantId}/items`

**Query Parameters:**
| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| `category` | string | Filter by category | `MEAL`, `BAKERY` |
| `itemType` | string | Filter by type | `SECRET_BOX` |
| `status` | string | Filter by status | `ACTIVE` |
| `search` | string | Search in name, description | `fresh` |

**Example Request:**

```http
GET /api/v1/merchants/660e8400-e29b-41d4-a716-446655440000/items?itemType=SECRET_BOX&status=ACTIVE&page=0&size=20
```

**Example Response:**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "990e8400-e29b-41d4-a716-446655440000",
        "merchantId": "660e8400-e29b-41d4-a716-446655440000",
        "name": "Secret Box - Fresh Bakery",
        "description": "Surprise box with fresh bakery items",
        "category": "BAKERY",
        "itemType": "SECRET_BOX",
        "basePrice": 29.99,
        "discountedPrice": 14.99,
        "discountPercent": 50,
        "status": "ACTIVE",
        "imageUrl": "https://cdn.resqeats.com/items/secret-box-bakery.png",
        "createdAt": "2026-01-05T09:00:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 15,
    "totalPages": 1
  },
  "message": null,
  "timestamp": "2026-01-07T14:30:00"
}
```

---

## Response Format

### Success Response Structure

```json
{
  "success": true,
  "data": {
    "content": [...],      // Array of items
    "page": 0,            // Current page (0-indexed)
    "size": 20,           // Items per page
    "totalElements": 100, // Total number of items
    "totalPages": 5       // Total number of pages
  },
  "message": null,
  "timestamp": "2026-01-07T14:30:00"
}
```

### Error Response Structure

```json
{
  "success": false,
  "data": null,
  "message": "Error description",
  "timestamp": "2026-01-07T14:30:00"
}
```

---

## Status Enums Reference

### User Status

- `ACTIVE` - Active user account
- `SUSPENDED` - Temporarily suspended
- `DELETED` - Soft deleted account
- `INACTIVE` - Deactivated by user

### User Roles

- `ADMIN` - Platform administrator
- `MERCHANT` - Merchant owner
- `OUTLET_USER` - Outlet staff member
- `USER` - Regular customer

### Merchant Status

- `PENDING` - Awaiting approval
- `APPROVED` - Active and approved
- `SUSPENDED` - Temporarily suspended
- `REJECTED` - Registration rejected

### Outlet Status

- `ACTIVE` - Accepting orders
- `INACTIVE` - Temporarily closed
- `SUSPENDED` - Suspended by admin
- `PENDING` - Pending activation

### Order Status

- `CREATED` - Order created, not submitted
- `PENDING_OUTLET_ACCEPTANCE` - Waiting for outlet to accept
- `PAID` - Payment captured, accepted by outlet
- `PREPARING` - Being prepared
- `READY_FOR_PICKUP` - Ready for customer pickup
- `PICKED_UP` - Customer picked up
- `COMPLETED` - Order completed
- `CANCELLED` - Cancelled by customer
- `DECLINED` - Declined by outlet
- `EXPIRED` - Pickup window expired
- `REFUNDED` - Payment refunded

### Item Status

- `ACTIVE` - Available for sale
- `INACTIVE` - Not currently available
- `OUT_OF_STOCK` - Temporarily out of stock

### Item Categories

- `MEAL` - Complete meals
- `BAKERY` - Bakery products
- `GROCERY` - Grocery items
- `BEVERAGE` - Drinks
- `OTHER` - Other items

---

## Best Practices

### 1. **Pagination**

- Always use pagination for list endpoints
- Default page size is 20, max is 100
- Use `size=100` only when necessary

### 2. **Filtering**

- Combine multiple filters for precise results
- Use date ranges to limit data volume
- Use search for user-friendly text queries

### 3. **Sorting**

- Common sort fields: `createdAt`, `updatedAt`, `name`
- Use `desc` for newest first: `?sort=createdAt,desc`
- Multiple sorts: `?sort=status,asc&sort=createdAt,desc`

### 4. **Performance**

- Filter at API level instead of client-side
- Request only required fields if possible
- Use appropriate page sizes for mobile vs desktop

### 5. **Location-based Search**

- Use `latitude`, `longitude`, and `radiusKm` together
- Default radius is 10km if not specified
- Radius is calculated using Haversine formula

---

## Example Integration Code

### JavaScript/TypeScript (Fetch)

```javascript
async function fetchFilteredOutlets(filters) {
  const params = new URLSearchParams({
    page: filters.page || 0,
    size: filters.size || 20,
    ...(filters.city && { city: filters.city }),
    ...(filters.status && { status: filters.status }),
    ...(filters.latitude && { latitude: filters.latitude }),
    ...(filters.longitude && { longitude: filters.longitude }),
    ...(filters.radiusKm && { radiusKm: filters.radiusKm }),
  });

  const response = await fetch(`/api/v1/outlets?${params}`, {
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
  });

  return await response.json();
}

// Usage
const result = await fetchFilteredOutlets({
  city: "New York",
  status: "ACTIVE",
  latitude: 40.7128,
  longitude: -74.006,
  radiusKm: 5,
  page: 0,
  size: 20,
});
```

### React Query Example

```javascript
import { useQuery } from "@tanstack/react-query";

function useFilteredOrders(filters) {
  return useQuery({
    queryKey: ["orders", filters],
    queryFn: async () => {
      const params = new URLSearchParams(filters);
      const response = await fetch(`/api/v1/orders?${params}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      return response.json();
    },
  });
}

// Usage in component
const { data, isLoading } = useFilteredOrders({
  status: "PREPARING",
  dateFrom: "2026-01-07T00:00:00",
  page: 0,
  size: 20,
});
```

---

## Support

For additional questions or issues, contact the backend team or refer to the API specification in Swagger UI at `/swagger-ui.html`.

**Document Version:** 1.0  
**Last Updated:** January 7, 2026
