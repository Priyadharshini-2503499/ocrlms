// orders.html — list orders (optionally filtered by ?customerId=)
import { buildLayout } from "../assets/js/layout.js";
import { OrderAPI } from "../assets/js/endpoints.js";
import { money, badge, escapeHtml, showToast, getParam, el } from "../assets/js/ui.js";

buildLayout({
  active: "orders",
  title: "Orders",
  subtitle: "Multi-channel fulfillment",
  role: "Store Manager",
  initials: "SM",
});

const customerId = getParam("customerId");
if (customerId) {
  el("list-title").textContent = `Orders for customer #${customerId}`;
}

function isClosed(status) {
  return status === "DELIVERED" || status === "CANCELLED";
}

function rowHtml(o) {
  const actions = isClosed(o.orderStatus)
    ? `<a href="order-detail.html?id=${o.orderId}" class="btn btn-ghost btn-sm">View</a>`
    : `
      <a href="order-detail.html?id=${o.orderId}" class="btn btn-ghost btn-sm">View</a>
      <a href="update-status.html?id=${o.orderId}" class="btn btn-ghost btn-sm">Status</a>
      <button class="btn btn-rust btn-sm" data-cancel="${o.orderId}">Cancel</button>`;

  return `
    <tr>
      <td>
        <div style="font-weight:600;color:var(--ink);">#${o.orderId}</div>
        <div class="sub">${(o.items || []).length} item(s)</div>
      </td>
      <td>#${o.customerId}</td>
      <td>${escapeHtml(o.orderChannel)}</td>
      <td class="mono">${escapeHtml(o.orderDate)}</td>
      <td class="price mono">${money(o.totalAmount)}</td>
      <td>${badge(o.orderStatus)}</td>
      <td class="row-actions">${actions}</td>
    </tr>`;
}

function emptyHtml() {
  return `
    <tr><td colspan="7">
      <div class="empty">
        <div class="big">🧾</div>
        No orders found. <a href="place-order.html" class="text-link">Place an order</a>.
      </div>
    </td></tr>`;
}

async function load() {
  const rows = el("rows");
  try {
    const orders = customerId ? await OrderAPI.byCustomer(customerId) : await OrderAPI.list();
    rows.innerHTML = orders.length ? orders.map(rowHtml).join("") : emptyHtml();

    rows.querySelectorAll("[data-cancel]").forEach((btn) => {
      btn.addEventListener("click", async () => {
        if (!confirm("Cancel this order?")) return;
        try {
          await OrderAPI.cancel(btn.getAttribute("data-cancel"));
          showToast("Order cancelled.");
          load();
        } catch (err) { showToast(err.message); }
      });
    });
  } catch (err) {
    rows.innerHTML = `<tr><td colspan="7"><div class="empty">Could not load orders.</div></td></tr>`;
    showToast(err.message);
  }
}

load();

