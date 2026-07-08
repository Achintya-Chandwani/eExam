// ============================================================
// eExam — shared API + session utilities (used by every page)
// ============================================================

const API_BASE = "/api";

function getSession() {
  const raw = localStorage.getItem("eexam_session");
  return raw ? JSON.parse(raw) : null;
}

function setSession(session) {
  localStorage.setItem("eexam_session", JSON.stringify(session));
}

function clearSession() {
  localStorage.removeItem("eexam_session");
}

function requireRole(role) {
  const session = getSession();
  if (!session || session.role !== role) {
    window.location.href = "/index.html";
    return null;
  }
  return session;
}

function logout() {
  clearSession();
  window.location.href = "/index.html";
}

async function api(path, options = {}) {
  const session = getSession();
  const headers = Object.assign(
    { "Content-Type": "application/json" },
    options.headers || {}
  );
  if (session && session.token) {
    headers["Authorization"] = "Bearer " + session.token;
  }

  const res = await fetch(API_BASE + path, Object.assign({}, options, { headers }));

  if (res.status === 401 || res.status === 403) {
    if (res.status === 401) {
      clearSession();
      window.location.href = "/index.html";
      throw new Error("Session expired. Please log in again.");
    }
    const body = await safeJson(res);
    throw new Error((body && body.error) || "You don't have permission to do that.");
  }

  const body = await safeJson(res);

  if (!res.ok) {
    const message = (body && (body.error || Object.values(body)[0])) || "Something went wrong.";
    throw new Error(message);
  }

  return body;
}

async function safeJson(res) {
  const text = await res.text();
  if (!text) return null;
  try { return JSON.parse(text); } catch (e) { return null; }
}

function toast(message, type = "success") {
  const el = document.createElement("div");
  el.className = "toast " + type;
  el.textContent = message;
  document.body.appendChild(el);
  setTimeout(() => el.remove(), 3800);
}

function fmtDateTime(iso) {
  if (!iso) return "-";
  const d = new Date(iso);
  return d.toLocaleString(undefined, { dateStyle: "medium", timeStyle: "short" });
}

function toLocalInputValue(iso) {
  if (!iso) return "";
  const d = new Date(iso);
  const pad = (n) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}
