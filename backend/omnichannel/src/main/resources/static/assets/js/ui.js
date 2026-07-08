// =====================================================================
// ui.js — small shared helpers used by every page.
// (money format, toast, read id from URL, escape text, badge classes)
// =====================================================================

import { CONFIG } from "./config.js";

const ROLE_KEY = "auriaRole";

// ---- Role (saved on the landing page, read by the sidebar) ----------
export function getRole() {
  return localStorage.getItem(ROLE_KEY) || "admin";
}
export function setRole(role) {
  localStorage.setItem(ROLE_KEY, role);
}

// ---- Read a value from the query string (e.g. ?id=5) ----------------
// We use query params so pages work on any static host (Live Server).
// If you switch to path-style URLs later, only this function changes.
export function getParam(name) {
  return new URLSearchParams(window.location.search).get(name);
}

// ---- Money / text helpers ------------------------------------------
export function money(value) {
  const n = Number(value ?? 0);
  return CONFIG.CURRENCY + n.toLocaleString("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

export function escapeHtml(value) {
  return String(value ?? "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

// ---- Toast (same look/behaviour as the old app.js) ------------------
export function showToast(message) {
  if (!message) return;
  let wrap = document.querySelector(".toast-wrap");
  if (!wrap) {
    wrap = document.createElement("div");
    wrap.className = "toast-wrap";
    document.body.appendChild(wrap);
  }
  const t = document.createElement("div");
  t.className = "toast";
  t.textContent = message;
  wrap.appendChild(t);
  requestAnimationFrame(() => t.classList.add("show"));
  setTimeout(() => {
    t.classList.remove("show");
    setTimeout(() => t.remove(), 300);
  }, 3200);
}

// ---- Status badge markup (same colour rules as layout.html) ---------
const BADGE_CLASS = {
  DELIVERED: "b-green", APPROVED: "b-green", REFUNDED: "b-green", ACTIVE: "b-green",
  CONFIRMED: "b-info", SHIPPED: "b-info", REDEEMED: "b-info",
  CANCELLED: "b-rust", REJECTED: "b-rust", DISCONTINUED: "b-rust",
  PLACED: "b-gold", REQUESTED: "b-gold", GOLD: "b-gold",
  SILVER: "b-silver",
  PLATINUM: "b-platinum",
  INACTIVE: "b-gray", EXPIRED: "b-gray",
};
export function badge(label) {
  const cls = BADGE_CLASS[label] || "b-gray";
  return `<span class="bdg ${cls}"><span class="d"></span><span>${escapeHtml(label)}</span></span>`;
}

// ---- Tiny helpers for pages ----------------------------------------
export function qs(sel, root = document) { return root.querySelector(sel); }
export function el(id) { return document.getElementById(id); }

// Show a friendly inline error inside a form's .alert box.
export function showFormError(message, boxId = "form-error") {
  const box = document.getElementById(boxId);
  if (!box) return;
  if (message) {
    box.textContent = message;
    box.style.display = "";
  } else {
    box.style.display = "none";
  }
}

