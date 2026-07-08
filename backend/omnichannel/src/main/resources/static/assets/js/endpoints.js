// =====================================================================
// endpoints.js — EVERY backend call lives here, named once.
// When the real backend is ready, fix any path strings in THIS file only.
// The page JS never hard-codes a URL.
// =====================================================================

import { api } from "./api.js";

// auth-service  ->  /api/auth
export const AuthAPI = {
  login:      (body)  => api.post(`/api/auth/login`, body),
  listUsers:  ()      => api.get(`/api/auth/users`),
  createUser: (body)  => api.post(`/api/auth/users`, body),
};

// productcatalog-service  ->  /products
export const ProductAPI = {
  list:        ()           => api.get(`/products`),
  get:         (id)         => api.get(`/products/${id}`),
  create:      (body)       => api.post(`/products`, body),
  // price is a request param on the backend
  updatePrice: (id, price)  => api.put(`/products/${id}/price?basePrice=${Number(price)}`),
  deactivate:  (id)         => api.put(`/products/${id}/deactivate`),
};

// order-service  ->  /api/orders
export const OrderAPI = {
  list:         ()              => api.get(`/api/orders`),
  byCustomer:   (customerId)    => api.get(`/api/orders?customerId=${customerId}`),
  get:          (id)            => api.get(`/api/orders/${id}`),
  place:        (body)          => api.post(`/api/orders`, body),
  // status update is a PATCH; cancel is a POST
  updateStatus: (id, status)    => api.patch(`/api/orders/${id}/status`, { newStatus: status }),
  cancel:       (id)            => api.post(`/api/orders/${id}/cancel`),
};

// loyalty-service  ->  /api/customers
// NOTE: redeem uses a REQUEST PARAM (points).
export const CustomerAPI = {
  list:     ()             => api.get(`/api/customers`),
  get:      (id)           => api.get(`/api/customers/${id}`),
  register: (body)         => api.post(`/api/customers`, body),
  redeem:   (id, points)   => api.post(`/api/customers/${id}/redeem?points=${Number(points)}`),
  addPoints:(id, points)   => api.post(`/api/customers/${id}/add-points?points=${Number(points)}`),
};

// returns-service  ->  /api/returns
export const ReturnAPI = {
  list:    ()       => api.get(`/api/returns`),
  get:     (id)     => api.get(`/api/returns/${id}`),
  create:  (body)   => api.post(`/api/returns`, body),
  // approve / reject / refund are POST on the backend
  approve: (id)     => api.post(`/api/returns/${id}/approve`),
  reject:  (id)     => api.post(`/api/returns/${id}/reject`),
  refund:  (id)     => api.post(`/api/returns/${id}/refund`),
};

// promotions-service  ->  /api/promotions
export const PromotionAPI = {
  list:   ()              => api.get(`/api/promotions/coupons`),
  create: (body)          => api.post(`/api/promotions/coupons`, body),
  apply:  (code, amount)  => api.post(`/api/promotions/apply?code=${encodeURIComponent(code)}&amount=${Number(amount)}`),
};

