// update-status.html — advance an order to its next allowed status
import { buildLayout } from "../assets/js/layout.js";
import { OrderAPI } from "../assets/js/endpoints.js";
import { badge, showToast, getParam, el } from "../assets/js/ui.js";

buildLayout({
  active: "orders",
  title: "Update Status",
  subtitle: "Advance order fulfillment",
  role: "Store Manager",
  initials: "SM",
});

const id = getParam("id");

// Same flow rules as the backend.
const ORDER_FLOW = {
  PLACED: ["CONFIRMED", "CANCELLED"],
  CONFIRMED: ["SHIPPED", "CANCELLED"],
  SHIPPED: ["DELIVERED"],
  DELIVERED: [],
  CANCELLED: [],
};

function render(o) {
  el("back-link").href = `order-detail.html?id=${o.orderId}`;
  const allowed = ORDER_FLOW[o.orderStatus] || [];

  const body = allowed.length
    ? `
      <form id="status-form">
        <div class="field">
          <label>New status</label>
          <select name="newStatus">
            ${allowed.map((s) => `<option value="${s}">${s}</option>`).join("")}
          </select>
        </div>
        <div class="form-actions">
          <a href="order-detail.html?id=${o.orderId}" class="btn btn-ghost">Cancel</a>
          <button type="submit" class="btn btn-primary">Update status</button>
        </div>
      </form>`
    : `<div class="alert alert-info">No further status changes are allowed for this order.</div>`;

  el("view").innerHTML = `
    <div class="card" style="max-width:520px;">
      <div class="card-h">
        <div><h2>Order #${o.orderId}</h2><p>Choose the next status.</p></div>
        ${badge(o.orderStatus)}
      </div>
      <div class="card-b">${body}</div>
    </div>`;

  const form = el("status-form");
  if (form) {
    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      try {
        await OrderAPI.updateStatus(id, form.newStatus.value);
        showToast("Status updated.");
        setTimeout(() => (window.location.href = `order-detail.html?id=${o.orderId}`), 600);
      } catch (err) { showToast(err.message); }
    });
  }
}

async function load() {
  try {
    const o = await OrderAPI.get(id);
    render(o);
  } catch (err) {
    el("view").innerHTML = `<div class="empty">Could not load order.</div>`;
    showToast(err.message);
  }
}

load();

