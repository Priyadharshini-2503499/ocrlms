import { buildLayout } from "../assets/js/layout.js";
import { PromotionAPI } from "../assets/js/endpoints.js";
import { money, badge, escapeHtml, showToast, el } from "../assets/js/ui.js";
import { getBackendRole, requireAuth } from "../assets/js/auth.js";

// Guard: redirect to login if the user has no active session token.
requireAuth();

// Build shared sidebar + topbar (role/username read from session inside buildLayout)
buildLayout({
  active: "promotions",
  title: "Promotions",
  subtitle: "Campaigns & coupons",
});

// ---- Role check ---------------------------------------------------
// Only ADMIN and MARKETING_MANAGER may create, edit, or delete coupons.
const backendRole = getBackendRole();   // "ADMIN" | "MARKETING_MANAGER" | etc.
const canWrite    = backendRole === "ADMIN" || backendRole === "MARKETING_MANAGER";

if (!canWrite) {
  // Hide the entire create coupon card for read-only roles
  const cc = el("create-card");
  if (cc) cc.style.display = "none";
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
  const t = (tier || "GLOBAL").toUpperCase();
  if (t === "GOLD")     return "b-gold";
  if (t === "PLATINUM") return "b-platinum";
  if (t === "SILVER")   return "b-silver";
  return "b-gray";
}

function minBasketText(v) {
  return v && v > 0
    ? money(v)
    : `<span style="color:var(--muted);">None</span>`;
}

function rowHtml(c) {
  const actionsCells = canWrite
    ? `<td style="white-space:nowrap;">
         <button class="btn-icon-edit" id="edit-btn-${c.couponId}"
                 onclick="window._editCoupon(${c.couponId})"
                 title="Edit coupon">
           <i class="bi bi-pencil-square"></i>
         </button>
         <button class="btn-icon-del" id="del-btn-${c.couponId}"
                 onclick="window._deleteCoupon(${c.couponId}, '${escapeHtml(c.couponCode)}')"
                 title="Delete coupon">
           <i class="bi bi-trash3"></i>
         </button>
       </td>`
    : "";

  return `
    <tr>
      <td class="mono" style="font-weight:600;color:var(--ink);">${escapeHtml(c.couponCode)}</td>
      <td>${escapeHtml(c.discountType)}</td>
      <td class="mono">${valueText(c)}</td>
      <td><span class="bdg ${tierClass(c.targetTier)}" style="font-size:10px;padding:2px 8px;">
            ${escapeHtml(c.targetTier || "GLOBAL")}
          </span></td>
      <td class="mono">${minBasketText(c.minimumBasketValue)}</td>
      <td class="sub">${escapeHtml(String(c.validFrom))} &rarr; ${escapeHtml(String(c.validTo))}</td>
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
  const colspan = canWrite ? 8 : 7;
  rows.innerHTML = `<tr><td colspan="${colspan}"><div class="empty">Loading…</div></td></tr>`;
  try {
    const coupons = await PromotionAPI.list();
    rows.innerHTML = coupons.length
      ? coupons.map(rowHtml).join("")
      : emptyHtml();
  } catch (err) {
    rows.innerHTML = `<tr><td colspan="${colspan}">
      <div class="empty" style="color:#e74c3c;">
        Could not load coupons: ${escapeHtml(err.message)}
      </div></td></tr>`;
    showToast("Could not load coupons: " + err.message);
  }
}

// ---- CREATE --------------------------------------------------------

const createForm = el("create-form");
if (createForm) {
  createForm.addEventListener("submit", async (e) => {
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
}
// ---- APPLY / SIMULATE ----------------------------------------------

el("apply-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const f   = e.target;
  const ok  = el("apply-ok");
  const bad = el("apply-error");
  ok.style.display  = "none";
  bad.style.display = "none";

  // Read by element name attribute (form element access)
  const code           = f.code.value.trim().toUpperCase();
  const amount         = Number(f.amount.value);
  const customerIdRaw  = f.customerId.value.trim();

  if (!code || !amount) {
    bad.textContent   = "Please enter a coupon code and basket value.";
    bad.style.display = "";
    return;
  }

  const body = {
    code,
    amount,
    customerId: customerIdRaw ? Number(customerIdRaw) : null,
  };

  const btn = el("apply-btn");
  btn.disabled    = true;
  btn.textContent = "Validating…";

  try {
    const result = await PromotionAPI.apply(body);
    ok.innerHTML = `
      <div style="font-weight:600;">${escapeHtml(result.message)}</div>
      <div style="margin-top:4px;">Final total: <strong class="mono">${money(result.finalAmount)}</strong></div>`;
    ok.style.display = "";
  } catch (err) {
    // Backend sends the exact validation message — show it directly
    bad.textContent   = err.message;
    bad.style.display = "";
  } finally {
    btn.disabled    = false;
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
  editErrBox.style.display    = "none";
  el("edit-save-btn").disabled    = false;
  el("edit-save-btn").textContent = "Save changes";

  try {
    const data = await PromotionAPI.get(id);
    // Backend returns { success: true, coupon: {...} }
    const c = data.coupon || data;

    el("edit-id").value                 = c.couponId;
    el("edit-couponCode").value         = c.couponCode;
    el("edit-discountType").value       = c.discountType;
    el("edit-discountValue").value      = c.discountValue;

    // Dates come as "yyyy-MM-dd" strings (fixed via @JsonFormat + jackson config)
    // HTML date inputs require exactly this format.
    el("edit-validFrom").value          = String(c.validFrom);
    el("edit-validTo").value            = String(c.validTo);

    el("edit-targetTier").value         = c.targetTier || "GLOBAL";
    el("edit-minimumBasketValue").value = c.minimumBasketValue ?? 0;
    el("edit-maxRedemptionCount").value = c.maxRedemptionCount ?? 0;
    el("edit-couponStatus").value       = c.couponStatus;

    editModal.classList.add("open");
  } catch (err) {
    showToast("Could not load coupon for editing: " + err.message);
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
  btn.disabled    = true;
  btn.textContent = "Saving…";

  try {
    await PromotionAPI.update(id, body);
    showToast("Coupon updated successfully.");
    closeEditModal();
    loadCoupons();           // Refresh the table without page reload
  } catch (err) {
    // Show backend validation messages directly in the modal
    editErrBox.textContent   = err.message;
    editErrBox.style.display = "";
    btn.disabled    = false;
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

// Exposed globally so inline onclick in rowHtml can call it
window._deleteCoupon = function (id, code) {
  _pendingDeleteId = id;
  confirmMsg.textContent = `Permanently delete coupon "${code}"? This action cannot be undone.`;
  confirmOverlay.classList.add("open");
};

confirmOkBtn.addEventListener("click", async () => {
  if (_pendingDeleteId === null) return;
  confirmOkBtn.disabled    = true;
  confirmOkBtn.textContent = "Deleting…";

  try {
    await PromotionAPI.remove(_pendingDeleteId);
    showToast("Coupon deleted successfully.");
    confirmOverlay.classList.remove("open");
    loadCoupons();           // Remove from UI without page reload
  } catch (err) {
    showToast("Delete failed: " + err.message);
    confirmOverlay.classList.remove("open");
  } finally {
    confirmOkBtn.disabled    = false;
    confirmOkBtn.textContent = "Delete";
    _pendingDeleteId = null;
  }
});

// ---- Initial load --------------------------------------------------
loadCoupons();