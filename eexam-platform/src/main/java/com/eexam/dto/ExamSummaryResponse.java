package com.eexam.dto;

import com.eexam.model.Exam;

import java.time.LocalDateTime;

public class ExamSummaryResponse {

    private Long id;
    private String title;
    private String description;
    private Integer durationMinutes;
    private LocalDateTime scheduledStart;
    private LocalDateTime scheduledEnd;
    private boolean active;
    private LocalDateTime createdAt;
    private int totalMarks;
    private int questionCount;

    // examiner info, flattened (no proxy/lazy fields at all)
    private Long examinerId;
    private String examinerName;
    private String examinerUsername;

    public static ExamSummaryResponse from(Exam exam) {
        ExamSummaryResponse dto = new ExamSummaryResponse();
        dto.id = exam.getId();
        dto.title = exam.getTitle();
        dto.description = exam.getDescription();
        dto.durationMinutes = exam.getDurationMinutes();
        dto.scheduledStart = exam.getScheduledStart();
        dto.scheduledEnd = exam.getScheduledEnd();
        dto.active = exam.isActive();
        dto.createdAt = exam.getCreatedAt();
        dto.totalMarks = exam.totalMarks();
        dto.questionCount = exam.getQuestions().size();

        dto.examinerId = exam.getExaminer().getId();
        dto.examinerName = exam.getExaminer().getFullName();
        dto.examinerUsername = exam.getExaminer().getUsername();
        return dto;
    }

    // --- getters (needed for Jackson to serialize) ---
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public LocalDateTime getScheduledStart() { return scheduledStart; }
    public LocalDateTime getScheduledEnd() { return scheduledEnd; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public int getTotalMarks() { return totalMarks; }
    public int getQuestionCount() { return questionCount; }
    public Long getExaminerId() { return examinerId; }
    public String getExaminerName() { return examinerName; }
    public String getExaminerUsername() { return examinerUsername; }
}
