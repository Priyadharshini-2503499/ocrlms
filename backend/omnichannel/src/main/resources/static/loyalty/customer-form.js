// customer-form.html — register a new customer
import { buildLayout } from "../assets/js/layout.js";
import { CustomerAPI } from "../assets/js/endpoints.js";
import { showToast, showFormError, el } from "../assets/js/ui.js";

buildLayout({
  active: "customers",
  title: "Register Customer",
  subtitle: "Create a loyalty profile",
  role: "Customer Service",
  initials: "CS",
});

el("customer-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  showFormError("");

  const f = e.target;
  const body = {
    fullName: f.fullName.value.trim(),
    emailId: f.emailId.value.trim(),
    phoneNumber: f.phoneNumber.value.trim(),
  };

  if (!body.fullName || !body.emailId || !body.phoneNumber) {
    showFormError("Please fill in all required fields.");
    return;
  }

  try {
    await CustomerAPI.register(body);
    showToast("Customer registered.");
    setTimeout(() => (window.location.href = "customers.html"), 600);
  } catch (err) {
    showFormError(err.message);
  }
});

