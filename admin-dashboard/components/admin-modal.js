window.AdminModal = (() => {
  let root;
  let escapeHandler;

  function ensureRoot() {
    if (root) return root;
    root = document.createElement("div");
    root.id = "admin-modal-root";
    document.body.appendChild(root);
    return root;
  }

  function openForm({ title, fields, submitLabel = "Save", onSubmit }) {
    const container = ensureRoot();
    container.innerHTML = `<div class="modal-backdrop"><section class="admin-modal" role="dialog" aria-modal="true" aria-labelledby="modal-title">
      <div class="modal-header"><h2 id="modal-title">${escapeHtml(title)}</h2><button class="modal-close" type="button" aria-label="Close">×</button></div>
      <form class="modal-form"><div class="modal-fields">${fields.map(renderField).join("")}</div><div class="modal-error" role="alert"></div>
        <div class="modal-actions"><button class="secondary modal-cancel" type="button">Cancel</button><button class="primary modal-submit" type="submit">${escapeHtml(submitLabel)}</button></div>
      </form></section></div>`;
    activateCloseHandlers();
    const form = container.querySelector("form");
    form.addEventListener("submit", async (event) => {
      event.preventDefault();
      const button = form.querySelector(".modal-submit");
      const error = form.querySelector(".modal-error");
      error.classList.remove("show"); button.disabled = true; button.textContent = "Saving…";
      try {
        const data = Object.fromEntries(new FormData(form).entries());
        fields.filter(field => field.type === "checkbox").forEach(field => { data[field.name] = form.elements[field.name].checked; });
        await onSubmit(data);
      } catch (exception) {
        error.textContent = exception.message || "Unable to save this record.";
        error.classList.add("show"); button.disabled = false; button.textContent = submitLabel;
      }
    });
    container.querySelector("input, select, textarea")?.focus();
  }

  function showSuccess({ title, message, primaryLabel = "Done", secondaryLabel, onPrimary, onSecondary }) {
    const container = ensureRoot();
    if (escapeHandler) document.removeEventListener("keydown", escapeHandler);
    container.innerHTML = `<div class="modal-backdrop"><section class="admin-modal success-modal" role="dialog" aria-modal="true" aria-labelledby="success-title">
      <button class="modal-close floating" type="button" aria-label="Close">×</button><div class="success-icon">✓</div>
      <h2 id="success-title">${escapeHtml(title)}</h2><p>${escapeHtml(message)}</p>
      <div class="modal-actions centered">${secondaryLabel ? `<button class="secondary success-secondary" type="button">${escapeHtml(secondaryLabel)}</button>` : ""}<button class="primary success-primary" type="button">${escapeHtml(primaryLabel)}</button></div>
    </section></div>`;
    activateCloseHandlers();
    container.querySelector(".success-primary").addEventListener("click", () => { close(); onPrimary?.(); });
    container.querySelector(".success-secondary")?.addEventListener("click", () => { close(); onSecondary?.(); });
  }

  function confirm({ title, message, confirmLabel = "Delete", onConfirm }) {
    const container = ensureRoot();
    container.innerHTML = `<div class="modal-backdrop"><section class="admin-modal confirm-modal" role="alertdialog" aria-modal="true" aria-labelledby="confirm-title">
      <button class="modal-close floating" type="button" aria-label="Close">×</button><div class="danger-icon">!</div>
      <h2 id="confirm-title">${escapeHtml(title)}</h2><p>${escapeHtml(message)}</p><div class="modal-error" role="alert"></div>
      <div class="modal-actions centered"><button class="secondary modal-cancel" type="button">Cancel</button><button class="danger confirm-action" type="button">${escapeHtml(confirmLabel)}</button></div>
    </section></div>`;
    activateCloseHandlers();
    container.querySelector(".confirm-action").addEventListener("click", async event => {
      const error = container.querySelector(".modal-error"); event.currentTarget.disabled=true; event.currentTarget.textContent="Deleting…";
      try { await onConfirm(); close(); } catch (exception) { error.textContent=exception.message||"Unable to complete this action."; error.classList.add("show"); event.currentTarget.disabled=false; event.currentTarget.textContent=confirmLabel; }
    });
  }

  function renderField(field) {
    const required = field.required ? " required" : "";
    const full = field.full ? " full" : "";
    const label = `${escapeHtml(field.label)}${field.required ? ' <em>*</em>' : ""}`;
    if (field.type === "textarea") return `<label class="modal-field${full}"><span>${label}</span><textarea name="${escapeHtml(field.name)}" maxlength="${field.maxlength || 2000}" rows="${field.rows || 4}" placeholder="${escapeHtml(field.placeholder || "")}"${required}>${escapeHtml(field.value || "")}</textarea></label>`;
    if (field.type === "select") return `<label class="modal-field${full}"><span>${label}</span><select name="${escapeHtml(field.name)}"${required}><option value="">${escapeHtml(field.placeholder || "Select an option")}</option>${field.options.map(option => `<option value="${escapeHtml(option.value)}"${String(option.value) === String(field.value) ? " selected" : ""}>${escapeHtml(option.label)}</option>`).join("")}</select></label>`;
    if (field.type === "checkbox") return `<label class="modal-field checkbox${full}"><input type="checkbox" name="${escapeHtml(field.name)}" ${field.checked ? "checked" : ""}/><span>${escapeHtml(field.label)}</span></label>`;
    return `<label class="modal-field${full}"><span>${label}</span><input type="${field.type || "text"}" name="${escapeHtml(field.name)}" value="${escapeHtml(field.value || "")}" placeholder="${escapeHtml(field.placeholder || "")}" ${field.min !== undefined ? `min="${field.min}"` : ""}${field.max !== undefined ? ` max="${field.max}"` : ""}${field.step !== undefined ? ` step="${field.step}"` : ""}${required}/>${field.hint ? `<small>${escapeHtml(field.hint)}</small>` : ""}</label>`;
  }

  function activateCloseHandlers() {
    document.body.classList.add("modal-open");
    root.querySelectorAll(".modal-close,.modal-cancel").forEach(button => button.addEventListener("click", close));
    root.querySelector(".modal-backdrop").addEventListener("click", event => { if (event.target.classList.contains("modal-backdrop")) close(); });
    escapeHandler = event => { if (event.key === "Escape") close(); };
    document.addEventListener("keydown", escapeHandler);
  }

  function close() {
    if (!root) return;
    root.innerHTML = ""; document.body.classList.remove("modal-open");
    if (escapeHandler) document.removeEventListener("keydown", escapeHandler);
  }
  function escapeHtml(value) { const element=document.createElement("span"); element.textContent=String(value); return element.innerHTML; }
  return { openForm, showSuccess, confirm, close };
})();
