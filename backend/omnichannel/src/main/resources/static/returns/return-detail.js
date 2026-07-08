// return-detail.html — view a return, approve, process refund, timeline
import { buildLayout } from "../assets/js/layout.js";
import { ReturnAPI } from "../assets/js/endpoints.js";
import { money, badge, escapeHtml, showToast, getParam, el } from "../assets/js/ui.js";

buildLayout({
  active: "returns",
  title: "Return Detail",
  subtitle: "Eligibility & refund settlement",
  role: "Customer Service",
  initials: "CS",
});

const id = getParam("id");

function stepClass(s, doneStates, currentState) {
  if (doneStates.includes(s)) return "done";
  if (s === currentState) return "current";
  return "todo";
}

function render(r) {
  const s = r.returnStatus;
  // Eligibility is derived from status (the backend ReturnResponse has no flags).
  const canApprove = s === "REQUESTED";
  const canReject = s === "REQUESTED";
  const canRefund = s === "APPROVED";
  const approveBtn = canApprove
    ? `<form id="approve-form"><button type="submit" class="btn btn-primary btn-sm">Approve return</button></form>`
    : "";
  const rejectBtn = canReject
    ? `<form id="reject-form"><button type="submit" class="btn btn-rust btn-sm">Reject return</button></form>`
    : "";
  const refundBtn = canRefund
    ? `<form id="refund-form"><button type="submit" class="btn btn-green btn-sm">Process refund</button></form>`
    : "";
  const closedNote =
    s === "REFUNDED" ? `<span class="text-muted-soft">Refund settled.</span>`
    : s === "REJECTED" ? `<span class="text-muted-soft">Request rejected.</span>` : "";

  el("view").innerHTML = `
    <div class="grid cols-2" style="align-items:stretch; gap:22px;">

      <div class="card">
        <div class="card-h">
          <div><h2>Return #${r.returnId}</h2><p>Order #${r.orderId}</p></div>
          ${badge(s)}
        </div>
        <div class="card-b">
          <div style="display:flex; flex-direction:column; gap:6px; margin-bottom:24px;">
            <span class="k" style="color:var(--muted); font-size:11px; text-transform:uppercase; letter-spacing:.08em; font-weight:600;">Refund amount</span>
            <span class="serif mono" style="font-size:34px; font-weight:500; color:var(--ink);">${money(r.refundAmount)}</span>
          </div>
          <dl class="kv">
            <dt>Reason</dt><dd>${escapeHtml(r.returnReason)}</dd>
            <dt>Requested on</dt><dd class="mono">${escapeHtml(r.requestDate)}</dd>
          </dl>
          <div class="row-actions" style="justify-content:flex-start;margin-top:24px;">
            ${approveBtn}${rejectBtn}${refundBtn}${closedNote}
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-h"><div><h2>Progress</h2><p>Return lifecycle.</p></div></div>
        <div class="card-b">
          <div class="timeline">
            <div class="step done">
              <span class="dot"></span>
              <div><div class="lbl">Requested</div><div class="meta">${escapeHtml(r.requestDate)}</div></div>
            </div>
            <div class="step ${stepClass(s, ["APPROVED", "REFUNDED"], "REQUESTED")}">
              <span class="dot"></span>
              <div><div class="lbl">Approved</div><div class="meta">Eligibility confirmed</div></div>
            </div>
            <div class="step ${stepClass(s, ["REFUNDED"], "APPROVED")}">
              <span class="dot"></span>
              <div><div class="lbl">Refunded</div><div class="meta">Refund settled to customer</div></div>
            </div>
          </div>
        </div>
      </div>

    </div>`;

  const approveForm = el("approve-form");
  if (approveForm) {
    approveForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      try {
        await ReturnAPI.approve(id);
        showToast("Return approved.");
        load();
      } catch (err) { showToast(err.message); }
    });
  }

  const rejectForm = el("reject-form");
  if (rejectForm) {
    rejectForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      if (!confirm("Reject this return request?")) return;
      try {
        await ReturnAPI.reject(id);
        showToast("Return rejected.");
        load();
      } catch (err) { showToast(err.message); }
    });
  }

  const refundForm = el("refund-form");
  if (refundForm) {
    refundForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      if (!confirm("Process the refund now?")) return;
      try {
        await ReturnAPI.refund(id);
        showToast("Refund processed.");
        load();
      } catch (err) { showToast(err.message); }
    });
  }
}

async function load() {
  try {
    const r = await ReturnAPI.get(id);
    render(r);
  } catch (err) {
    el("view").innerHTML = `<div class="empty">Could not load return.</div>`;
    showToast(err.message);
  }
}

load();

