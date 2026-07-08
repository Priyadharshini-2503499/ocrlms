// products.html — list all products
import { buildLayout } from "../assets/js/layout.js";
import { ProductAPI } from "../assets/js/endpoints.js";
import { money, badge, escapeHtml, showToast, el } from "../assets/js/ui.js";

buildLayout({
  active: "products",
  title: "Product Catalog",
  subtitle: "Master data & channel pricing",
  role: "Merchandiser",
  initials: "MD",
});

function rowHtml(p) {
  return `
    <tr>
      <td class="mono">${escapeHtml(p.skuCode)}</td>
      <td>
        <div style="font-weight:600;color:var(--ink);">${escapeHtml(p.productName)}</div>
        <div class="sub">#${p.productId}</div>
      </td>
      <td>${escapeHtml(p.category)}</td>
      <td class="price mono">${money(p.basePrice)}</td>
      <td>${badge(p.productStatus)}</td>
      <td class="row-actions">
        <a href="product-detail.html?id=${p.productId}" class="btn btn-ghost btn-sm">View</a>
      </td>
    </tr>`;
}

function emptyHtml() {
  return `
    <tr><td colspan="6">
      <div class="empty">
        <div class="big">📦</div>
        No products yet. <a href="product-form.html" class="text-link">Add your first product</a>.
      </div>
    </td></tr>`;
}

async function load() {
  const rows = el("rows");
  try {
    const products = await ProductAPI.list();
    rows.innerHTML = products.length ? products.map(rowHtml).join("") : emptyHtml();
  } catch (err) {
    rows.innerHTML = `<tr><td colspan="6"><div class="empty">Could not load products.</div></td></tr>`;
    showToast(err.message);
  }
}

load();

