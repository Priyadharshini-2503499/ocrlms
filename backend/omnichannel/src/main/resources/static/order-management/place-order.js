// place-order.html — create a multi-item order
import { buildLayout } from "../assets/js/layout.js";
import { OrderAPI, ProductAPI, PromotionAPI } from "../assets/js/endpoints.js";
import { showToast, showFormError, escapeHtml, money, el } from "../assets/js/ui.js";

buildLayout({
  active: "place-order",
  title: "Place Order",
  subtitle: "Create a multi-item order",
  role: "Store Manager",
  initials: "SM",
});

let products = [];

function optionsHtml() {
  return (
    `<option value="">Select a product…</option>` +
    products
      .filter((p) => p.productStatus === "ACTIVE")
      .map((p) => `<option value="${p.productId}">${escapeHtml(p.productName)} — ₹${p.basePrice}</option>`)
      .join("")
  );
}

function addRow() {
  const tr = document.createElement("tr");
  tr.innerHTML = `
    <td><select class="item-product" required>${optionsHtml()}</select></td>
    <td><input type="number" min="1" class="item-qty" placeholder="1" required/></td>
    <td><button type="button" class="btn btn-ghost btn-sm remove-item">✕</button></td>`;
  el("item-rows").appendChild(tr);
}

el("add-item").addEventListener("click", addRow);

el("item-rows").addEventListener("click", (e) => {
  const btn = e.target.closest(".remove-item");
  if (!btn) return;
  btn.closest("tr").remove();
  resetCouponPreview();
});

// Re-collect the selected line items from the table.
function collectItems() {
  const items = [];
  el("item-rows").querySelectorAll("tr").forEach((row) => {
    const productId = row.querySelector(".item-product").value;
    const quantity = Number(row.querySelector(".item-qty").value);
    if (productId && quantity >= 1) items.push({ productId: Number(productId), quantity });
  });
  return items;
}

// Compute the subtotal client-side from current product prices.
function computeSubtotal(items) {
  return items.reduce((sum, it) => {
    const p = products.find((pr) => Number(pr.productId) === Number(it.productId));
    return sum + (p ? Number(p.basePrice) * it.quantity : 0);
  }, 0);
}

function showCouponBox(html, kind) {
  const box = el("coupon-result");
  box.className = "alert " + (kind === "ok" ? "alert-ok" : kind === "info" ? "alert-info" : "alert-error");
  box.innerHTML = html;
  box.style.display = "";
}

function resetCouponPreview() {
  const box = el("coupon-result");
  if (box) box.style.display = "none";
}

// Preview the discount via the promotions service (does not place the order).
el("apply-coupon").addEventListener("click", async () => {
  const form = el("order-form");
  const code = form.couponCode.value.trim();
  if (!code) {
    showCouponBox("Enter a coupon code to preview the discount.", "error");
    return;
  }
  const items = collectItems();
  if (!items.length) {
    showCouponBox("Add at least one item before applying a coupon.", "error");
    return;
  }
  const subtotal = computeSubtotal(items);
  if (!(subtotal > 0)) {
    showCouponBox("Subtotal must be greater than zero to apply a coupon.", "error");
    return;
  }
  showCouponBox("Checking coupon…", "info");
  try {
    const res = await PromotionAPI.apply(code, subtotal);
    const finalAmount = Number(res.finalAmount);
    const discount = subtotal - finalAmount;
    if (discount > 0) {
      showCouponBox(
        `<div style="font-weight:600;">Coupon ${escapeHtml(code)} applied.</div>
         <div style="margin-top:6px;display:flex;flex-direction:column;gap:2px;">
           <span>Subtotal: <strong class="mono">${money(subtotal)}</strong></span>
           <span>Discount: <strong class="mono">−${money(discount)}</strong></span>
           <span>New total: <strong class="mono">${money(finalAmount)}</strong></span>
         </div>`,
        "ok"
      );
    } else {
      showCouponBox("Coupon is valid but gives no discount on this order.", "info");
    }
  } catch (err) {
    showCouponBox(err.message, "error");
  }
});

// If the user edits the code or item quantities, clear the stale preview.
el("order-form").addEventListener("input", (e) => {
  if (e.target.name === "couponCode" || e.target.classList.contains("item-qty")) {
    resetCouponPreview();
  }
});
el("order-form").addEventListener("change", (e) => {
  if (e.target.classList.contains("item-product")) resetCouponPreview();
});

el("order-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  showFormError("");

  const f = e.target;
  const customerId = Number(f.customerId.value);
  if (!(customerId >= 1)) {
    showFormError("Please enter a valid Customer ID.");
    return;
  }

  const items = collectItems();

  if (!items.length) {
    showFormError("Add at least one item with a product and quantity.");
    return;
  }

  const couponCode = f.couponCode.value.trim();

  try {
    const order = await OrderAPI.place({
      customerId,
      orderChannel: f.orderChannel.value,
      items,
      couponCode: couponCode || null,
    });
    // Let the user know whether the coupon actually applied.
    if (couponCode && order && order.couponCode) {
      showToast(`Order placed. Coupon ${order.couponCode} applied.`);
    } else if (couponCode) {
      showToast("Order placed. Coupon was not applied (invalid or no discount).");
    } else {
      showToast("Order placed.");
    }
    setTimeout(() => (window.location.href = "orders.html"), 800);
  } catch (err) {
    showFormError(err.message);
  }
});

async function init() {
  try {
    products = await ProductAPI.list();
  } catch (err) {
    showToast("Could not load products: " + err.message);
    products = [];
  }
  addRow(); // start with one empty row
}

init();

