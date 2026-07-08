const session = requireRole("ADMIN");
if (session) {
  document.getElementById("userName").textContent = session.fullName;
  document.getElementById("userRole").textContent = session.role;
}

// ---- View switching ----
const navButtons = document.querySelectorAll(".sidebar nav button");
navButtons.forEach((btn) => {
  btn.addEventListener("click", () => showView(btn.dataset.view));
});

function showView(view) {
  document.querySelectorAll("main > section").forEach((s) => (s.style.display = "none"));
  document.getElementById("view-" + view).style.display = "block";
  navButtons.forEach((b) => b.classList.toggle("active", b.dataset.view === view));
  if (view === "overview") loadOverview();
  if (view === "users") loadUsers();
  if (view === "exams") loadExams();
}

// ---- Overview ----
async function loadOverview() {
  try {
    const [users, exams] = await Promise.all([api("/admin/users"), api("/admin/exams")]);
    const students = users.filter((u) => u.role === "STUDENT").length;
    const examiners = users.filter((u) => u.role === "EXAMINER").length;
    const activeExams = exams.filter((e) => e.active).length;

    document.getElementById("statGrid").innerHTML = `
      ${statCard(students, "Students")}
      ${statCard(examiners, "Examiners")}
      ${statCard(exams.length, "Total Exams")}
      ${statCard(activeExams, "Active Exams")}
      ${statCard(users.length, "Total Accounts")}
      ${statCard(exams.reduce((s, e) => s + (e.questionCount || 0), 0), "Total Questions")}
    `;
  } catch (e) {
    toast(e.message, "error");
  }
}
function statCard(value, label) {
  return `<div class="card stat-card"><div class="value">${value}</div><div class="label">${label}</div></div>`;
}

// ---- Users ----
async function loadUsers() {
  try {
    const role = document.getElementById("roleFilter").value;
    const users = await api("/admin/users" + (role ? "?role=" + role : ""));
    document.getElementById("usersTable").innerHTML = users.map((u) => `
      <tr>
        <td>${escapeHtml(u.fullName)}</td>
        <td class="mono">${escapeHtml(u.username)}</td>
        <td>${escapeHtml(u.email)}</td>
        <td><span class="badge badge-${u.role.toLowerCase()}">${u.role}</span></td>
        <td><span class="badge ${u.enabled ? "badge-active" : "badge-inactive"}">${u.enabled ? "Active" : "Disabled"}</span></td>
        <td style="text-align:right; white-space:nowrap;">
          <button class="btn btn-ghost btn-sm" onclick="editUser(${u.id})">Edit</button>
          <button class="btn btn-ghost btn-sm" onclick="openResetModal(${u.id})">Reset PW</button>
          <button class="btn btn-ghost btn-sm" onclick="toggleEnabled(${u.id}, ${u.enabled})">${u.enabled ? "Disable" : "Enable"}</button>
          <button class="btn btn-danger btn-sm" onclick="deleteUser(${u.id})">Delete</button>
        </td>
      </tr>
    `).join("") || `<tr><td colspan="6"><div class="empty-state">No users yet.</div></td></tr>`;
  } catch (e) {
    toast(e.message, "error");
  }
}

let usersCache = [];
async function editUser(id) {
  try {
    const u = await api("/admin/users/" + id);
    openUserModal(u);
  } catch (e) { toast(e.message, "error"); }
}

function openUserModal(user) {
  document.getElementById("userModalTitle").textContent = user ? "Edit user" : "New user";
  document.getElementById("userId").value = user ? user.id : "";
  document.getElementById("fullName").value = user ? user.fullName : "";
  document.getElementById("username").value = user ? user.username : "";
  document.getElementById("username").disabled = !!user;
  document.getElementById("email").value = user ? user.email : "";
  document.getElementById("role").value = user ? user.role : "STUDENT";
  document.getElementById("password").value = "";
  document.getElementById("password").placeholder = user ? "Leave blank to keep current password" : "";
  document.getElementById("password").required = !user;
  document.getElementById("userModal").style.display = "flex";
}
function closeUserModal() { document.getElementById("userModal").style.display = "none"; }

document.getElementById("userForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const id = document.getElementById("userId").value;
  const payload = {
    fullName: document.getElementById("fullName").value.trim(),
    username: document.getElementById("username").value.trim(),
    email: document.getElementById("email").value.trim(),
    role: document.getElementById("role").value,
    password: document.getElementById("password").value,
  };
  try {
    if (id) {
      await api("/admin/users/" + id, { method: "PUT", body: JSON.stringify(payload) });
      toast("User updated");
    } else {
      await api("/admin/users", { method: "POST", body: JSON.stringify(payload) });
      toast("User created");
    }
    closeUserModal();
    loadUsers();
  } catch (e) {
    toast(e.message, "error");
  }
});

function openResetModal(id) {
  document.getElementById("resetUserId").value = id;
  document.getElementById("newPassword").value = "";
  document.getElementById("resetModal").style.display = "flex";
}
function closeResetModal() { document.getElementById("resetModal").style.display = "none"; }

document.getElementById("resetForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const id = document.getElementById("resetUserId").value;
  try {
    await api("/admin/users/" + id + "/reset-password", {
      method: "POST",
      body: JSON.stringify({ newPassword: document.getElementById("newPassword").value }),
    });
    toast("Password reset");
    closeResetModal();
  } catch (e) {
    toast(e.message, "error");
  }
});

async function toggleEnabled(id, currentlyEnabled) {
  try {
    await api(`/admin/users/${id}/${currentlyEnabled ? "disable" : "enable"}`, { method: "POST" });
    toast(currentlyEnabled ? "User disabled" : "User enabled");
    loadUsers();
  } catch (e) {
    toast(e.message, "error");
  }
}

async function deleteUser(id) {
  if (!confirm("Delete this user permanently?")) return;
  try {
    await api("/admin/users/" + id, { method: "DELETE" });
    toast("User deleted");
    loadUsers();
  } catch (e) {
    toast(e.message, "error");
  }
}

// ---- Exams (read-only oversight) ----
async function loadExams() {
  try {
    const exams = await api("/admin/exams");
    document.getElementById("examsList").innerHTML = exams.map((ex) => `
      <div class="exam-ticket">
        <div class="stub"><div class="num">${ex.durationMinutes}</div><div class="unit">mins</div></div>
        <div class="body">
          <h3>${escapeHtml(ex.title)}</h3>
          <div class="meta">By ${escapeHtml(ex.examinerName || "—")} · ${fmtDateTime(ex.scheduledStart)} → ${fmtDateTime(ex.scheduledEnd)} · ${ex.questionCount || 0} questions</div>
          <div class="desc">${escapeHtml(ex.description || "")}</div>
        </div>
        <div class="actions"><span class="badge ${ex.active ? "badge-active" : "badge-inactive"}">${ex.active ? "Active" : "Inactive"}</span></div>
      </div>
    `).join("") || `<div class="empty-state">No exams have been created yet.</div>`;
  } catch (e) {
    toast(e.message, "error");
  }
}

function escapeHtml(str) {
  if (str === null || str === undefined) return "";
  return String(str).replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
}

showView("overview");
