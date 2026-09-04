class AdminTopbar extends HTMLElement {
  connectedCallback() {
    const pageTitle = this.getAttribute("page-title") || "Admin";
    const user = window.AdminAuth.getUser();
    const name = user?.firstName || "Admin";

    this.innerHTML = `
      <header class="topbar">
        <button class="menu-button" type="button" aria-label="Toggle sidebar" aria-expanded="true">☰</button>
        <h1>${this.escapeHtml(pageTitle)}</h1>
        <label class="search">
          <span aria-hidden="true">⌕</span>
          <input type="search" placeholder="Search anything..." aria-label="Search this page" />
          <kbd>⌘K</kbd>
        </label>
        <button class="notification" type="button" aria-label="Open notifications">♧<span>3</span></button>
        <button class="top-profile" type="button" aria-label="Open ${this.escapeHtml(name)}'s profile" title="Open profile">
          <span class="avatar">${this.escapeHtml(name.charAt(0).toUpperCase())}</span>
          <span>${this.escapeHtml(name)}</span>
        </button>
      </header>`;

    const searchInput = this.querySelector('input[type="search"]');
    this.querySelector(".menu-button").addEventListener("click", (event) => {
      this.dispatchEvent(new CustomEvent("admin-sidebar-toggle", { bubbles: true }));
      event.currentTarget.setAttribute("aria-expanded", String(event.currentTarget.getAttribute("aria-expanded") !== "true"));
    });
    searchInput.addEventListener("input", () => {
      window.dispatchEvent(new CustomEvent("admin-global-search", { detail: searchInput.value }));
    });
    this.querySelector(".notification").addEventListener("click", () => {
      window.location.href = "notifications.html";
    });
    this.querySelector(".top-profile").addEventListener("click", () => { window.location.href = "profile.html"; });

    this.handleShortcut = (event) => {
      if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === "k") {
        event.preventDefault();
        searchInput.focus();
      }
    };
    document.addEventListener("keydown", this.handleShortcut);
    this.loadNotificationCount();
  }

  disconnectedCallback() {
    document.removeEventListener("keydown", this.handleShortcut);
  }

  setTitle(title) {
    this.setAttribute("page-title", title);
    const heading = this.querySelector("h1");
    if (heading) heading.textContent = title;
  }

  setSidebarExpanded(expanded) {
    this.querySelector(".menu-button")?.setAttribute("aria-expanded", String(expanded));
  }

  async loadNotificationCount() {
    const badge = this.querySelector(".notification span");
    try {
      const response = await fetch(window.AdminAuth.apiBaseUrl + "/api/admin/notifications", {
        headers: { Authorization: `Bearer ${window.AdminAuth.getToken()}` },
      });
      if (!response.ok) return;
      const result = await response.json();
      badge.textContent = String(result.summary.unread);
      badge.hidden = result.summary.unread === 0;
    } catch {
      badge.hidden = true;
    }
  }

  escapeHtml(value) {
    return String(value).replace(/[&<>'"]/g, (character) => ({
      "&": "&amp;", "<": "&lt;", ">": "&gt;", "'": "&#39;", '"': "&quot;",
    })[character]);
  }
}

customElements.define("admin-topbar", AdminTopbar);
