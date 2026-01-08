# Frontend Implementation Quick Start Guide

## 🎯 API Design Philosophy

**Unified Endpoints with Automatic Scope Filtering**: The API uses a single endpoint pattern for each resource type. The backend automatically applies scope filtering based on the authenticated user's role:

- **ADMIN/SUPER_ADMIN**: See all data (global access)
- **MERCHANT**: See only their merchant's data
- **OUTLET_USER**: See only their outlet's data
- **USER**: See only their own data

This means the same endpoint works differently based on who calls it!

---

## 🚀 Quick Reference for Common Use Cases

### 1. **Customer App - Find Nearby Outlets**

```javascript
// Search for nearby active outlets
GET /api/v1/outlets?latitude=40.7128&longitude=-74.0060&radiusKm=5&status=ACTIVE

// Expected result: 5-10 active outlets within 5km
```

### 2. **Any Dashboard - View Orders (Scoped Automatically)**

```javascript
// Same endpoint for all roles - backend filters automatically!
const today = new Date().toISOString().split('T')[0];
GET /api/v1/orders?dateFrom=${today}T00:00:00&dateTo=${today}T23:59:59&page=0&size=50

// ADMIN sees: All orders across all outlets
// MERCHANT sees: Orders for their merchant's outlets only
// OUTLET_USER sees: Orders for their specific outlet only
// USER sees: Their own orders only
```

### 3. **Admin Panel - Monitor Pending Merchants**

```javascript
// Get all pending merchant applications
GET /api/v1/merchants/list?status=PENDING&sort=createdAt,desc&page=0&size=20

// Expected result: Latest merchant applications awaiting approval
```

### 4. **Customer App - Browse Available Secret Boxes**

```javascript
// Find secret boxes under $20 near user
GET /api/v1/items/search?itemType=SECRET_BOX&maxDiscountedPrice=20&status=ACTIVE&page=0&size=20

// Expected result: Affordable secret boxes available for purchase
```

### 5. **Dashboard - Active Orders Needing Attention**

```javascript
// Get orders pending acceptance or being prepared - same endpoint for all roles!
GET /api/v1/orders?status=PENDING_OUTLET_ACCEPTANCE&page=0&size=20

// Get orders currently being prepared
GET /api/v1/orders?status=PREPARING&page=0&size=20

// Expected result: Orders requiring immediate action (scoped by user's access)
```

### 6. **Customer App - Order History**

```javascript
// Last 30 days orders
const thirtyDaysAgo = new Date(Date.now() - 30*24*60*60*1000).toISOString();
GET /api/v1/orders?dateFrom=${thirtyDaysAgo}&sort=createdAt,desc&page=0&size=20

// Expected result: Recent order history (user sees only their own orders)
```

---

## 📱 Mobile App Examples

### React Native - Nearby Outlets

```javascript
import { useState, useEffect } from "react";
import * as Location from "expo-location";

function useNearbyOutlets(radiusKm = 5) {
  const [outlets, setOutlets] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchNearbyOutlets() {
      // Get user location
      const { coords } = await Location.getCurrentPositionAsync({});

      // Fetch nearby outlets
      const params = new URLSearchParams({
        latitude: coords.latitude,
        longitude: coords.longitude,
        radiusKm: radiusKm,
        status: "ACTIVE",
        page: 0,
        size: 20,
      });

      const response = await fetch(`${API_BASE}/outlets?${params}`, {
        headers: { Authorization: `Bearer ${token}` },
      });

      const result = await response.json();
      setOutlets(result.data.content);
      setLoading(false);
    }

    fetchNearbyOutlets();
  }, [radiusKm]);

  return { outlets, loading };
}
```

### Flutter - Filter Orders (Same Endpoint for All Roles!)

```dart
Future<OrderListResponse> fetchFilteredOrders({
  OrderStatus? status,
  DateTime? dateFrom,
  DateTime? dateTo,
  int page = 0,
  int size = 20,
}) async {
  // Same endpoint works for Customer, Merchant, Outlet Staff, Admin
  // Backend automatically filters based on user's role!
  final queryParams = {
    'page': page.toString(),
    'size': size.toString(),
    if (status != null) 'status': status.name.toUpperCase(),
    if (dateFrom != null) 'dateFrom': dateFrom.toIso8601String(),
    if (dateTo != null) 'dateTo': dateTo.toIso8601String(),
  };

  final uri = Uri.parse('$baseUrl/orders').replace(queryParameters: queryParams);

  final response = await http.get(
    uri,
    headers: {'Authorization': 'Bearer $token'},
  );

  return OrderListResponse.fromJson(jsonDecode(response.body));
}

// Usage - Same code works for any user role!
final orders = await fetchFilteredOrders(
  status: OrderStatus.PREPARING,
  dateFrom: DateTime.now().subtract(Duration(days: 7)),
  page: 0,
  size: 20,
);
```

