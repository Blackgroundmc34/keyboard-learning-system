const API_BASE_URL=window.AdminAuth.apiBaseUrl;
const session = window.AdminAuth.requireAdmin();
const token = session?.token;
let profile;
const fieldIds = ["first-name", "last-name", "email", "phone", "bio"];

document.querySelector("#edit-button").addEventListener("click", () => setEditing(true));
document.querySelector("#cancel-button").addEventListener("click", () => { fillProfile(profile); setEditing(false); });
document.querySelector("#profile-form").addEventListener("submit", saveProfile);
loadProfile();

async function loadProfile() {
  try {
    const response = await fetch(`${API_BASE_URL}/api/auth/me`, { headers: { Authorization: `Bearer ${token}` } });
    if (response.status === 401 || response.status === 403) return window.AdminAuth.clearSession();
    const result = await response.json();
    if (!response.ok) throw new Error(result.message || "Unable to load profile");
    profile = result.user;
    fillProfile(profile);
  } catch (error) { showMessage(error.message || "Unable to connect to the backend.", "error"); }
}

async function saveProfile(event) {
  event.preventDefault();
  try {
    const response = await fetch(`${API_BASE_URL}/api/auth/me`, {
      method: "PATCH", headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" },
      body: JSON.stringify({ firstName:value("first-name"), lastName:value("last-name"), email:value("email"), phone:value("phone"), bio:value("bio") }),
    });
    const result = await response.json();
    if (!response.ok) throw new Error(result.message || "Unable to update profile");
    profile = result.user;
    sessionStorage.setItem("adminUser", JSON.stringify({ id:profile.id, firstName:profile.firstName, lastName:profile.lastName, email:profile.email, role:profile.role }));
    fillProfile(profile); setEditing(false); showMessage("Profile updated successfully.", "success");
  } catch (error) { showMessage(error.message || "Unable to update profile.", "error"); }
}

function fillProfile(user) {
  if (!user) return;
  document.querySelector("#profile-name").textContent=`${user.firstName} ${user.lastName}`; document.querySelector("#profile-avatar").textContent=user.firstName.charAt(0).toUpperCase();
  document.querySelector("#profile-email").textContent=user.email; document.querySelector("#profile-phone").textContent=user.phone || "Not provided"; document.querySelector("#profile-created").textContent=formatDate(user.createdAt);
  document.querySelector("#profile-role").textContent=formatLabel(user.role); document.querySelector("#fact-role").textContent=formatLabel(user.role); document.querySelector("#fact-verified").textContent=user.emailVerifiedAt ? "Verified" : "Not verified"; document.querySelector("#fact-login").textContent=formatDate(user.lastLoginAt);
  document.querySelector("#first-name").value=user.firstName || ""; document.querySelector("#last-name").value=user.lastName || ""; document.querySelector("#email").value=user.email || ""; document.querySelector("#phone").value=user.phone || ""; document.querySelector("#bio").value=user.bio || "";
}
function setEditing(editing) { fieldIds.forEach(id=>document.querySelector(`#${id}`).disabled=!editing); document.querySelector("#form-actions").hidden=!editing; document.querySelector("#edit-button").hidden=editing; if(editing)document.querySelector("#first-name").focus(); }
function value(id) { return document.querySelector(`#${id}`).value.trim(); }
function formatDate(value) { return value ? new Intl.DateTimeFormat("en-ZA",{dateStyle:"medium",timeStyle:"short"}).format(new Date(value)) : "—"; }
function formatLabel(value) { return String(value).toLowerCase().replace(/\b\w/g,letter=>letter.toUpperCase()); }
function showMessage(text,type) { const element=document.querySelector("#profile-message"); element.textContent=text; element.className=`profile-message show ${type}`; }
