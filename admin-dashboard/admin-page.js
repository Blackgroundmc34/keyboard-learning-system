const API_BASE_URL=window.AdminAuth.apiBaseUrl;
const supportedViews = new Set(["users","lessons","enrollments","progress","activities","messages","announcements","notifications","reports","settings","audit-logs"]);
const view = new URLSearchParams(window.location.search).get("view") || "users";
if (!supportedViews.has(view)) window.location.replace("dashboard.html");

const session = window.AdminAuth.requireAdmin();
const token = session?.token;
let columns = [];
let records = [];
let title = "Admin";

document.querySelector("#record-search").addEventListener("input", renderRecords);
window.addEventListener("admin-global-search", (event) => { document.querySelector("#record-search").value = event.detail; renderRecords(); });
document.querySelector("#refresh-button").addEventListener("click", loadRecords);
document.querySelector("#page-action").addEventListener("click", () => showMessage(`Create ${title.toLowerCase()} functionality will be added with its form.`));
loadRecords();

async function loadRecords() {
  try {
    const response = await fetch(`${API_BASE_URL}/api/admin/resources/${encodeURIComponent(view)}`, { headers: { Authorization: `Bearer ${token}` } });
    if (response.status === 401 || response.status === 403) return window.AdminAuth.clearSession();
    const result = await response.json();
    if (!response.ok) throw new Error(result.message || "Unable to load records");
    title = result.title; columns = result.columns; records = result.records;
    document.title = `${title} | Keyboard Learning System`;
    document.querySelector("admin-topbar").setTitle(title);
    document.querySelector("#page-title").textContent = title;
    document.querySelector("#breadcrumb-title").textContent = title;
    document.querySelector("#page-subtitle").textContent = `Manage ${title.toLowerCase()} in the platform`;
    document.querySelector("#record-total").textContent = Number(result.total).toLocaleString();
    document.querySelector("#resource-head").innerHTML = columns.map(column => `<th>${formatLabel(column)}</th>`).join("");
    renderRecords();
  } catch (error) {
    showMessage(error.message || "Unable to connect to the backend server.");
    document.querySelector("#resource-body").innerHTML = '<tr class="empty-row"><td>Records could not be loaded.</td></tr>';
  }
}

function renderRecords() {
  const term = document.querySelector("#record-search").value.trim().toLowerCase();
  const filtered = records.filter(record => !term || Object.values(record).some(value => String(value ?? "").toLowerCase().includes(term)));
  const body = document.querySelector("#resource-body");
  body.innerHTML = filtered.length
    ? filtered.map(record => `<tr>${columns.map(column => `<td title="${escapeAttribute(formatValue(record[column]))}">${renderValue(record[column])}</td>`).join("")}</tr>`).join("")
    : `<tr class="empty-row"><td colspan="${Math.max(columns.length,1)}">No records found.</td></tr>`;
  document.querySelector("#result-count").textContent = `Showing ${filtered.length} of ${records.length} records`;
}

function renderValue(value) { if (value === null || value === undefined || value === "") return "—"; if (value === 0 || value === 1) return `<span class="boolean ${value ? "" : "no"}">${value ? "Yes" : "No"}</span>`; return escapeHtml(formatValue(value)); }
function formatValue(value) { if (value === null || value === undefined) return ""; if (typeof value === "string" && /^\d{4}-\d{2}-\d{2}T/.test(value)) return new Intl.DateTimeFormat("en-ZA",{dateStyle:"medium",timeStyle:"short"}).format(new Date(value)); return String(value); }
function formatLabel(value) { return value.replace(/([a-z])([A-Z])/g,"$1 $2").replaceAll("-"," ").replace(/\b\w/g,letter=>letter.toUpperCase()); }
function showMessage(text) { const element=document.querySelector("#page-message"); element.textContent=text; element.classList.add("show"); window.setTimeout(()=>element.classList.remove("show"),3500); }
function escapeHtml(value) { const element=document.createElement("span"); element.textContent=String(value); return element.innerHTML; }
function escapeAttribute(value) { return escapeHtml(value).replaceAll('"',"&quot;"); }
