// customers.html — list all customers
import { buildLayout } from "../assets/js/layout.js";
import { CustomerAPI } from "../assets/js/endpoints.js";
import { badge, escapeHtml, showToast, el } from "../assets/js/ui.js";

buildLayout({
  active: "customers",
  title: "Customers & Loyalty",
  subtitle: "Profiles & tier progression",
  role: "Customer Service",
  initials: "CS",
});

function rowHtml(c) {
  return `
    <tr>
      <td>
        <div style="font-weight:600;color:var(--ink);">${escapeHtml(c.fullName)}</div>
        <div class="sub">#${c.customerId}</div>
      </td>
      <td>
        <div>${escapeHtml(c.emailId)}</div>
        <div class="sub">${escapeHtml(c.phoneNumber)}</div>
      </td>
      <td class="mono text-gold" style="font-weight:600;">${c.loyaltyPoints}</td>
      <td>${badge(c.loyaltyTier)}</td>
      <td class="row-actions">
        <a href="customer-detail.html?id=${c.customerId}" class="btn btn-ghost btn-sm">View</a>
      </td>
    </tr>`;
}

function emptyHtml() {
  return `
    <tr><td colspan="5">
      <div class="empty">
        <div class="big">👤</div>
        No customers yet. <a href="customer-form.html" class="text-link">Register one</a>.
      </div>
    </td></tr>`;
}

async function load() {
  const rows = el("rows");
  try {
    const customers = await CustomerAPI.list();
    rows.innerHTML = customers.length ? customers.map(rowHtml).join("") : emptyHtml();
  } catch (err) {
    rows.innerHTML = `<tr><td colspan="5"><div class="empty">Could not load customers.</div></td></tr>`;
    showToast(err.message);
  }
}

load();

