// return-form.html — initiate a return request
import { buildLayout } from "../assets/js/layout.js";
import { ReturnAPI } from "../assets/js/endpoints.js";
import { showToast, showFormError, el } from "../assets/js/ui.js";

buildLayout({
  active: "returns",
  title: "Initiate Return",
  subtitle: "Start a return request",
  role: "Customer Service",
  initials: "CS",
});

el("return-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  showFormError("");

  const f = e.target;
  const body = {
    orderId: Number(f.orderId.value),
    returnReason: f.returnReason.value.trim(),
    requestDate: f.requestDate.value || null,
  };

  if (!(body.orderId >= 1)) {
    showFormError("Please enter a valid Order ID.");
    return;
  }

  try {
    await ReturnAPI.create(body);
    showToast("Return initiated.");
    setTimeout(() => (window.location.href = "returns.html"), 600);
  } catch (err) {
    showFormError(err.message);
  }
});

