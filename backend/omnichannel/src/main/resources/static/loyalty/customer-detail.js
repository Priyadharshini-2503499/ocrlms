// customer-detail.js — streamlined profile rendering & tier-based coupon delivery
import { buildLayout } from "../assets/js/layout.js";
import { CustomerAPI } from "../assets/js/endpoints.js";
import { badge, escapeHtml, showToast, getParam, el } from "../assets/js/ui.js";

buildLayout({
  active: "customers",
  title: "Customer Detail",
  subtitle: "Profile, points & redemption",
  role: "Customer Service",
  initials: "CS",
});

const id = getParam("id");

function render(c, coupons) {
  // Dynamic Tier CSS Class Mapping
  const tierStyle = c.loyaltyTier === 'SILVER' ? 'b-silver' : c.loyaltyTier === 'GOLD' ? 'b-gold' : 'b-platinum';

  // Dynamic Generation loop through coupons array
  let couponsHtml = '';
  if (coupons && coupons.length > 0) {
    couponsHtml = coupons.map(cp => `
      <div style="display: flex; justify-content: space-between; align-items: center; padding: 14px 0; border-bottom: 1px solid var(--line2);">
        <div>
          <span class="mono" style="font-weight: 600; background: var(--ivory); padding: 4px 10px; border-radius: 4px; border: 1px solid var(--line); font-size: 13px; color: var(--ink);">
            ${escapeHtml(cp.couponCode)}
          </span>
          <div style="font-size: 12px; color: var(--muted); margin-top: 6px;">
            Type: <strong>${escapeHtml(cp.discountType)}</strong> &middot; Discount: <span class="text-gold" style="font-weight:600;">${cp.discountValue}${cp.discountType === 'PERCENTAGE' ? '%' : ' ₹'}</span>
          </div>
        </div>
        <div style="text-align: right;">
          <span class="bdg b-silver" style="font-size: 11px; text-transform: uppercase;">${escapeHtml(cp.couponStatus)}</span>
          <div style="font-size: 11px; color: var(--muted); margin-top: 4px;">Expires: ${cp.validTo}</div>
        </div>
      </div>
    `).join('');
  } else {
    couponsHtml = `<div class="empty" style="padding: 24px 0; color: var(--muted);">No exclusive promotional coupons are unlocked for the current ${escapeHtml(c.loyaltyTier)} tier right now.</div>`;
  }

  // 🌟 FIXED: Rendered as a single structured block without the duplicate adjustments deck
  el("view").innerHTML = `
    <div style="display: flex; flex-direction: column; gap: 22px;">

      <div class="card">
        <div class="card-h">
          <div><h2>${escapeHtml(c.fullName)}</h2><p>Account Reference: #${c.customerId}</p></div>
          ${badge(c.loyaltyTier)}
        </div>
        <div class="card-b">
          <div style="display:flex; flex-direction:column; gap:6px; margin-bottom:24px;">
            <span class="k" style="color:var(--muted); font-size:11px; text-transform:uppercase; letter-spacing:.08em; font-weight:600;">Loyalty points balance</span>
            <span class="serif mono text-gold" style="font-size:34px; font-weight:500;">${c.loyaltyPoints}</span>

            <span class="bdg ${tierStyle}" style="align-self:flex-start; margin-top:4px;">
              <span class="d"></span>
              <span>${escapeHtml(c.loyaltyTier)} tier status</span>
            </span>

          </div>
          <dl class="kv">
            <dt>Email Address</dt><dd>${escapeHtml(c.emailId)}</dd>
            <dt>Phone Number</dt><dd>${escapeHtml(c.phoneNumber)}</dd>
          </dl>
          <a href="../order-management/orders.html?customerId=${c.customerId}"
             class="btn btn-ghost btn-sm" style="margin-top:24px; width: fit-content;">View order history</a>
        </div>
      </div>

      <div class="card">
        <div class="card-h">
          <div>
            <h2>Exclusive Unlocked Coupons</h2>
            <p>Promotions automatically available to this account based on their current ${escapeHtml(c.loyaltyTier)} tier level status.</p>
          </div>
        </div>
        <div class="card-b" style="display: flex; flex-direction: column; gap: 4px;">
          ${couponsHtml}
        </div>
      </div>

    </div>`;
}

// Orchestrates parallel fetching of customer info and tier-based coupons
async function load() {
  try {
    if (!id) {
      el("view").innerHTML = `<div class="empty">No customer ID provided in URL context.</div>`;
      return;
    }

    const c = await CustomerAPI.get(id);
    let coupons = [];

    try {
      const token = localStorage.getItem("token") || localStorage.getItem("auriaToken");
      const response = await fetch(`http://localhost:8085/api/promotions/coupons/tier/${c.loyaltyTier}`, {
        method: "GET",
        headers: {
          "Authorization": token ? `Bearer ${token}` : "",
          "Content-Type": "application/json"
        }
      });

      if (response.ok) {
        coupons = await response.json();
      }
    } catch (promoErr) {
      console.warn("Promotions registry offline fallback triggered:", promoErr.message);
    }

    render(c, coupons);
  } catch (err) {
    console.error("Core Customer Loader Encountered Failure: ", err);
    el("view").innerHTML = `<div class="alert alert-error" style="margin:20px 0;">Error pulling data: ${escapeHtml(err.message)}</div>`;
    showToast(err.message);
  }
}

load();