import axios from "axios";

// API Configuration
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "";

// Create axios instance with default config
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor for error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    // Only redirect to login for auth-related endpoints, not for all 401s
    if (
      error.response?.status === 401 &&
      error.config?.url?.includes("/auth/")
    ) {
      // Token expired or invalid
      localStorage.removeItem("token");
      localStorage.removeItem("refreshToken");
      localStorage.removeItem("user");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

// Types
export interface User {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Order {
  id: number;
  orderNumber: string;
  userId: number;
  orderDate: string;
  totalAmount: number;
  status: string;
  orderItems: OrderItem[];
}

export interface OrderItem {
  id: number;
  productId: number;
  quantity: number;
  price: number;
}

export interface Product {
  id: number;
  sku: string;
  name: string;
  description: string;
  price: number;
  quantity: number;
  status: string;
}

export interface Notification {
  id: number;
  userId: number;
  title: string;
  message: string;
  type: string;
  isRead: boolean;
  createdAt: string;
}

export interface DashboardStats {
  totalUsers: number;
  activeUsers: number;
  totalOrders: number;
  totalRevenue: number;
  totalProducts: number;
  lowStockProducts: number;
  pendingNotifications: number;
}

// Auth API
export const authAPI = {
  login: (credentials: { username: string; password: string }) =>
    api.post("/api/auth/login", credentials),

  register: (userData: {
    username: string;
    email: string;
    password: string;
    firstName: string;
    lastName: string;
  }) => api.post("/api/auth/register", userData),

  validate: (token: string) => api.post("/api/auth/validate", { token }),

  refresh: (refreshToken: string) =>
    api.post("/api/auth/refresh", { refreshToken }),

  logout: () => api.post("/api/auth/logout"),
};

// Users API
export const usersAPI = {
  getAll: (params?: { page?: number; size?: number; search?: string }) =>
    api.get("/api/users", { params }),

  getById: (id: number) => api.get(`/api/users/${id}`),

  create: (userData: Partial<User>) => api.post("/api/users", userData),

  update: (id: number, userData: Partial<User>) =>
    api.put(`/api/users/${id}`, userData),

  delete: (id: number) => api.delete(`/api/users/${id}`),

  toggleStatus: (id: number) => api.patch(`/api/users/${id}/toggle-status`),
};

// Orders API
export const ordersAPI = {
  getAll: (params?: {
    page?: number;
    size?: number;
    userId?: number;
    status?: string;
  }) => api.get("/api/orders", { params }),

  getById: (id: number) => api.get(`/api/orders/${id}`),

  create: (orderData: {
    userId: number;
    orderItems: Array<{ productId: number; quantity: number; price: number }>;
  }) => api.post("/api/orders", orderData),

  updateStatus: (id: number, status: string) =>
    api.patch(`/api/orders/${id}/status`, { status }),

  cancel: (id: number) => api.patch(`/api/orders/${id}/cancel`),

  getByUser: (userId: number) => api.get(`/api/orders/user/${userId}`),
};

// Inventory API
export const inventoryAPI = {
  getProducts: (params?: {
    page?: number;
    size?: number;
    search?: string;
    status?: string;
  }) => api.get("/api/inventory/products", { params }),

  getProductById: (id: number) => api.get(`/api/inventory/products/${id}`),

  createProduct: (productData: {
    sku: string;
    name: string;
    description: string;
    price: number;
    quantity: number;
  }) => api.post("/api/inventory/products", productData),

  updateProduct: (id: number, productData: Partial<Product>) =>
    api.put(`/api/inventory/products/${id}`, productData),

  deleteProduct: (id: number) => api.delete(`/api/inventory/products/${id}`),

  checkStock: (productId: number, quantity: number) =>
    api.get(`/api/inventory/products/${productId}/stock`, {
      params: { quantity },
    }),

  reserveStock: (productId: number, quantity: number) =>
    api.post(`/api/inventory/products/${productId}/reserve`, { quantity }),

  releaseStock: (productId: number, quantity: number) =>
    api.post(`/api/inventory/products/${productId}/release`, { quantity }),

  getLowStockProducts: () => api.get("/api/inventory/products/low-stock"),
};

// Notifications API (uses HTTP Basic Auth)
const notificationsApi = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "",
  timeout: 10000,
  headers: {
    "Content-Type": "application/json",
  },
});

// Add HTTP Basic Auth for notifications
notificationsApi.interceptors.request.use(
  (config) => {
    // Use admin credentials for notifications service
    const credentials = btoa("admin:admin123");
    config.headers.Authorization = `Basic ${credentials}`;
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export const notificationsAPI = {
  getAll: (params?: { page?: number; size?: number; unreadOnly?: boolean }) =>
    notificationsApi.get("/api/notifications", { params }),

  getById: (id: number) => notificationsApi.get(`/api/notifications/${id}`),

  markAsRead: (id: number) =>
    notificationsApi.patch(`/api/notifications/${id}/read`),

  markAllAsRead: () => notificationsApi.patch("/api/notifications/read-all"),

  delete: (id: number) => notificationsApi.delete(`/api/notifications/${id}`),

  getUnreadCount: () => notificationsApi.get("/api/notifications/unread-count"),
};

// Dashboard API
export const dashboardAPI = {
  getStats: () => api.get("/api/dashboard/stats"),

  getRecentActivity: (limit?: number) =>
    api.get("/api/dashboard/activity", { params: { limit } }),

  getChartData: (period: "day" | "week" | "month" | "year") =>
    api.get("/api/dashboard/charts", { params: { period } }),
};

// GraphQL API for complex queries
export const graphqlAPI = {
  query: (query: string, variables?: Record<string, any>) =>
    api.post("/graphql", { query, variables }),
};

export default api;
