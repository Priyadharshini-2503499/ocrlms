// admin/users.html — ADMIN only: list accounts and provision new users.
import { buildLayout } from "../assets/js/layout.js";
import { AuthAPI } from "../assets/js/endpoints.js";
import { requireAuth, isAdmin, getUsername, redirectToLogin } from "../assets/js/auth.js";
import { badge, escapeHtml, showToast, showFormError, el } from "../assets/js/ui.js";

// Guard: must be signed in AND be an admin.
if (!requireAuth()) {
  // requireAuth already redirected.
} else if (!isAdmin()) {
  showToast("Administrator access required.");
  redirectToLogin();
}

buildLayout({
  active: "users",
  title: "User Administration",
  subtitle: "Provision and manage accounts",
  role: "Admin",
  initials: (getUsername()[0] || "A").toUpperCase(),
});

const ROLE_LABEL = {
  ADMIN: "Admin",
  MERCHANDISER: "Merchandiser",
  STORE_MANAGER: "Store Manager",
  CUSTOMER_SERVICE: "Customer Service",
  MARKETING_MANAGER: "Marketing Manager",
};

function rowHtml(u) {
  return `
    <tr>
      <td>
        <div style="font-weight:600;color:var(--ink);">${escapeHtml(u.fullName)}</div>
        <div class="sub">#${u.userId}</div>
      </td>
      <td class="mono">${escapeHtml(u.username)}</td>
      <td>${escapeHtml(ROLE_LABEL[u.role] || u.role)}</td>
      <td>${badge(u.enabled ? "ACTIVE" : "INACTIVE")}</td>
    </tr>`;
}

function emptyHtml() {
  return `
    <tr><td colspan="4">
      <div class="empty"><div class="big">👥</div>No users yet.</div>
    </td></tr>`;
}

async function loadUsers() {
  const rows = el("rows");
  try {
    const users = await AuthAPI.listUsers();
    rows.innerHTML = users.length ? users.map(rowHtml).join("") : emptyHtml();
  } catch (err) {
    rows.innerHTML = `<tr><td colspan="4"><div class="empty">Could not load users.</div></td></tr>`;
    showToast(err.message);
  }
}

const form = el("user-form");
const btn = el("submit-btn");

form.addEventListener("submit", async (e) => {
  e.preventDefault();
  showFormError("");

  const body = {
    fullName: el("fullName").value.trim(),
    username: el("username").value.trim(),
    password: el("password").value,
    role: el("role").value,
  };

  if (!body.fullName || !body.username || !body.password || !body.role) {
    showFormError("All fields are required.");
    return;
  }

  btn.disabled = true;
  btn.textContent = "Creating…";

  try {
    const created = await AuthAPI.createUser(body);
    showToast(`User '${created.username}' created.`);
    form.reset();
    await loadUsers();
  } catch (err) {
    showFormError(err.message || "Could not create user.");
  } finally {
    btn.disabled = false;
    btn.textContent = "+ Create user";
  }
});

loadUsers();

