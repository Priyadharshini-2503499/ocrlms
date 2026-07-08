// =====================================================================
// layout.js — builds the shared sidebar + topbar on every page.
// Replaces the old Thymeleaf "layout :: sidebar / topbar" fragments.
// Call buildLayout({...}) once from each page's own JS.
// =====================================================================

import { getRole } from "./ui.js";
import { logout } from "./auth.js";

// Sidebar menu definition (matches the old layout.html groups).
// "base" is the relative path back to the frontend root from a module page.
const NAV_GROUPS = [
  {
    title: "Merchandising", roles: ["merchandiser", "admin"],
    links: [{ key: "products", label: "Product Catalog", href: "productcatalog/products.html" }],
  },
  {
    title: "Store Operations", roles: ["storemanager", "admin"],
    links: [
      { key: "orders", label: "Orders", href: "order-management/orders.html" },
      { key: "place-order", label: "Place Order", href: "order-management/place-order.html" },
    ],
  },
  {
    title: "Customer Care", roles: ["csa", "admin"],
    links: [
      { key: "customers", label: "Customers & Loyalty", href: "loyalty/customers.html" },
      { key: "returns", label: "Returns & Refunds", href: "returns/returns.html" },
    ],
  },
  {
    title: "Marketing", roles: ["marketing", "admin"],
    links: [{ key: "promotions", label: "Promotions", href: "promotion/index.html" }],
  },
];

// base = relative prefix to reach the frontend root (e.g. "../" from a module).
function sidebarHtml(active, base) {
  const role = getRole();

  const groups = NAV_GROUPS.map((g) => {
    const visible = g.roles.includes(role);
    const links = g.links
      .map((l) => {
        const cls = l.key === active ? "active" : "";
        return `<a href="${base}${l.href}" class="${cls}">${l.label}</a>`;
      })
      .join("");
    return `
      <div class="nav-group" style="${visible ? "" : "display:none;"}">
        <div class="grp">${g.title}</div>
        <nav class="nav">${links}</nav>
      </div>`;
  }).join("");

  return `
    <div class="brand">
      <div class="ring serif">A</div>
      <div class="name">Aurelia<small>Retail Console</small></div>
    </div>
    ${groups}
    <div class="side-foot">
      <a href="#" id="logout-btn" style="color:#9FB4A9;">&larr; Logout</a>
    </div>`;
}

function topbarHtml({ title, subtitle, role, initials }) {
  return `
    <div class="tt">
      <h1 class="serif">${title}</h1>
      <p>${subtitle || ""}</p>
    </div>
    <div class="who">
      <span class="role-pill">${role || ""}</span>
      <span class="avatar">${initials || ""}</span>
    </div>`;
}

/**
 * Build the shared shell into #sidebar and #topbar.
 * @param {Object} opts
 * @param {string} opts.active   - menu key to highlight (e.g. "products")
 * @param {string} opts.title    - topbar title
 * @param {string} opts.subtitle - topbar subtitle
 * @param {string} opts.role     - role label shown in the pill
 * @param {string} opts.initials - avatar initials
 * @param {string} [opts.base]   - relative path to frontend root (default "../")
 */
export function buildLayout(opts) {
  const base = opts.base ?? "../";
  const side = document.getElementById("sidebar");
  const top = document.getElementById("topbar");
  if (side) {
    side.className = "sidebar";
    side.innerHTML = sidebarHtml(opts.active, base);
  }
  if (top) {
    top.className = "topbar";
    top.innerHTML = topbarHtml(opts);
  }
  // Make sure a toast container exists.
  if (!document.querySelector(".toast-wrap")) {
    const wrap = document.createElement("div");
    wrap.className = "toast-wrap";
    document.body.appendChild(wrap);
  }
  // Attach logout handler
  const logoutBtn = document.getElementById("logout-btn");
  if (logoutBtn) {
    logoutBtn.addEventListener("click", (e) => {
      e.preventDefault();
      logout();
    });
  }
}