---

## 💻 Web App Examples

### Vue.js - Item Search with Debounce

```vue
<template>
  <div>
    <input
      v-model="searchQuery"
      @input="debouncedSearch"
      placeholder="Search items..."
    />
    <select v-model="selectedCategory" @change="searchItems">
      <option value="">All Categories</option>
      <option value="MEAL">Meals</option>
      <option value="BAKERY">Bakery</option>
      <option value="BEVERAGE">Beverages</option>
    </select>

    <div v-for="item in items" :key="item.id">
      {{ item.name }} - ${{ item.discountedPrice }}
    </div>
  </div>
</template>

<script>
import { ref, watch } from "vue";
import { debounce } from "lodash";

export default {
  setup() {
    const searchQuery = ref("");
    const selectedCategory = ref("");
    const items = ref([]);

    const searchItems = async () => {
      const params = new URLSearchParams({
        page: 0,
        size: 20,
        status: "ACTIVE",
        ...(searchQuery.value && { search: searchQuery.value }),
        ...(selectedCategory.value && { category: selectedCategory.value }),
      });

      const response = await fetch(`/api/v1/items/search?${params}`);
      const result = await response.json();
      items.value = result.data.content;
    };

    const debouncedSearch = debounce(searchItems, 500);

    return {
      searchQuery,
      selectedCategory,
      items,
      debouncedSearch,
      searchItems,
    };
  },
};
</script>
```

### Angular - Merchant Dashboard Service

```typescript
import { Injectable } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";
import { Observable } from "rxjs";

export interface MerchantFilters {
  status?: string;
  category?: string;
  search?: string;
  dateFrom?: string;
  page?: number;
  size?: number;
}

@Injectable({ providedIn: "root" })
export class MerchantService {
  private baseUrl = "/api/v1/admin/merchants";

  constructor(private http: HttpClient) {}

  getMerchants(filters: MerchantFilters): Observable<any> {
    let params = new HttpParams()
      .set("page", (filters.page || 0).toString())
      .set("size", (filters.size || 20).toString());

    if (filters.status) params = params.set("status", filters.status);
    if (filters.category) params = params.set("category", filters.category);
    if (filters.search) params = params.set("search", filters.search);
    if (filters.dateFrom) params = params.set("dateFrom", filters.dateFrom);

    return this.http.get(this.baseUrl, { params });
  }

  // Usage in component
  // this.merchantService.getMerchants({ status: 'APPROVED', page: 0, size: 20 })
  //   .subscribe(response => this.merchants = response.data.content);
}
```

---

## 🔍 Advanced Filtering Examples

### 1. **Multi-Status Order Filtering**

```javascript
// Get orders in multiple states (for dashboard overview)
GET /api/v1/outlet/orders?status=PENDING_OUTLET_ACCEPTANCE&page=0&size=50

// Fetch separately and combine client-side
const [pending, preparing] = await Promise.all([
  fetch('/api/v1/outlet/orders?status=PENDING_OUTLET_ACCEPTANCE'),
  fetch('/api/v1/outlet/orders?status=PREPARING')
]);
```

### 2. **Date Range with Pickup Time**

```javascript
// Orders for specific pickup window
const params = new URLSearchParams({
  pickupTimeFrom: '2026-01-07T17:00:00',
  pickupTimeTo: '2026-01-07T19:00:00',
  status: 'READY_FOR_PICKUP',
  page: 0,
  size: 50
});

GET /api/v1/outlet/orders?${params}
// Expected: Orders ready for pickup between 5-7 PM
```

### 3. **Price Range Item Search**

```javascript
// Budget-friendly items ($5-$15)
GET /api/v1/items/search?minDiscountedPrice=5&maxDiscountedPrice=15&status=ACTIVE&page=0&size=20

// Premium items (over $30)
GET /api/v1/items/search?minDiscountedPrice=30&status=ACTIVE&page=0&size=20
```

### 4. **Location-Based with Category Filter**

```javascript
// Find bakeries within 3km
GET /api/v1/outlets?latitude=40.7128&longitude=-74.0060&radiusKm=3&search=bakery&status=ACTIVE&page=0&size=10
```

---

## 🎯 State Management Integration

### Redux Toolkit (RTK Query)

