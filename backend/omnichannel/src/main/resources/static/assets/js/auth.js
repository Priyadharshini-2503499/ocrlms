// =====================================================================
// auth.js — JWT session helpers (token + user identity) used app-wide.
// The token is issued by auth-service (/api/auth/login) and verified at
// the API Gateway on every request.
// =====================================================================

const TOKEN_KEY = "auriaToken";
const USER_KEY = "auriaUser";
const ROLE_KEY = "auriaRole"; // nav role used by layout/sidebar (lowercase)

// Map backend roles (auth-service Role enum) -> frontend nav roles.
const NAV_ROLE = {
  ADMIN: "admin",
  MERCHANDISER: "merchandiser",
  STORE_MANAGER: "storemanager",
  CUSTOMER_SERVICE: "csa",
  MARKETING_MANAGER: "marketing",
};

const MODULE_FOLDERS = [
  "loyalty", "order-management", "productcatalog",
  "promotion", "returns", "admin",
];

export function setSession({ token, username, role }) {
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(USER_KEY, username || "");
  localStorage.setItem(ROLE_KEY, NAV_ROLE[role] || "admin");
  localStorage.setItem("auriaBackendRole", role || "");
}

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function getUsername() {
  return localStorage.getItem(USER_KEY) || "";
}

export function getBackendRole() {
  return localStorage.getItem("auriaBackendRole") || "";
}

// Nav role (lowercase) derived from the JWT at login. Drives sidebar visibility.
export function getNavRole() {
  return localStorage.getItem(ROLE_KEY) || "admin";
}

// Where each role lands after sign-in / when visiting the root.
const LANDING = {
  admin: "admin/users.html",
  merchandiser: "productcatalog/products.html",
  storemanager: "order-management/orders.html",
  csa: "loyalty/customers.html",
  marketing: "promotion/index.html",
};

export function landingFor(navRole = getNavRole(), base = "") {
  return base + (LANDING[navRole] || "admin/users.html");
}

export function isAdmin() {
  return getBackendRole() === "ADMIN";
}

export function isLoggedIn() {
  return !!getToken();
}

export function clearSession() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
  localStorage.removeItem(ROLE_KEY);
  localStorage.removeItem("auriaBackendRole");
}

// Relative path to login.html from wherever we currently are.
export function loginPath() {
  const inModule = MODULE_FOLDERS.some((f) => window.location.pathname.includes(`/${f}/`));
  return inModule ? "../login.html" : "login.html";
}

export function redirectToLogin() {
  window.location.href = loginPath();
}

// Guard for protected pages — call early. Redirects to login if no token.
export function requireAuth() {
  if (!isLoggedIn()) {
    redirectToLogin();
    return false;
  }
  return true;
}

export function logout() {
  clearSession();
  redirectToLogin();
}

