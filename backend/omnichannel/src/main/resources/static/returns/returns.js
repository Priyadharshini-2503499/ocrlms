// returns.html — list all returns
import { buildLayout } from "../assets/js/layout.js";
import { ReturnAPI } from "../assets/js/endpoints.js";
import { money, badge, escapeHtml, showToast, el } from "../assets/js/ui.js";

buildLayout({
  active: "returns",
  title: "Returns & Refunds",
  subtitle: "Return requests & settlements",
  role: "Customer Service",
  initials: "CS",
});

function rowHtml(r) {
  return `
    <tr>
      <td style="font-weight:600;color:var(--ink);">#${r.returnId}</td>
      <td>#${r.orderId}</td>
      <td>${escapeHtml(r.returnReason)}</td>
      <td class="price mono">${money(r.refundAmount)}</td>
      <td class="mono">${escapeHtml(r.requestDate)}</td>
      <td>${badge(r.returnStatus)}</td>
      <td class="row-actions">
        <a href="return-detail.html?id=${r.returnId}" class="btn btn-ghost btn-sm">View</a>
      </td>
    </tr>`;
}

function emptyHtml() {
  return `
    <tr><td colspan="7">
      <div class="empty">
        <div class="big">↩️</div>
        No returns yet. <a href="return-form.html" class="text-link">Initiate one</a>.
      </div>
    </td></tr>`;
}

async function load() {
  const rows = el("rows");
  try {
    const returns = await ReturnAPI.list();
    rows.innerHTML = returns.length ? returns.map(rowHtml).join("") : emptyHtml();
  } catch (err) {
    rows.innerHTML = `<tr><td colspan="7"><div class="empty">Could not load returns.</div></td></tr>`;
    showToast(err.message);
  }
}

load();

