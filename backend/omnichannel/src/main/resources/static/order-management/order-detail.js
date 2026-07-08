// order-detail.html — order summary, line items, manage actions
import { buildLayout } from "../assets/js/layout.js";
import { OrderAPI } from "../assets/js/endpoints.js";
import { money, badge, escapeHtml, showToast, getParam, el } from "../assets/js/ui.js";

buildLayout({
  active: "orders",
  title: "Order Detail",
  subtitle: "Line items & fulfillment",
  role: "Store Manager",
  initials: "SM",
});

const id = getParam("id");

function isClosed(status) {
  return status === "DELIVERED" || status === "CANCELLED";
}

function itemRow(it) {
  return `
    <tr>
      <td>
        <div style="font-weight:600;color:var(--ink);">${escapeHtml(it.productName)}</div>
        <div class="sub">#${it.productId}</div>
      </td>
      <td class="price mono">${money(it.unitPrice)}</td>
      <td>${it.quantity}</td>
      <td class="price mono" style="text-align:right;">${money(it.lineTotal)}</td>
    </tr>`;
}

function render(o) {
  const manage = isClosed(o.orderStatus)
    ? `<span class="text-muted-soft">This order is closed.</span>`
    : `
      <a href="update-status.html?id=${o.orderId}" class="btn btn-primary btn-sm">Update status</a>
      <button class="btn btn-rust btn-sm" id="cancel-btn">Cancel order</button>`;

  el("view").innerHTML = `
    <div style="display:flex; flex-direction:column; gap:22px;">

      <div class="grid cols-2" style="align-items:stretch; gap:22px;">
        <div class="card">
          <div class="card-h">
            <div><h2>Order #${o.orderId}</h2><p>${escapeHtml(o.orderChannel)} · ${escapeHtml(o.orderDate)}</p></div>
            ${badge(o.orderStatus)}
          </div>
          <div class="card-b">
            <div style="display:flex; flex-direction:column; gap:6px; margin-bottom:24px;">
              <span class="k" style="color:var(--muted); font-size:11px; text-transform:uppercase; letter-spacing:.08em; font-weight:600;">Order total</span>
              <span class="serif mono" style="font-size:34px; font-weight:500; color:var(--ink);">${money(o.totalAmount)}</span>
              ${o.couponCode ? `<span class="bdg b-gold" style="align-self:flex-start; margin-top:4px;"><span class="d"></span><span>Coupon ${escapeHtml(o.couponCode)}</span></span>` : ""}
            </div>
            <dl class="kv">
              <dt>Customer</dt><dd>#${o.customerId}</dd>
              <dt>Channel</dt><dd>${escapeHtml(o.orderChannel)}</dd>
              <dt>Order date</dt><dd class="mono">${escapeHtml(o.orderDate)}</dd>
              ${o.couponCode ? `<dt>Coupon applied</dt><dd class="mono">${escapeHtml(o.couponCode)}</dd>` : ""}
            </dl>
          </div>
        </div>

        <div class="card">
          <div class="card-h"><div><h2>Manage</h2><p>Advance status or cancel.</p></div></div>
          <div class="card-b">
            <div class="row-actions" style="justify-content:flex-start; gap:10px;">${manage}</div>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-h"><div><h2>Line items</h2><p>${(o.items || []).length} product(s)</p></div></div>
        <div class="table-wrap">
          <table>
            <thead><tr><th>Product</th><th>Unit price</th><th>Qty</th><th style="text-align:right;">Line total</th></tr></thead>
            <tbody>${(o.items || []).map(itemRow).join("")}</tbody>
            <tfoot>
              <tr>
                <td colspan="3" style="text-align:right;font-weight:600;color:var(--ink);">Total</td>
                <td class="price mono" style="text-align:right;font-weight:700;">${money(o.totalAmount)}</td>
              </tr>
            </tfoot>
          </table>
        </div>
      </div>

    </div>`;

  const cancelBtn = el("cancel-btn");
  if (cancelBtn) {
    cancelBtn.addEventListener("click", async () => {
      if (!confirm("Cancel this order?")) return;
      try {
        await OrderAPI.cancel(id);
        showToast("Order cancelled.");
        load();
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

