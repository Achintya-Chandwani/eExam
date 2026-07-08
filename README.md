<div align="center">

# 🎓 eExam Platform

### A Modern Role-Based Online Examination System

[![Live Demo](https://img.shields.io/badge/🚀%20Live%20Demo-eexam--s5sm.onrender.com-6366f1?style=for-the-badge)](https://eexam-s5sm.onrender.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)](LICENSE)

<br/>

> A full-stack examination platform with **Admin**, **Examiner**, and **Student** portals —  
> built with Spring Boot REST API + vanilla HTML/CSS/JS frontend served from the same JAR.

<br/>

**[🌐 Try it Live](https://eexam-s5sm.onrender.com)** · **[📖 API Docs](#-api-reference)** · **[🐳 Docker](#-run-with-docker)** · **[☁️ Deploy](#️-deploy-to-render)**

</div>

---

## ✨ Features

<table>
<tr>
<td width="33%">

### 👑 Admin
- Create & manage user accounts (Examiner / Student)
- Reset passwords, enable/disable accounts
- View all exams across all examiners
- Delete users

</td>
<td width="33%">

### 📝 Examiner
- Create & manage exams (title, duration, schedule window)
- Add MCQ questions with marks
- Activate / deactivate exams
- View detailed performance reports

</td>
<td width="33%">

### 🎓 Student
- Attempt live exams with real-time countdown timer
- Auto-submit when time runs out
- Practice from past exams with answers revealed
- Bookmark questions & view results

</td>
</tr>
</table>

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **Backend** | Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA |
| **Auth** | JWT (HS256 via JJWT 0.11.5) |
| **Database** | PostgreSQL 16 |
| **Frontend** | Vanilla HTML5 · CSS3 · JavaScript (no framework, no build step) |
| **Build** | Maven 3.9 |
| **Deployment** | Docker (multi-stage) · Render |

---

## 🗂️ Project Structure

```
eexam-platform/
├── 📄 pom.xml
└── src/main/
    ├── java/com/eexam/
    │   ├── 🚀 EexamApplication.java
    │   ├── config/              # SecurityConfig, DataInitializer, GlobalExceptionHandler
    │   ├── controller/          # AuthController, AdminController, ExaminerController, StudentController
    │   ├── dto/                 # Request & Response payload classes
    │   ├── model/               # JPA Entities: User, Exam, Question, ExamAttempt, Answer, Bookmark
    │   ├── repository/          # Spring Data JPA interfaces
    │   ├── security/            # JwtUtil, JwtAuthFilter, CustomUserDetailsService
    │   └── service/             # UserService, ExamService, StudentExamService
    └── resources/
        ├── application.properties
        └── static/              # Frontend (served directly by Spring Boot)
            ├── index.html       ← Login page
            ├── admin/dashboard.html
            ├── examiner/dashboard.html
            ├── student/dashboard.html
            ├── css/style.css
            └── js/              # api.js, login.js, admin.js, examiner.js, student.js
```

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 16 running locally

### 1. Clone the repository

```bash
git clone https://github.com/Achintya-Chandwani/eExam.git
cd eExam
```

### 2. Configure the database

Create a PostgreSQL database:

```sql
CREATE DATABASE eexam_db;
```

Set environment variables (or update `application.properties`):

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/eexam_db
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=your_password
export APP_JWT_SECRET=your_super_secret_key
```

### 3. Run the application

```bash
cd eexam-platform
mvn spring-boot:run
```

Open **http://localhost:8080** in your browser.

### 4. Default Admin Credentials

On first startup, an admin account is auto-created:

```
Username : admin
Password : Admin@123
```

> ⚠️ Change this password immediately after first login!

---

## 🐳 Run with Docker

The easiest way to run locally with zero setup:

```bash
# Clone the repo
git clone https://github.com/Achintya-Chandwani/eExam.git
cd eExam

# Start PostgreSQL + App together
docker-compose up --build
```

Open **http://localhost:8080** — that's it! 🎉

PostgreSQL data is persisted in a Docker volume (`eexam-postgres-data`).

---

## ☁️ Deploy to Render

This project is live at → **https://eexam-s5sm.onrender.com**

### Steps to deploy your own instance:

1. **Fork** this repo to your GitHub account
2. Go to [render.com](https://render.com) → **New → Web Service**
3. Connect your GitHub repo
4. Set **Environment** to `Docker` (Render auto-detects the `Dockerfile`)
5. Add a **PostgreSQL** database from Render dashboard
6. Set these environment variables:

| Variable | Value |
|----------|-------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://<host>:5432/<db>` |
| `SPRING_DATASOURCE_USERNAME` | `<pg-username>` |
| `SPRING_DATASOURCE_PASSWORD` | `<pg-password>` |
| `APP_JWT_SECRET` | Any random string (32+ chars recommended) |
| `APP_ADMIN_DEFAULT_USERNAME` | `admin` |
| `APP_ADMIN_DEFAULT_PASSWORD` | `YourSecurePassword` |
| `SERVER_PORT` | `8080` |

7. Click **Deploy** — Render builds the Docker image and starts the app 🚀

---

## 📡 API Reference

All endpoints are under `/api`. Protected routes require `Authorization: Bearer <token>`.

### Auth

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/auth/login` | Login → returns `{ token, username, fullName, role }` |

### Admin Routes `/api/admin/**`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/admin/users` | List all users |
| `POST` | `/api/admin/users` | Create a new user |
| `PUT` | `/api/admin/users/{id}` | Update user |
| `DELETE` | `/api/admin/users/{id}` | Delete user |
| `POST` | `/api/admin/users/{id}/reset-password` | Reset password |
| `PATCH` | `/api/admin/users/{id}/toggle` | Enable/disable account |
| `GET` | `/api/admin/exams` | View all exams (read-only) |

### Examiner Routes `/api/examiner/**`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/examiner/exams` | List my exams |
| `POST` | `/api/examiner/exams` | Create exam |
| `PUT` | `/api/examiner/exams/{id}` | Update exam |
| `DELETE` | `/api/examiner/exams/{id}` | Delete exam |
| `PATCH` | `/api/examiner/exams/{id}/activate` | Activate/deactivate |
| `GET/POST/PUT/DELETE` | `/api/examiner/exams/{id}/questions` | Manage questions |
| `GET` | `/api/examiner/exams/{id}/report` | Performance report |

### Student Routes `/api/student/**`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/student/exams` | Available exams |
| `POST` | `/api/student/exams/{id}/start` | Start exam attempt |
| `POST` | `/api/student/exams/{id}/submit` | Submit answers |
| `GET` | `/api/student/results` | My results |
| `GET` | `/api/student/practice` | Practice questions |
| `GET/POST/DELETE` | `/api/student/bookmarks` | Manage bookmarks |

---

## 🔐 Security

- **JWT Authentication** — stateless, token expires in 24h (configurable)
- **Role-Based Access Control** — `ADMIN`, `EXAMINER`, `STUDENT` roles enforced at API level
- **Passwords** — stored as plain text (suitable for demo; use BCrypt for production)
- **CORS** — open for development, configure in `SecurityConfig` before production use

---

## 🤝 Contributing

Pull requests are welcome! For major changes, please open an issue first.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License.

---

<div align="center">

Made with ❤️ by **Achintya Chandwani**

[![GitHub](https://img.shields.io/badge/GitHub-Achintya--Chandwani-181717?style=flat-square&logo=github)](https://github.com/Achintya-Chandwani)

⭐ **Star this repo if you found it helpful!**

</div>
