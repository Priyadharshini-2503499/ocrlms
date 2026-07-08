
(function () {
  "use strict";

  var ROLE_KEY = "auriaRole";

  function showToast(message) {
    if (!message) return;
    var wrap = document.querySelector(".toast-wrap");
    if (!wrap) return;
    var t = document.createElement("div");
    t.className = "toast";
    t.textContent = message;
    wrap.appendChild(t);
    requestAnimationFrame(function () { t.classList.add("show"); });
    setTimeout(function () {
      t.classList.remove("show");
      setTimeout(function () { t.remove(); }, 300);
    }, 3200);
  }

  function reindexItemRows() {
    var rows = document.querySelectorAll("#item-rows tr");
    rows.forEach(function (row, i) {
      var sel = row.querySelector("select");
      var qty = row.querySelector("input");
      if (sel) sel.name = "items[" + i + "].productId";
      if (qty) qty.name = "items[" + i + "].quantity";
    });
  }

  function initItemRows() {
    var addBtn = document.getElementById("add-item");
    var tpl = document.getElementById("item-row-tpl");
    var body = document.getElementById("item-rows");
    if (!addBtn || !tpl || !body) return;

    addBtn.addEventListener("click", function () {
      body.appendChild(tpl.content.cloneNode(true));
      reindexItemRows();
    });

    body.addEventListener("click", function (e) {
      var btn = e.target.closest(".remove-item");
      if (!btn) return;
      var row = btn.closest("tr");
      if (row) row.remove();
      reindexItemRows();
    });
  }

  function initRole() {
    document.querySelectorAll(".role-card[data-role]").forEach(function (card) {
      card.addEventListener("click", function () {
        localStorage.setItem(ROLE_KEY, card.getAttribute("data-role"));
      });
    });

    var role = localStorage.getItem(ROLE_KEY) || "admin";
    document.querySelectorAll(".sidebar .nav-group[data-roles]").forEach(function (grp) {
      var roles = grp.getAttribute("data-roles").split(/\s+/);
      grp.style.display = roles.indexOf(role) === -1 ? "none" : "";
    });
  }

  document.addEventListener("DOMContentLoaded", function () {
    showToast(document.body.getAttribute("data-flash"));
    initItemRows();
    initRole();
  });
})();