```javascript
import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";

export const resqeatsApi = createApi({
  reducerPath: "resqeatsApi",
  baseQuery: fetchBaseQuery({
    baseUrl: "/api/v1",
    prepareHeaders: (headers, { getState }) => {
      const token = getState().auth.token;
      if (token) headers.set("authorization", `Bearer ${token}`);
      return headers;
    },
  }),
  endpoints: (builder) => ({
    getFilteredOrders: builder.query({
      query: (filters) => ({
        url: "/orders",
        params: filters,
      }),
    }),
    getFilteredOutlets: builder.query({
      query: (filters) => ({
        url: "/outlets",
        params: filters,
      }),
    }),
  }),
});

export const { useGetFilteredOrdersQuery, useGetFilteredOutletsQuery } =
  resqeatsApi;

// Usage in component
function OrderList() {
  const { data, isLoading } = useGetFilteredOrdersQuery({
    status: "PREPARING",
    page: 0,
    size: 20,
  });

  return <div>{/* Render orders */}</div>;
}
```

### Zustand Store

```javascript
import create from "zustand";

const useOutletStore = create((set, get) => ({
  outlets: [],
  filters: {
    status: "ACTIVE",
    city: "",
    latitude: null,
    longitude: null,
    radiusKm: 5,
    page: 0,
    size: 20,
  },

  setFilters: (newFilters) =>
    set((state) => ({
      filters: { ...state.filters, ...newFilters },
    })),

  fetchOutlets: async () => {
    const { filters } = get();
    const params = new URLSearchParams(
      Object.entries(filters).filter(([_, v]) => v != null)
    );

    const response = await fetch(`/api/v1/outlets?${params}`);
    const result = await response.json();
    set({ outlets: result.data.content });
  },
}));

// Usage
const { outlets, setFilters, fetchOutlets } = useOutletStore();
```

---

## ⚡ Performance Tips

### 1. **Pagination Best Practices**

```javascript
// ❌ Bad: Fetching too many items
GET /api/v1/orders?size=1000

// ✅ Good: Use reasonable page size
GET /api/v1/orders?size=20&page=0

// ✅ Better: Implement infinite scroll
async function loadMoreOrders(page) {
  const response = await fetch(`/api/v1/orders?size=20&page=${page}`);
  const result = await response.json();
  return result.data.content;
}
```

### 2. **Efficient Filtering**

```javascript
// ❌ Bad: Fetching all and filtering client-side
const response = await fetch("/api/v1/items/search?size=1000");
const filtered = response.data.content.filter(
  (item) => item.category === "BAKERY"
);

// ✅ Good: Filter at API level
const response = await fetch("/api/v1/items/search?category=BAKERY&size=20");
```

### 3. **Caching Strategy**

```javascript
// Use React Query with stale time
const { data } = useQuery({
  queryKey: ["outlets", filters],
  queryFn: () => fetchOutlets(filters),
  staleTime: 5 * 60 * 1000, // 5 minutes
  cacheTime: 10 * 60 * 1000, // 10 minutes
});
```

---

## 🐛 Common Pitfalls & Solutions

### 1. **Date Formatting**

```javascript
// ❌ Bad: Wrong date format
dateFrom: "01/07/2026";

// ✅ Good: ISO 8601 format
dateFrom: "2026-01-07T00:00:00";

// Helper function
function toISODateString(date) {
  return new Date(date).toISOString().split(".")[0];
}
```

### 2. **Empty Filter Parameters**

```javascript
// ❌ Bad: Sending null/undefined values
const params = {
  status: selectedStatus, // might be null
  search: searchQuery, // might be empty string
};

// ✅ Good: Filter out empty values
const params = Object.entries({
  status: selectedStatus,
  search: searchQuery,
}).reduce((acc, [key, value]) => {
  if (value !== null && value !== undefined && value !== "") {
    acc[key] = value;
  }
  return acc;
}, {});
```

### 3. **Pagination Reset**

```javascript
// ❌ Bad: Keeping old page when filters change
function updateCategory(newCategory) {
  setCategory(newCategory);
  fetchItems(); // Still on page 5!
}

// ✅ Good: Reset to page 0 when filters change
function updateCategory(newCategory) {
  setCategory(newCategory);
  setPage(0);
  fetchItems();
}
```

---

## 📊 Response Handling

### Success with Data

```javascript
{
  "success": true,
  "data": {
    "content": [...],
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8
  },
  "message": null
}
```

### Empty Results

```javascript
{
  "success": true,
  "data": {
    "content": [],
    "page": 0,
    "size": 20,
    "totalElements": 0,
    "totalPages": 0
  },
  "message": null
}
```

### Error Handling

```javascript
async function fetchWithErrorHandling(url) {
  try {
    const response = await fetch(url);
    const result = await response.json();

    if (!result.success) {
      throw new Error(result.message || "API Error");
    }

    return result.data;
  } catch (error) {
    console.error("Fetch error:", error);
    throw error;
  }
}
```

---

## 🔗 Quick Links

- [Full API Documentation](./API_FILTER_DOCUMENTATION.md)
- Swagger UI: `/swagger-ui.html`
- Postman Collection: `/Resqeats-Complete-API.postman_collection.json`

---

**Document Version:** 1.0  
**Last Updated:** January 7, 2026
