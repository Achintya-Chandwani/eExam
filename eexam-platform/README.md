# eExam — Role-Based Online Examination System

A full-stack examination platform with **Admin**, **Examiner**, and **Student** portals.

**Stack:** Java 17 · Spring Boot 3.2 · Spring Security (JWT) · Spring Data JPA · MySQL · Maven · HTML/CSS/vanilla JS

---

## 1. Prerequisites

- Java 17+ (`java -version`)
- Maven 3.8+ (`mvn -version`)
- MySQL 8.x running locally

## 2. Database setup

You don't need to create the schema manually — Hibernate will create tables automatically
(`spring.jpa.hibernate.ddl-auto=update`) and the datasource URL includes `createDatabaseIfNotExist=true`.

Just make sure a MySQL user/password matching `application.properties` can connect:

```sql
-- Only needed if you want to create the DB/user explicitly:
CREATE DATABASE IF NOT EXISTS eexam_db;
```

Edit `src/main/resources/application.properties` if your MySQL username/password differ from the defaults
(`root` / `root`):

```properties
spring.datasource.username=root
spring.datasource.password=root
```

## 3. Run the backend

```bash
cd eexam-platform
mvn spring-boot:run
```

The app starts on **http://localhost:8080**.

On first startup, a default admin account is created automatically:

```
username: admin
password: Admin@123
```

(Change this immediately after logging in, via Admin → Manage Users → Reset PW on your own account,
or by editing `app.admin.default-username` / `app.admin.default-password` in `application.properties`
before the first run.)

## 4. Using the app

Open **http://localhost:8080** in your browser — this is the login page, shared by all three roles.

**As Admin (`admin` / `Admin@123`):**
1. Go to *Manage Users* → *+ New User* to create Examiner and Student accounts.
2. *View Exams* shows every exam across all examiners (read-only oversight).
3. You can reset passwords, enable/disable, or delete accounts.

**As Examiner:**
1. *My Exams* → *+ New Exam* — set title, description, duration, and the open/close window.
2. Click *Questions* on an exam card to add multiple-choice questions (4 options + correct answer + marks).
3. *Activate* an exam once it has questions, so students can see and attempt it during its scheduled window.
4. Click *Report* on any exam to see attempt counts, average/high/low scores, and per-student results.

**As Student:**
1. *Available Exams* shows exams that are active and currently within their scheduled window.
2. *Start Exam* begins a timed attempt (countdown based on the exam's duration, capped by the exam's close time).
   Answers are submitted once, either manually or automatically when time runs out.
3. *Practice* shows questions from exams whose window has already closed, with the correct answer revealed.
4. Bookmark any practice question via the ☆ button; manage bookmarks under *Bookmarks*.
5. *My Results* lists all submitted attempts and scores.

## 5. Project structure

```
eexam-platform/
├── pom.xml
├── src/main/java/com/eexam/
│   ├── EexamApplication.java
│   ├── config/          # Security config, JWT filter wiring, exception handling, default admin seeding
│   ├── model/            # JPA entities: User, Exam, Question, ExamAttempt, Answer, Bookmark, Role
│   ├── repository/       # Spring Data JPA repositories
│   ├── security/         # JwtUtil, JwtAuthFilter, CustomUserDetailsService
│   ├── dto/               # Request/response payloads
│   ├── service/           # Business logic (UserService, ExamService, StudentExamService)
│   └── controller/       # REST controllers: Auth, Admin, Examiner, Student
└── src/main/resources/
    ├── application.properties
    └── static/            # Plain HTML/CSS/JS frontend (no build step needed)
        ├── index.html      # shared login page
        ├── admin/dashboard.html
        ├── examiner/dashboard.html
        ├── student/dashboard.html
        ├── css/style.css
        └── js/ (api.js, login.js, admin.js, examiner.js, student.js)
```

## 6. API overview

All endpoints are under `/api`. Auth uses a `Bearer <JWT>` header (except `/api/auth/login`).

| Role | Base path | Highlights |
|---|---|---|
| Public | `/api/auth/login` | Returns `{ token, username, fullName, role }` |
| Admin | `/api/admin/users`, `/api/admin/exams` | Full CRUD on accounts, password reset, enable/disable, read-only exam oversight |
| Examiner | `/api/examiner/exams`, `/api/examiner/exams/{id}/questions`, `/api/examiner/exams/{id}/report` | Exam & question CRUD, activate/deactivate, performance report |
| Student | `/api/student/exams`, `/api/student/exams/{id}/start`, `/api/student/exams/{id}/submit`, `/api/student/practice`, `/api/student/bookmarks`, `/api/student/results` | Attempt flow, practice mode, bookmarking |

## 7. Notes / next steps you may want

- Passwords are hashed with BCrypt; the password field is never returned in any API response.
- JWTs expire after 24h by default (`app.jwt.expiration-ms`) — adjust as needed.
- This scaffold uses simple MCQ questions (4 options, one correct answer). Extending to
  multi-select or free-text questions would need a schema change in `Question`/`Answer`.
- There's no email-sending; "reset password" simply lets the admin set a new password directly.
- CORS is wide open (`*`) for local development — tighten `SecurityConfig.corsConfigurationSource()`
  before deploying anywhere public.
