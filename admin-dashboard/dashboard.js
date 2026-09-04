const API_BASE_URL=window.AdminAuth.apiBaseUrl;
const session = window.AdminAuth.requireAdmin();
const token = session?.token;
const user = session?.user;

const name = user?.firstName || "Admin";
document.querySelector("#welcome-name").textContent = name;
document.querySelector("#today").textContent = new Intl.DateTimeFormat("en-ZA", { dateStyle: "medium" }).format(new Date());

loadDashboard();
window.addEventListener("admin-global-search", (event) => {
  const term = String(event.detail || "").toLowerCase();
  document.querySelectorAll(".data-row").forEach((row) => {
    row.hidden = Boolean(term) && !row.textContent.toLowerCase().includes(term);
  });
});

async function loadDashboard() {
  try {
    const response = await fetch(`${API_BASE_URL}/api/admin/dashboard`, { headers: { Authorization: `Bearer ${token}` } });
    if (response.status === 401 || response.status === 403) return window.AdminAuth.clearSession();
    const result = await response.json();
    if (!response.ok) throw new Error(result.message || "Unable to load dashboard");
    renderDashboard(result.dashboard);
  } catch (error) {
    document.querySelector("#overview-bars").innerHTML = `<div class="empty">${escapeHtml(error.message || "Unable to connect to the backend")}</div>`;
  }
}

function renderDashboard(data) {
  const totals = data.totals;
  setText("total-users", totals.users); setText("total-courses", totals.courses); setText("total-lessons", totals.lessons); setText("total-enrollments", totals.enrollments);
  setText("role-students", data.usersByRole.students); setText("role-instructors", data.usersByRole.instructors); setText("role-admins", data.usersByRole.admins);
  const roleTotal = data.usersByRole.students + data.usersByRole.instructors + data.usersByRole.admins;
  setText("role-total", roleTotal);
  const studentEnd = roleTotal ? data.usersByRole.students / roleTotal * 100 : 0;
  const instructorEnd = roleTotal ? studentEnd + data.usersByRole.instructors / roleTotal * 100 : 0;
  document.querySelector("#role-donut").style.background = `conic-gradient(#7441e5 0 ${studentEnd}%, #3f8fea ${studentEnd}% ${instructorEnd}%, #ffa72f ${instructorEnd}% 100%)`;

  const values = [["Users",totals.users],["Courses",totals.courses],["Lessons",totals.lessons],["Enrollments",totals.enrollments]];
  const max = Math.max(...values.map(([,value]) => value), 1);
  document.querySelector("#overview-bars").innerHTML = values.map(([label,value]) => `<div class="bar-wrap" title="${value} ${label}"><i style="height:${Math.max(5,value/max*100)}%"></i><small>${label}</small></div>`).join("");

  renderList("recent-enrollments", data.recentEnrollments, (item, index) => `<div class="data-row"><span class="number">${index+1}</span><span><strong>${escapeHtml(item.studentName)}</strong><small>${escapeHtml(item.courseTitle)}</small></span><b>${relativeTime(item.enrolledAt)}</b></div>`);
  renderList("top-courses", data.topCourses, (item, index) => `<div class="data-row"><span class="number">${index+1}</span><span><strong>${escapeHtml(item.title)}</strong><small>Course</small></span><b>${item.enrollmentCount}</b></div>`);
}

function renderList(id, items, renderer) { document.querySelector(`#${id}`).innerHTML = items.length ? items.map(renderer).join("") : '<div class="empty">No data available yet</div>'; }
function setText(id, value) { document.querySelector(`#${id}`).textContent = Number(value).toLocaleString(); }
function relativeTime(date) { const minutes = Math.max(0,Math.floor((Date.now()-new Date(date).getTime())/60000)); if(minutes<1)return "now"; if(minutes<60)return `${minutes}m`; const hours=Math.floor(minutes/60); if(hours<24)return `${hours}h`; return `${Math.floor(hours/24)}d`; }
function escapeHtml(value) { const element=document.createElement("span"); element.textContent=String(value); return element.innerHTML; }
