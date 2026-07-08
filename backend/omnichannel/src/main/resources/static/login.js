// login.js — authenticate against auth-service and start a session.
import { setSession, isLoggedIn, landingFor } from "./assets/js/auth.js";
import { el, showFormError, showToast } from "./assets/js/ui.js";


const form = el("login-form");
const btn = el("submit-btn");
const errorBox = el("form-error");

form.addEventListener("submit", async (e) => {
  e.preventDefault();
  // Hide previous error
  errorBox.style.display = "none";
  errorBox.textContent = "";

  const username = el("username").value.trim();
  const password = el("password").value;

  if (!username || !password) {
    errorBox.textContent = "Username and password are required.";
    errorBox.style.display = "block";
    return;
  }

  btn.disabled = true;
  btn.textContent = "Signing in…";

  try {
    const response = await fetch("/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password }),
    });

    const data = await response.json();

    if (!response.ok) {
      const msg = data.message || "Invalid username or password.";
      errorBox.textContent = msg;
      errorBox.style.display = "block";
      showToast(msg);
      btn.disabled = false;
      btn.textContent = "Sign in";
      return;
    }

    setSession({ token: data.token, username: data.username, role: data.role });
    showToast(`Welcome, ${data.username}`);
    window.location.replace(landingFor());
  } catch (err) {
    const msg = "Something went wrong. Please try again.";
    errorBox.textContent = msg;
    errorBox.style.display = "block";
    showToast(msg);
    btn.disabled = false;
    btn.textContent = "Sign in";
  }
});