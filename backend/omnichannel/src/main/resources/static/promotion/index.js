// =====================================================================
// promotion/index.js — Coupon Management (create, edit, delete, simulate)
// Roles that can write (create/edit/delete): ADMIN, MARKETING_MANAGER
// =====================================================================
import { buildLayout } from "../assets/js/layout.js";
import { PromotionAPI } from "../assets/js/endpoints.js";
import { money, badge, escapeHtml, showToast, el } from "../assets/js/ui.js";
import { getBackendRole } from "../assets/js/auth.js";

buildLayout({
  active: "promotions",
  title: "Promotions",
  subtitle: "Campaigns & coupons",
});

// ---- Role check ---------------------------------------------------
// Only ADMIN and MARKETING_MANAGER may create, edit, or delete coupons.
const backendRole = getBackendRole();       // e.g. "ADMIN", "MARKETING_MANAGER"
const canWrite = backendRole === "ADMIN" || backendRole === "MARKETING_MANAGER";

if (!canWrite) {
  // Hide create card for read-only roles
  const cc = el("create-card");
  if (cc) cc.classList.add("hidden");
}

// Show Actions column header only for write roles
if (canWrite) {
  const th = el("actions-th");
  if (th) th.style.display = "";
}

// ---- Helpers -------------------------------------------------------

function valueText(c) {
  return c.discountType === "PERCENTAGE"
    ? `${c.discountValue}%`
    : money(c.discountValue);
}

function tierClass(tier) {
  if (!tier || tier === "GLOBAL") return "b-gray";
  if (tier === "GOLD")     return "b-gold";
  if (tier === "PLATINUM") return "b-platinum";
  return "b-silver";
}

function minBasketText(v) {
  return v && v > 0 ? money(v) : `<span style="color:var(--muted);">None</span>`;
}

function rowHtml(c) {
  const actionsCells = canWrite ? `
    <td style="white-space:nowrap;">
      <button class="btn-edit" id="edit-btn-${c.couponId}"
              onclick="window._editCoupon(${c.couponId})">Edit</button>
      <button class="btn-del"  id="del-btn-${c.couponId}"
              onclick="window._deleteCoupon(${c.couponId}, '${escapeHtml(c.couponCode)}')"
              style="margin-left:4px;">Delete</button>
    </td>` : "";

  const colspan = canWrite ? 8 : 7;

  return `
    <tr>
      <td class="mono" style="font-weight:600;color:var(--ink);">${escapeHtml(c.couponCode)}</td>
      <td>${escapeHtml(c.discountType)}</td>
      <td class="mono">${valueText(c)}</td>
      <td><span class="bdg ${tierClass(c.targetTier)}" style="font-size:10px;padding:2px 8px;">${escapeHtml(c.targetTier || "GLOBAL")}</span></td>
      <td class="mono">${minBasketText(c.minimumBasketValue)}</td>
      <td class="sub">${escapeHtml(c.validFrom)} &rarr; ${escapeHtml(c.validTo)}</td>
      <td>${badge(c.couponStatus)}</td>
      ${actionsCells}
    </tr>`;
}

function emptyHtml() {
  const colspan = canWrite ? 8 : 7;
  return `
    <tr><td colspan="${colspan}">
      <div class="empty">
        <div class="big">🎟️</div>
        No coupons yet. Create your first campaign above.
      </div>
    </td></tr>`;
}

// ---- Load coupon list ----------------------------------------------

async function loadCoupons() {
  const rows = el("rows");
  try {
    const coupons = await PromotionAPI.list();
    rows.innerHTML = coupons.length ? coupons.map(rowHtml).join("") : emptyHtml();
  } catch (err) {
    const colspan = canWrite ? 8 : 7;
    rows.innerHTML = `<tr><td colspan="${colspan}"><div class="empty">Could not load coupons.</div></td></tr>`;
    showToast(err.message);
  }
}

// ---- CREATE --------------------------------------------------------

el("create-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const f = e.target;
  const errBox = el("create-error");
  errBox.style.display = "none";

  const body = {
    couponCode:         f.couponCode.value.trim().toUpperCase(),
    discountType:       f.discountType.value,
    discountValue:      Number(f.discountValue.value),
    validFrom:          f.validFrom.value,
    validTo:            f.validTo.value,
    targetTier:         f.targetTier.value,
    minimumBasketValue: Number(f.minimumBasketValue.value) || 0,
    maxRedemptionCount: Number(f.maxRedemptionCount.value) || 0,
  };

  if (!body.couponCode || !body.validFrom || !body.validTo) {
    errBox.textContent = "Please fill in all required fields.";
    errBox.style.display = "";
    return;
  }

  const btn = el("create-btn");
  btn.disabled = true;
  btn.textContent = "Creating…";

  try {
    await PromotionAPI.create(body);
    showToast("Coupon created successfully.");
    f.reset();
    loadCoupons();
  } catch (err) {
    errBox.textContent = err.message;
    errBox.style.display = "";
  } finally {
    btn.disabled = false;
    btn.textContent = "Create coupon";
  }
});

// ---- APPLY / SIMULATE ----------------------------------------------

