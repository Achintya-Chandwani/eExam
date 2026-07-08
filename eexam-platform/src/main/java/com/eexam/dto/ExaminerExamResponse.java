package com.eexam.dto;

import com.eexam.model.Exam;

import java.time.LocalDateTime;
import java.util.List;

public class ExaminerExamResponse {

    private Long id;
    private String title;
    private String description;
    private Integer durationMinutes;
    private LocalDateTime scheduledStart;
    private LocalDateTime scheduledEnd;
    private boolean active;
    private LocalDateTime createdAt;
    private List<QuestionResponse> questions;

    public static ExaminerExamResponse from(Exam exam) {
        ExaminerExamResponse dto = new ExaminerExamResponse();
        dto.id = exam.getId();
        dto.title = exam.getTitle();
        dto.description = exam.getDescription();
        dto.durationMinutes = exam.getDurationMinutes();
        dto.scheduledStart = exam.getScheduledStart();
        dto.scheduledEnd = exam.getScheduledEnd();
        dto.active = exam.isActive();
        dto.createdAt = exam.getCreatedAt();
        dto.questions = exam.getQuestions().stream().map(QuestionResponse::from).toList();
        return dto;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public LocalDateTime getScheduledStart() { return scheduledStart; }
    public LocalDateTime getScheduledEnd() { return scheduledEnd; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<QuestionResponse> getQuestions() { return questions; }
}
