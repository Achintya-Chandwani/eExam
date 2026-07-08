// Redirect if already logged in
(function () {
  const session = getSession();
  if (session) {
    window.location.href = "/" + session.role.toLowerCase() + "/dashboard.html";
  }
})();

const loginForm = document.getElementById("loginForm");
const errMsg = document.getElementById("errMsg");
const submitBtn = document.getElementById("submitBtn");

loginForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  errMsg.textContent = "";
  submitBtn.disabled = true;
  submitBtn.textContent = "Signing in…";

  const username = document.getElementById("username").value.trim();
  const password = document.getElementById("password").value;

  try {
    const res = await fetch(API_BASE + "/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password }),
    });
    const data = await res.json();

    if (!res.ok) {
      throw new Error(data.error || "Invalid username or password");
    }

    setSession(data);
    window.location.href = "/" + data.role.toLowerCase() + "/dashboard.html";
  } catch (err) {
    errMsg.textContent = err.message;
  } finally {
    submitBtn.disabled = false;
    submitBtn.textContent = "Sign in";
  }
});
