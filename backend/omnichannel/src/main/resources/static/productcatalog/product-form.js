
// product-form.html — create a new product
import { buildLayout } from "../assets/js/layout.js";
import { ProductAPI } from "../assets/js/endpoints.js";
import { showToast, showFormError, el } from "../assets/js/ui.js";

buildLayout({
  active: "products",
  title: "Add Product",
  subtitle: "Create a new catalog entry",
  role: "Merchandiser",
  initials: "MD",
});

el("product-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  showFormError("");

  const form = e.target;
  const body = {
    skuCode: form.skuCode.value.trim(),
    category: form.category.value.trim(),
    productName: form.productName.value.trim(),
    basePrice: Number(form.basePrice.value),
    productStatus: form.productStatus.value,
  };

  if (!body.skuCode || !body.category || !body.productName) {
    showFormError("Please fill in all required fields.");
    return;
  }
  if (!(body.basePrice >= 0)) {
    showFormError("Base price must be a positive number.");
    return;
  }

  try {
    await ProductAPI.create(body);
    showToast("Product saved.");
    setTimeout(() => (window.location.href = "products.html"), 600);
  } catch (err) {
    showFormError(err.message);
  }
});

