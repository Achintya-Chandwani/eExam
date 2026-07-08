const session = requireRole("STUDENT");
if (session) {
  document.getElementById("userName").textContent = session.fullName;
  document.getElementById("userRole").textContent = session.role;
}

const navButtons = document.querySelectorAll(".sidebar nav button");
navButtons.forEach((btn) => btn.addEventListener("click", () => showView(btn.dataset.view)));

function showView(view) {
  document.querySelectorAll("main > section").forEach((s) => (s.style.display = "none"));
  document.getElementById("view-" + view).style.display = "block";
  navButtons.forEach((b) => b.classList.toggle("active", b.dataset.view === view));
  if (view === "overview") loadOverview();
  if (view === "exams") loadExams();
  if (view === "practice") loadPractice();
  if (view === "bookmarks") loadBookmarks();
  if (view === "results") loadResults();
}

async function loadOverview() {
  try {
    const [exams, results, bookmarks] = await Promise.all([
      api("/student/exams"), api("/student/results"), api("/student/bookmarks"),
    ]);
    document.getElementById("statGrid").innerHTML = `
      <div class="card stat-card"><div class="value">${exams.length}</div><div class="label">Exams Open Now</div></div>
      <div class="card stat-card"><div class="value">${results.length}</div><div class="label">Exams Completed</div></div>
      <div class="card stat-card"><div class="value">${bookmarks.length}</div><div class="label">Bookmarked</div></div>
    `;
  } catch (e) { toast(e.message, "error"); }
}

// ---- Available exams ----
async function loadExams() {
  try {
    const exams = await api("/student/exams");
    document.getElementById("examsList").innerHTML = exams.map((ex) => `
      <div class="exam-ticket">
        <div class="stub"><div class="num">${ex.durationMinutes}</div><div class="unit">mins</div></div>
        <div class="body">
          <h3>${escapeHtml(ex.title)}</h3>
          <div class="meta">Closes ${fmtDateTime(ex.scheduledEnd)}</div>
          <div class="desc">${escapeHtml(ex.description || "")}</div>
        </div>
        <div class="actions"><button class="btn btn-amber" onclick="beginExam(${ex.id})">Start Exam</button></div>
      </div>
    `).join("") || `<div class="empty-state">No exams are open right now. Check back later.</div>`;
  } catch (e) { toast(e.message, "error"); }
}

// ---- Exam attempt flow ----
let attemptTimerInterval = null;
let currentAttemptExamId = null;

async function beginExam(examId) {
  if (!confirm("Once you start, the timer begins immediately. Continue?")) return;
  try {
    const attempt = await api(`/student/exams/${examId}/start`, { method: "POST" });
    const questions = await api(`/student/exams/${examId}/questions`);

    currentAttemptExamId = examId;
    document.getElementById("attemptExamTitle").textContent = attempt.exam.title;
    document.getElementById("attemptQuestionsArea").innerHTML = questions.map((q, i) => `
      <div class="question-card">
        <div class="qhead"><span class="qnum">Q${i + 1} · ${q.marks} mark(s)</span></div>
        <div style="margin-bottom:14px;">${escapeHtml(q.questionText)}</div>
        <div class="option-group" data-question-id="${q.id}">
          ${optionRow(q.id, "A", q.optionA)}
          ${optionRow(q.id, "B", q.optionB)}
          ${optionRow(q.id, "C", q.optionC)}
          ${optionRow(q.id, "D", q.optionD)}
        </div>
      </div>
    `).join("");

    document.getElementById("dashboardShell").style.display = "none";
    document.getElementById("examAttemptView").style.display = "block";

    const endTime = new Date(new Date(attempt.startedAt).getTime() + attempt.exam.durationMinutes * 60000);
    const hardEnd = new Date(attempt.exam.scheduledEnd);
    startTimer(endTime < hardEnd ? endTime : hardEnd);
  } catch (e) { toast(e.message, "error"); }
}

function optionRow(questionId, letter, text) {
  return `
    <div class="option-row" data-letter="${letter}" onclick="selectOption(${questionId}, '${letter}', this)">
      <div class="option-letter">${letter}</div><div>${escapeHtml(text)}</div>
    </div>`;
}

const selectedAnswers = {};
function selectOption(questionId, letter, el) {
  selectedAnswers[questionId] = letter;
  const group = el.parentElement;
  group.querySelectorAll(".option-row").forEach((r) => r.classList.remove("selected"));
  el.classList.add("selected");
}

