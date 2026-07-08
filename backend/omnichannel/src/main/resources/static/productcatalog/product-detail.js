// product-detail.html — view one product, update price, deactivate
import { buildLayout } from "../assets/js/layout.js";
import { ProductAPI } from "../assets/js/endpoints.js";
import { money, badge, escapeHtml, showToast, getParam, el } from "../assets/js/ui.js";

buildLayout({
  active: "products",
  title: "Product Detail",
  subtitle: "SKU lifecycle & pricing",
  role: "Merchandiser",
  initials: "MD",
});

const id = getParam("id");

function render(p) {
  const disableDeactivate = p.productStatus !== "ACTIVE" ? "disabled" : "";
  el("view").innerHTML = `
    <div class="grid cols-2" style="align-items:stretch; gap:22px;">

      <div class="card">
        <div class="card-h">
          <div>
            <h2>${escapeHtml(p.productName)}</h2>
            <p class="mono">${escapeHtml(p.skuCode)}</p>
          </div>
          ${badge(p.productStatus)}
        </div>
        <div class="card-b">
          <div style="display:flex; flex-direction:column; gap:6px; margin-bottom:24px;">
            <span class="k" style="color:var(--muted); font-size:11px; text-transform:uppercase; letter-spacing:.08em; font-weight:600;">Base price</span>
            <span class="serif mono" style="font-size:34px; font-weight:500; color:var(--ink);">${money(p.basePrice)}</span>
          </div>
          <dl class="kv">
            <dt>Product ID</dt><dd>#${p.productId}</dd>
            <dt>Category</dt><dd>${escapeHtml(p.category)}</dd>
            <dt>Status</dt><dd>${escapeHtml(p.productStatus)}</dd>
          </dl>
        </div>
      </div>

      <div class="card">
        <div class="card-h"><div><h2>Manage</h2><p>Update pricing or retire the SKU.</p></div></div>
        <div class="card-b">
          <form id="price-form">
            <div class="field">
              <label>Update base price</label>
              <div class="input-group">
                <span class="addon">₹</span>
                <input type="number" step="0.01" min="0" id="basePrice" value="${p.basePrice}" required/>
              </div>
              <span class="hint">Applies immediately across channels.</span>
            </div>
            <div class="form-actions">
              <button type="submit" class="btn btn-primary">Save price</button>
            </div>
          </form>

          <hr style="border:none;border-top:1px solid var(--line2);margin:20px 0;"/>

          <form id="deactivate-form">
            <div class="toolbar">
              <div>
                <div style="font-weight:600;">Deactivate product</div>
                <span class="hint">Hides it from new orders.</span>
              </div>
              <button type="submit" class="btn btn-rust btn-sm" ${disableDeactivate}>Deactivate</button>
            </div>
          </form>
        </div>
      </div>

    </div>`;

  el("price-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    try {
      await ProductAPI.updatePrice(id, el("basePrice").value);
      showToast("Price updated.");
      load();
    } catch (err) { showToast(err.message); }
  });

  el("deactivate-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    if (!confirm("Deactivate this product?")) return;
    try {
      await ProductAPI.deactivate(id);
      showToast("Product deactivated.");
      load();
    } catch (err) { showToast(err.message); }
  });
}

async function load() {
  try {
    const p = await ProductAPI.get(id);
    render(p);
  } catch (err) {
    el("view").innerHTML = `<div class="empty">Could not load product.</div>`;
    showToast(err.message);
  }
}

load();

