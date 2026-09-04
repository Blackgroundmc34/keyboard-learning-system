const API_BASE_URL = `${window.location.protocol === "https:" ? "https:" : "http:"}//${window.location.hostname || "localhost"}:3000`;
const form = document.querySelector("#login-form");
const emailInput = document.querySelector("#email");
const passwordInput = document.querySelector("#password");
const togglePassword = document.querySelector("#toggle-password");
const submitButton = document.querySelector("#submit-button");
const message = document.querySelector("#form-message");

togglePassword.addEventListener("click", () => {
  const isPassword = passwordInput.type === "password";
  passwordInput.type = isPassword ? "text" : "password";
  togglePassword.setAttribute("aria-label", isPassword ? "Hide password" : "Show password");
});

document.querySelector("#forgot-password").addEventListener("click", (event) => {
  event.preventDefault();
  showMessage("Password recovery will be added in a later step.", "error");
});

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  clearMessage();
  const email = emailInput.value.trim();
  const password = passwordInput.value;

  if (!email || !password) {
    showMessage("Enter both your email address and password.", "error");
    return;
  }

  setLoading(true);
  try {
    const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    });
    const result = await response.json();
    if (!response.ok) throw new Error(result.message || "Unable to sign in.");
    if (result.user?.role !== "ADMIN") throw new Error("This account does not have administrator access.");

    localStorage.removeItem("adminToken");
    localStorage.removeItem("adminUser");
    sessionStorage.setItem("adminToken", result.token);
    sessionStorage.setItem("adminUser", JSON.stringify(result.user));
    showMessage("Login successful. Opening dashboard…", "success");
    window.setTimeout(() => { window.location.href = "dashboard.html"; }, 450);
  } catch (error) {
    const text = error instanceof Error ? error.message : "Unable to connect to the server.";
    showMessage(text === "fetch failed" ? "Unable to connect to the backend server." : text, "error");
  } finally {
    setLoading(false);
  }
});

function showMessage(text, type) { message.textContent = text; message.className = `form-message ${type}`; }
function clearMessage() { message.textContent = ""; message.className = "form-message"; }
function setLoading(loading) { submitButton.disabled = loading; submitButton.querySelector("span").textContent = loading ? "Signing In…" : "Sign In"; }