function startTimer(endTime) {
  clearInterval(attemptTimerInterval);
  const timerEl = document.getElementById("examTimer");
  function tick() {
    const remainingMs = endTime - new Date();
    if (remainingMs <= 0) {
      clearInterval(attemptTimerInterval);
      timerEl.textContent = "00:00";
      toast("Time's up — submitting automatically", "error");
      submitAttempt(true);
      return;
    }
    const totalSec = Math.floor(remainingMs / 1000);
    const mins = String(Math.floor(totalSec / 60)).padStart(2, "0");
    const secs = String(totalSec % 60).padStart(2, "0");
    timerEl.textContent = `${mins}:${secs}`;
    timerEl.classList.toggle("warn", totalSec < 60);
  }
  tick();
  attemptTimerInterval = setInterval(tick, 1000);
}

document.getElementById("submitAttemptBtn").addEventListener("click", () => submitAttempt(false));

async function submitAttempt(auto) {
  clearInterval(attemptTimerInterval);
  if (!auto && !confirm("Submit your answers now? This cannot be undone.")) return;

  const answers = Object.keys(selectedAnswers).map((qid) => ({
    questionId: parseInt(qid, 10),
    selectedOption: selectedAnswers[qid],
  }));

  try {
    const result = await api(`/student/exams/${currentAttemptExamId}/submit`, {
      method: "POST",
      body: JSON.stringify({ answers }),
    });
    toast(`Submitted! Score: ${result.score}/${result.totalMarks}`);
    Object.keys(selectedAnswers).forEach((k) => delete selectedAnswers[k]);
    document.getElementById("examAttemptView").style.display = "none";
    document.getElementById("dashboardShell").style.display = "flex";
    showView("results");
  } catch (e) {
    toast(e.message, "error");
    document.getElementById("examAttemptView").style.display = "none";
    document.getElementById("dashboardShell").style.display = "flex";
    showView("exams");
  }
}

// ---- Practice ----
async function loadPractice() {
  try {
    const questions = await api("/student/practice");
    document.getElementById("practiceList").innerHTML = questions.map((q) => `
      <div class="question-card">
        <div class="qhead">
          <span class="qnum">${escapeHtml(q.examTitle)}</span>
          <button class="btn btn-ghost btn-sm" onclick="toggleBookmark(${q.id}, this)">☆ Bookmark</button>
        </div>
        <div style="margin-bottom:14px;">${escapeHtml(q.questionText)}</div>
        ${["A","B","C","D"].map((letter) => `
          <div class="option-row ${letter === q.correctOption ? "correct" : ""}">
            <div class="option-letter">${letter}</div><div>${escapeHtml(q["option" + letter])}</div>
          </div>
        `).join("")}
      </div>
    `).join("") || `<div class="empty-state">No past exams available for practice yet.</div>`;
  } catch (e) { toast(e.message, "error"); }
}

async function toggleBookmark(questionId, btn) {
  try {
    await api(`/student/questions/${questionId}/bookmark`, { method: "POST" });
    btn.textContent = "★ Bookmarked";
    toast("Question bookmarked");
  } catch (e) { toast(e.message, "error"); }
}

// ---- Bookmarks ----
async function loadBookmarks() {
  try {
    const bookmarks = await api("/student/bookmarks");
    document.getElementById("bookmarksList").innerHTML = bookmarks.map((b) => `
      <div class="question-card">
        <div class="qhead">
          <span class="qnum">${escapeHtml(b.question.examTitle)}</span>
          <button class="btn btn-danger btn-sm" onclick="removeBookmark(${b.question.id})">Remove</button>
        </div>
        <div style="margin-bottom:14px;">${escapeHtml(b.question.questionText)}</div>
        ${["A","B","C","D"].map((letter) => `
          <div class="option-row ${letter === b.question.correctOption ? "correct" : ""}">
            <div class="option-letter">${letter}</div><div>${escapeHtml(b.question["option" + letter])}</div>
          </div>
        `).join("")}
      </div>
    `).join("") || `<div class="empty-state">You haven't bookmarked any questions yet.</div>`;
  } catch (e) { toast(e.message, "error"); }
}

async function removeBookmark(questionId) {
  try {
    await api(`/student/questions/${questionId}/bookmark`, { method: "DELETE" });
    toast("Bookmark removed");
    loadBookmarks();
  } catch (e) { toast(e.message, "error"); }
}

// ---- Results ----
async function loadResults() {
  try {
    const results = await api("/student/results");
    document.getElementById("resultsTable").innerHTML = results.map((r) => `
      <tr>
        <td>${escapeHtml(r.exam.title)}</td>
        <td class="mono">${r.score}/${r.totalMarks}</td>
        <td>${fmtDateTime(r.submittedAt)}</td>
      </tr>
    `).join("") || `<tr><td colspan="3"><div class="empty-state">No submitted attempts yet.</div></td></tr>`;
  } catch (e) { toast(e.message, "error"); }
}

function escapeHtml(str) {
  if (str === null || str === undefined) return "";
  return String(str).replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
}

showView("overview");
