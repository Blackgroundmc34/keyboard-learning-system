window.AdminAuth = {
  apiBaseUrl: `${window.location.protocol === "https:" ? "https:" : "http:"}//${window.location.hostname || "localhost"}:3000`,

  getToken() {
    return sessionStorage.getItem("adminToken");
  },

  getUser() {
    const storedUser = sessionStorage.getItem("adminUser");
    if (!storedUser) return null;
    try {
      const user = JSON.parse(storedUser);
      return user?.role === "ADMIN" ? user : null;
    } catch {
      return null;
    }
  },

  requireAdmin() {
    const token = this.getToken();
    const user = this.getUser();
    if (!token || !user) {
      this.clearSession();
      return null;
    }
    return { token, user };
  },

  clearSession() {
    // Remove legacy persistent sessions as well as the active tab session.
    localStorage.removeItem("adminToken");
    localStorage.removeItem("adminUser");
    sessionStorage.removeItem("adminToken");
    sessionStorage.removeItem("adminUser");
    window.location.replace("index.html");
  },
};
