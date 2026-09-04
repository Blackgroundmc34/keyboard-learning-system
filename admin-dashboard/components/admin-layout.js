class AdminLayout extends HTMLElement {
  connectedCallback() {
    const pageTitle = this.getAttribute("page-title") || "Admin";
    const activePage = this.getAttribute("active-page") || new URLSearchParams(window.location.search).get("view") || "";
    const pageContent = this.innerHTML;

    const manageLinks = [
      ["users", "♙", "Users"],
      ["courses", "▣", "Courses"],
      ["lessons", "□", "Lessons"],
      ["enrollments", "♢", "Enrollments"],
      ["progress", "☑", "Progress"],
      ["activities", "▤", "Learning Activities"],
      ["messages", "✉", "Messages"],
      ["announcements", "◁", "Announcements"],
      ["notifications", "♧", "Notifications"],
    ];
    const systemLinks = [
      ["reports", "▥", "Reports"],
      ["settings", "⚙", "System Settings"],
      ["audit-logs", "▤", "Audit Logs"],
    ];

    this.innerHTML = `
      <aside class="sidebar" id="sidebar">
        <a class="logo" href="dashboard.html">
          <img class="logo-image" src="assets/images/logo_admin.png" alt="Keyboard Learning System" />
        </a>
        <nav aria-label="Admin navigation">
          ${this.renderLink("dashboard", "▦", "Dashboard", activePage)}
          <p>Manage</p>
          ${manageLinks.map(([key, icon, label]) => this.renderLink(key, icon, label, activePage)).join("")}
          <p>System</p>
          ${systemLinks.map(([key, icon, label]) => this.renderLink(key, icon, label, activePage)).join("")}
        </nav>
        <button class="admin-profile" id="logout-button" type="button" title="Sign out">
          <span class="avatar" id="sidebar-avatar">A</span>
          <span><strong id="sidebar-name">Admin User</strong><small>Sign out</small></span>
          <b aria-hidden="true">↪</b>
        </button>
      </aside>
      <div class="app">
        <admin-topbar page-title="${this.escapeHtml(pageTitle)}"></admin-topbar>
        ${pageContent}
      </div>
      <div class="overlay" id="overlay"></div>`;

    this.bindGlobalInteractions();
  }

  renderLink(key, icon, label, activePage) {
    const href = key === "dashboard"
      ? "dashboard.html"
      : ["courses", "users", "lessons", "enrollments", "progress", "activities", "messages", "announcements", "notifications", "reports", "settings", "audit-logs"].includes(key)
        ? `${key}.html`
        : `admin-page.html?view=${encodeURIComponent(key)}`;
    const active = key === activePage ? ' class="active" aria-current="page"' : "";
    return `<a href="${href}"${active}><span>${icon}</span>${this.escapeHtml(label)}${["users", "courses", "lessons", "enrollments", "progress", "activities", "messages", "announcements", "notifications", "reports", "settings", "audit-logs"].includes(key) ? "<b>›</b>" : ""}</a>`;
  }

  bindGlobalInteractions() {
    const sidebar = this.querySelector("#sidebar");
    const app = this.querySelector(".app");
    const overlay = this.querySelector("#overlay");
    if (localStorage.getItem("adminSidebarCollapsed") === "true" && window.innerWidth > 760) {
      sidebar.classList.add("collapsed");
      app.classList.add("expanded");
      this.querySelector("admin-topbar").setSidebarExpanded(false);
    }
    this.addEventListener("admin-sidebar-toggle", () => {
      if (window.innerWidth <= 760) {
        const open = sidebar.classList.toggle("open");
        overlay.classList.toggle("show", open);
        this.querySelector("admin-topbar").setSidebarExpanded(open);
      } else {
        const collapsed = sidebar.classList.toggle("collapsed");
        app.classList.toggle("expanded", collapsed);
        this.querySelector("admin-topbar").setSidebarExpanded(!collapsed);
        localStorage.setItem("adminSidebarCollapsed", String(collapsed));
      }
    });
    overlay.addEventListener("click", () => {
      sidebar.classList.remove("open");
      overlay.classList.remove("show");
      this.querySelector("admin-topbar").setSidebarExpanded(false);
    });
    this.querySelector("#logout-button").addEventListener("click", () => {
      window.AdminAuth.clearSession();
    });

    const user = window.AdminAuth.getUser();
    if (user) {
      const name = user.firstName || "Admin";
      this.querySelector("#sidebar-name").textContent = `${user.firstName} ${user.lastName}`;
      this.querySelectorAll("#sidebar-avatar").forEach((element) => {
        element.textContent = name.charAt(0).toUpperCase();
      });
    }
  }

  escapeHtml(value) {
    return String(value).replace(/[&<>'"]/g, (character) => ({
      "&": "&amp;", "<": "&lt;", ">": "&gt;", "'": "&#39;", '"': "&quot;",
    })[character]);
  }
}

customElements.define("admin-layout", AdminLayout);
