// promotion/index — create coupon, simulate discount, list coupons
import { buildLayout } from "../assets/js/layout.js";
import { PromotionAPI } from "../assets/js/endpoints.js";
import { money, badge, escapeHtml, showToast, el } from "../assets/js/ui.js";

buildLayout({
  active: "promotions",
  title: "Promotions",
  subtitle: "Campaigns & coupons",
  role: "Marketing Manager",
  initials: "MM",
});

function valueText(c) {
  return c.discountType === "PERCENTAGE" ? `${c.discountValue}%` : money(c.discountValue);
}

// 🌟 UPDATED: Render structure to include the Target Tier label capsule
function rowHtml(c) {
  const tierClass = c.targetTier === 'GOLD' ? 'b-gold' : c.targetTier === 'PLATINUM' ? 'b-platinum' : 'b-silver';
  return `
    <tr>
      <td class="mono" style="font-weight:600;color:var(--ink);">${escapeHtml(c.couponCode)}</td>
      <td>${escapeHtml(c.discountType)}</td>
      <td class="mono">${valueText(c)}</td>
      <td><span class="bdg ${tierClass}" style="font-size:10px; padding:2px 8px;">${escapeHtml(c.targetTier || 'GLOBAL')}</span></td>
      <td class="sub">${escapeHtml(c.validFrom)} &rarr; ${escapeHtml(c.validTo)}</td>
      <td>${badge(c.couponStatus)}</td>
    </tr>`;
}

// 🌟 UPDATED: Changed colspan to 6
function emptyHtml() {
  return `
    <tr><td colspan="6">
      <div class="empty">
        <div class="big">🎟️</div>
        No coupons yet. Create your first campaign above.
      </div>
    </td></tr>`;
}

async function loadCoupons() {
  const rows = el("rows");
  try {
    const coupons = await PromotionAPI.list();
    rows.innerHTML = coupons.length ? coupons.map(rowHtml).join("") : emptyHtml();
  } catch (err) {
    rows.innerHTML = `<tr><td colspan="6"><div class="empty">Could not load coupons.</div></td></tr>`; // 🌟 UPDATED: Colspan 6
    showToast(err.message);
  }
}

// Create coupon
el("create-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const f = e.target;
  const errBox = el("create-error");
  errBox.style.display = "none";

  // 🌟 UPDATED: Added targetTier selection collector mapping property
  const body = {
    couponCode: f.couponCode.value.trim(),
    discountType: f.discountType.value,
    discountValue: Number(f.discountValue.value),
    validFrom: f.validFrom.value,
    validTo: f.validTo.value,
    targetTier: f.targetTier.value
  };

  if (!body.couponCode || !body.validFrom || !body.validTo) {
    errBox.textContent = "Please fill in all required fields.";
    errBox.style.display = "";
    return;
  }
  try {
    await PromotionAPI.create(body);
    showToast("Coupon created.");
    f.reset();
    loadCoupons();
  } catch (err) {
    errBox.textContent = err.message;
    errBox.style.display = "";
  }
});

// Apply / simulate coupon
el("apply-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const f = e.target;
  const ok = el("apply-ok");
  const bad = el("apply-error");
  ok.style.display = "none";
  bad.style.display = "none";

  try {
    const result = await PromotionAPI.apply(f.code.value.trim(), f.amount.value);
    ok.innerHTML = `
      <div style="font-weight:600;">${escapeHtml(result.message)}</div>
      <div style="margin-top:4px;">Final total: <strong class="mono">${money(result.finalAmount)}</strong></div>`;
    ok.style.display = "";
  } catch (err) {
    bad.textContent = err.message;
    bad.style.display = "";
  }
});

loadCoupons();