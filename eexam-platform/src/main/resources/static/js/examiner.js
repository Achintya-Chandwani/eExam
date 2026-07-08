const session = requireRole("EXAMINER");
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
}

let examsCache = [];

async function loadOverview() {
  try {
    const exams = await api("/examiner/exams");
    examsCache = exams;
    const active = exams.filter((e) => e.active).length;
    const totalQuestions = exams.reduce((s, e) => s + (e.questions ? e.questions.length : 0), 0);
    document.getElementById("statGrid").innerHTML = `
      <div class="card stat-card"><div class="value">${exams.length}</div><div class="label">Total Exams</div></div>
      <div class="card stat-card"><div class="value">${active}</div><div class="label">Active Exams</div></div>
      <div class="card stat-card"><div class="value">${totalQuestions}</div><div class="label">Total Questions</div></div>
    `;
  } catch (e) { toast(e.message, "error"); }
}

async function loadExams() {
  try {
    const exams = await api("/examiner/exams");
    examsCache = exams;
    document.getElementById("examsList").innerHTML = exams.map((ex) => `
      <div class="exam-ticket">
        <div class="stub"><div class="num">${ex.durationMinutes}</div><div class="unit">mins</div></div>
        <div class="body">
          <h3>${escapeHtml(ex.title)}</h3>
          <div class="meta">${fmtDateTime(ex.scheduledStart)} → ${fmtDateTime(ex.scheduledEnd)} · ${ex.questions ? ex.questions.length : 0} questions · ${ex.questions ? ex.questions.reduce((s,q)=>s+q.marks,0) : 0} marks</div>
          <div class="desc">${escapeHtml(ex.description || "")}</div>
          <span class="badge ${ex.active ? "badge-active" : "badge-inactive"}">${ex.active ? "Active" : "Inactive"}</span>
        </div>
        <div class="actions" style="flex-direction:column; align-items:stretch; gap:6px;">
          <button class="btn btn-ghost btn-sm" onclick="openQuestionsModal(${ex.id})">Questions</button>
          <button class="btn btn-ghost btn-sm" onclick="openEditExam(${ex.id})">Edit</button>
          <button class="btn btn-ghost btn-sm" onclick="openReportModal(${ex.id})">Report</button>
          <button class="btn btn-ghost btn-sm" onclick="toggleActive(${ex.id}, ${ex.active})">${ex.active ? "Deactivate" : "Activate"}</button>
          <button class="btn btn-danger btn-sm" onclick="deleteExam(${ex.id})">Delete</button>
        </div>
      </div>
    `).join("") || `<div class="empty-state">You haven't created any exams yet. Click "+ New Exam" to get started.</div>`;
  } catch (e) { toast(e.message, "error"); }
}

// ---- Exam modal ----
function openExamModal(exam) {
  document.getElementById("examModalTitle").textContent = exam ? "Edit exam" : "New exam";
  document.getElementById("examId").value = exam ? exam.id : "";
  document.getElementById("title").value = exam ? exam.title : "";
  document.getElementById("description").value = exam ? (exam.description || "") : "";
  document.getElementById("durationMinutes").value = exam ? exam.durationMinutes : 30;
  document.getElementById("scheduledStart").value = exam ? toLocalInputValue(exam.scheduledStart) : "";
  document.getElementById("scheduledEnd").value = exam ? toLocalInputValue(exam.scheduledEnd) : "";
  document.getElementById("examModal").style.display = "flex";
}
function closeExamModal() { document.getElementById("examModal").style.display = "none"; }
function openEditExam(id) {
  const exam = examsCache.find((e) => e.id === id);
  openExamModal(exam);
}

document.getElementById("examForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const id = document.getElementById("examId").value;
  const payload = {
    title: document.getElementById("title").value.trim(),
    description: document.getElementById("description").value.trim(),
    durationMinutes: parseInt(document.getElementById("durationMinutes").value, 10),
    scheduledStart: document.getElementById("scheduledStart").value,
    scheduledEnd: document.getElementById("scheduledEnd").value,
  };
  try {
    if (id) {
      await api("/examiner/exams/" + id, { method: "PUT", body: JSON.stringify(payload) });
      toast("Exam updated");
    } else {
      await api("/examiner/exams", { method: "POST", body: JSON.stringify(payload) });
      toast("Exam created");
    }
    closeExamModal();
    loadExams();
  } catch (e) { toast(e.message, "error"); }
});

async function toggleActive(id, currentlyActive) {
  try {
    await api(`/examiner/exams/${id}/${currentlyActive ? "deactivate" : "activate"}`, { method: "POST" });
    toast(currentlyActive ? "Exam deactivated" : "Exam activated");
    loadExams();
  } catch (e) { toast(e.message, "error"); }
}

async function deleteExam(id) {
  if (!confirm("Delete this exam and all its questions?")) return;
  try {
    await api("/examiner/exams/" + id, { method: "DELETE" });
    toast("Exam deleted");
    loadExams();
  } catch (e) { toast(e.message, "error"); }
}

// ---- Questions modal ----
let currentExamId = null;

