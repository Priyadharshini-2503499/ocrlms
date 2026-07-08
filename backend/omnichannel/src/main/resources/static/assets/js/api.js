// =====================================================================
// api.js — the fetch helper. All calls go to CONFIG.BASE_URL.
// Sends JWT token in Authorization header for authenticated requests.
// =====================================================================

import { CONFIG } from "./config.js";
import { getToken, clearSession } from "./auth.js";

async function request(method, path, body) {
  const headers = { "Content-Type": "application/json" };

  // Attach JWT token if available
  const token = getToken();
  if (token) {
    headers["Authorization"] = "Bearer " + token;
  }

  const res = await fetch(CONFIG.BASE_URL + path, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  // If 401/403 — token expired or unauthorized, redirect to login
  if (res.status === 401 || res.status === 403) {
    let message = "Access denied.";
    try {
      const data = await res.json();
      message = data.message || message;
    } catch (_) {}

    if (res.status === 401) {
      clearSession();
      // Don't redirect if already on the login page
      const onLoginPage = window.location.pathname.endsWith("login.html") || window.location.pathname.endsWith("login");
      if (!onLoginPage) {
        window.location.href = window.location.pathname.includes("/")
          && window.location.pathname.split("/").length > 2
          ? "../login.html" : "login.html";
      }
    }

    const err = new Error(message);
    err.status = res.status;
    throw err;
  }

  if (!res.ok) {
    let message = `Request failed (${res.status})`;
    try {
      const text = await res.text();
      if (text) {
        try {
          const data = JSON.parse(text);
          if (data && data.message) {
            message = data.message;
          } else if (data && data.fieldErrors && typeof data.fieldErrors === "object") {
            message = Object.values(data.fieldErrors).join(", ") || message;
          } else if (data && data.error) {
            message = data.error;
          }
        } catch (_) {
          message = text.length <= 200 ? text : message;
        }
      }
    } catch (_) {}

    const err = new Error(message);
    err.status = res.status;
    throw err;
  }

  if (res.status === 204) return null;
  const text = await res.text();
  return text ? JSON.parse(text) : null;
}

export const api = {
  get:   (p)    => request("GET", p),
  post:  (p, b) => request("POST", p, b),
  put:   (p, b) => request("PUT", p, b),
  patch: (p, b) => request("PATCH", p, b),
  del:   (p)    => request("DELETE", p),
};