el("apply-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const f = e.target;
  const ok  = el("apply-ok");
  const bad = el("apply-error");
  ok.style.display  = "none";
  bad.style.display = "none";

  const customerIdRaw = f.customerId.value.trim();
  const body = {
    code:       f.code.value.trim(),
    amount:     Number(f.amount.value),
    customerId: customerIdRaw ? Number(customerIdRaw) : null,
  };

  const btn = el("apply-btn");
  btn.disabled = true;
  btn.textContent = "Validating…";

  try {
    const result = await PromotionAPI.apply(body);
    ok.innerHTML = `
      <div style="font-weight:600;">${escapeHtml(result.message)}</div>
      <div style="margin-top:4px;">Final total: <strong class="mono">${money(result.finalAmount)}</strong></div>`;
    ok.style.display = "";
  } catch (err) {
    bad.textContent = err.message;
    bad.style.display = "";
  } finally {
    btn.disabled = false;
    btn.textContent = "Simulate discount";
  }
});

// ---- EDIT ----------------------------------------------------------

const editModal     = el("edit-modal");
const editForm      = el("edit-form");
const editErrBox    = el("edit-error");
const editCancelBtn = el("edit-cancel-btn");

function closeEditModal() {
  editModal.classList.remove("open");
  editErrBox.style.display = "none";
}

editCancelBtn.addEventListener("click", closeEditModal);
editModal.addEventListener("click", (e) => {
  if (e.target === editModal) closeEditModal();
});

// Exposed globally so inline onclick handlers in rowHtml can call it
window._editCoupon = async function (id) {
  editErrBox.style.display = "none";
  el("edit-save-btn").disabled = false;
  el("edit-save-btn").textContent = "Save changes";

  try {
    const data = await PromotionAPI.get(id);
    const c = data.coupon;

    el("edit-id").value                  = c.couponId;
    el("edit-couponCode").value          = c.couponCode;
    el("edit-discountType").value        = c.discountType;
    el("edit-discountValue").value       = c.discountValue;
    el("edit-validFrom").value           = c.validFrom;
    el("edit-validTo").value             = c.validTo;
    el("edit-targetTier").value          = c.targetTier || "GLOBAL";
    el("edit-minimumBasketValue").value  = c.minimumBasketValue || 0;
    el("edit-maxRedemptionCount").value  = c.maxRedemptionCount || 0;
    el("edit-couponStatus").value        = c.couponStatus;

    editModal.classList.add("open");
  } catch (err) {
    showToast("Could not load coupon: " + err.message);
  }
};

editForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  editErrBox.style.display = "none";

  const id = Number(el("edit-id").value);
  const body = {
    couponCode:         el("edit-couponCode").value.trim().toUpperCase(),
    discountType:       el("edit-discountType").value,
    discountValue:      Number(el("edit-discountValue").value),
    validFrom:          el("edit-validFrom").value,
    validTo:            el("edit-validTo").value,
    targetTier:         el("edit-targetTier").value,
    minimumBasketValue: Number(el("edit-minimumBasketValue").value) || 0,
    maxRedemptionCount: Number(el("edit-maxRedemptionCount").value) || 0,
    couponStatus:       el("edit-couponStatus").value,
  };

  const btn = el("edit-save-btn");
  btn.disabled = true;
  btn.textContent = "Saving…";

  try {
    await PromotionAPI.update(id, body);
    showToast("Coupon updated successfully.");
    closeEditModal();
    loadCoupons();
  } catch (err) {
    editErrBox.textContent = err.message;
    editErrBox.style.display = "";
    btn.disabled = false;
    btn.textContent = "Save changes";
  }
});

// ---- DELETE --------------------------------------------------------

const confirmOverlay = el("confirm-overlay");
const confirmMsg     = el("confirm-msg");
const confirmOkBtn   = el("confirm-ok");
const confirmCancel  = el("confirm-cancel");
let _pendingDeleteId = null;

confirmCancel.addEventListener("click", () => confirmOverlay.classList.remove("open"));
confirmOverlay.addEventListener("click", (e) => {
  if (e.target === confirmOverlay) confirmOverlay.classList.remove("open");
});

window._deleteCoupon = function (id, code) {
  _pendingDeleteId = id;
  confirmMsg.textContent = `Permanently delete coupon "${code}"? This cannot be undone.`;
  confirmOverlay.classList.add("open");
};

confirmOkBtn.addEventListener("click", async () => {
  if (_pendingDeleteId === null) return;
  confirmOkBtn.disabled = true;
  confirmOkBtn.textContent = "Deleting…";

  try {
    await PromotionAPI.remove(_pendingDeleteId);
    showToast("Coupon deleted.");
    confirmOverlay.classList.remove("open");
    loadCoupons();
  } catch (err) {
    showToast("Delete failed: " + err.message);
    confirmOverlay.classList.remove("open");
  } finally {
    confirmOkBtn.disabled = false;
    confirmOkBtn.textContent = "Delete";
    _pendingDeleteId = null;
  }
});

// ---- Initial load --------------------------------------------------
loadCoupons();