async function openQuestionsModal(examId) {
  currentExamId = examId;
  const exam = examsCache.find((e) => e.id === examId);
  document.getElementById("questionsModalTitle").textContent = "Questions — " + (exam ? exam.title : "");
  resetQuestionForm();
  await loadQuestions();
  document.getElementById("questionsModal").style.display = "flex";
}
function closeQuestionsModal() {
  document.getElementById("questionsModal").style.display = "none";
  currentExamId = null;
  loadExams();
}

async function loadQuestions() {
  try {
    const questions = await api(`/examiner/exams/${currentExamId}/questions`);
    document.getElementById("questionsListArea").innerHTML = questions.map((q, i) => `
      <div class="question-card">
        <div class="qhead">
          <div><span class="qnum">Q${i + 1} · ${q.marks} mark(s)</span><div style="margin-top:6px;">${escapeHtml(q.questionText)}</div></div>
          <div style="white-space:nowrap;">
            <button class="btn btn-ghost btn-sm" onclick='editQuestion(${JSON.stringify(q).replace(/'/g, "&#39;")})'>Edit</button>
            <button class="btn btn-danger btn-sm" onclick="deleteQuestion(${q.id})">Delete</button>
          </div>
        </div>
        <div style="font-size:13px; color:var(--slate);">
          A. ${escapeHtml(q.optionA)} &nbsp; B. ${escapeHtml(q.optionB)} &nbsp; C. ${escapeHtml(q.optionC)} &nbsp; D. ${escapeHtml(q.optionD)}
          <br/>Correct: <strong style="color:var(--sage);">${q.correctOption}</strong>
        </div>
      </div>
    `).join("") || `<div class="empty-state">No questions yet — add the first one below.</div>`;
  } catch (e) { toast(e.message, "error"); }
}

function editQuestion(q) {
  document.getElementById("questionId").value = q.id;
  document.getElementById("questionText").value = q.questionText;
  document.getElementById("optionA").value = q.optionA;
  document.getElementById("optionB").value = q.optionB;
  document.getElementById("optionC").value = q.optionC;
  document.getElementById("optionD").value = q.optionD;
  document.getElementById("correctOption").value = q.correctOption;
  document.getElementById("marks").value = q.marks;
  document.getElementById("questionSubmitBtn").textContent = "Save changes";
  document.getElementById("cancelQuestionEdit").style.display = "inline-flex";
}

function resetQuestionForm() {
  document.getElementById("questionForm").reset();
  document.getElementById("questionId").value = "";
  document.getElementById("marks").value = 1;
  document.getElementById("questionSubmitBtn").textContent = "Add question";
  document.getElementById("cancelQuestionEdit").style.display = "none";
}

document.getElementById("questionForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const id = document.getElementById("questionId").value;
  const payload = {
    questionText: document.getElementById("questionText").value.trim(),
    optionA: document.getElementById("optionA").value.trim(),
    optionB: document.getElementById("optionB").value.trim(),
    optionC: document.getElementById("optionC").value.trim(),
    optionD: document.getElementById("optionD").value.trim(),
    correctOption: document.getElementById("correctOption").value,
    marks: parseInt(document.getElementById("marks").value, 10),
  };
  try {
    if (id) {
      await api("/examiner/questions/" + id, { method: "PUT", body: JSON.stringify(payload) });
      toast("Question updated");
    } else {
      await api(`/examiner/exams/${currentExamId}/questions`, { method: "POST", body: JSON.stringify(payload) });
      toast("Question added");
    }
    resetQuestionForm();
    loadQuestions();
  } catch (e) { toast(e.message, "error"); }
});

async function deleteQuestion(id) {
  if (!confirm("Delete this question?")) return;
  try {
    await api("/examiner/questions/" + id, { method: "DELETE" });
    toast("Question deleted");
    loadQuestions();
  } catch (e) { toast(e.message, "error"); }
}

// ---- Report modal ----
async function openReportModal(examId) {
  try {
    const report = await api(`/examiner/exams/${examId}/report`);
    document.getElementById("reportModalTitle").textContent = "Report — " + report.examTitle;
    document.getElementById("reportArea").innerHTML = `
      <div class="grid grid-3" style="margin-bottom:18px;">
        <div class="card stat-card"><div class="value">${report.totalAttempts}</div><div class="label">Attempts</div></div>
        <div class="card stat-card"><div class="value">${report.averageScore.toFixed(1)}</div><div class="label">Average / ${report.totalMarks}</div></div>
        <div class="card stat-card"><div class="value">${report.highestScore}</div><div class="label">Highest</div></div>
      </div>
      <table>
        <thead><tr><th>Student</th><th>Score</th><th>Submitted</th></tr></thead>
        <tbody>
          ${report.studentResults.map((r) => `
            <tr><td>${escapeHtml(r.studentFullName)}</td><td class="mono">${r.score}/${r.totalMarks}</td><td>${escapeHtml(r.submittedAt)}</td></tr>
          `).join("") || `<tr><td colspan="3"><div class="empty-state">No attempts submitted yet.</div></td></tr>`}
        </tbody>
      </table>
    `;
    document.getElementById("reportModal").style.display = "flex";
  } catch (e) { toast(e.message, "error"); }
}
function closeReportModal() { document.getElementById("reportModal").style.display = "none"; }

function escapeHtml(str) {
  if (str === null || str === undefined) return "";
  return String(str).replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
}

showView("overview");
