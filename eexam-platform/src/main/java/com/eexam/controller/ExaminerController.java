package com.eexam.controller;

import com.eexam.dto.ExamRequest;
import com.eexam.dto.ExaminerExamResponse;
import com.eexam.dto.PerformanceReportDto;
import com.eexam.dto.QuestionRequest;
import com.eexam.dto.QuestionResponse;
import com.eexam.model.User;
import com.eexam.repository.UserRepository;
import com.eexam.service.ExamService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/examiner")
public class ExaminerController {

    private final ExamService examService;
    private final UserRepository userRepository;

    public ExaminerController(ExamService examService, UserRepository userRepository) {
        this.examService = examService;
        this.userRepository = userRepository;
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }

    // ---- Exams ----

    @PostMapping("/exams")
    public ExaminerExamResponse createExam(@Valid @RequestBody ExamRequest request, Authentication authentication) {
        return ExaminerExamResponse.from(examService.createExam(request, currentUser(authentication)));
    }

    @GetMapping("/exams")
    public List<ExaminerExamResponse> myExams(Authentication authentication) {
        return examService.getExamsByExaminer(currentUser(authentication)).stream()
                .map(ExaminerExamResponse::from)
                .toList();
    }

    @GetMapping("/exams/{id}")
    public ExaminerExamResponse getExam(@PathVariable Long id, Authentication authentication) {
        return ExaminerExamResponse.from(examService.getExamOwnedBy(id, currentUser(authentication)));
    }

    @PutMapping("/exams/{id}")
    public ExaminerExamResponse updateExam(@PathVariable Long id, @Valid @RequestBody ExamRequest request, Authentication authentication) {
        return ExaminerExamResponse.from(examService.updateExam(id, request, currentUser(authentication)));
    }

    @DeleteMapping("/exams/{id}")
    public Map<String, String> deleteExam(@PathVariable Long id, Authentication authentication) {
        examService.deleteExam(id, currentUser(authentication));
        return Map.of("message", "Exam deleted successfully");
    }

    @PostMapping("/exams/{id}/activate")
    public Map<String, String> activate(@PathVariable Long id, Authentication authentication) {
        examService.setActive(id, true, currentUser(authentication));
        return Map.of("message", "Exam activated");
    }

    @PostMapping("/exams/{id}/deactivate")
    public Map<String, String> deactivate(@PathVariable Long id, Authentication authentication) {
        examService.setActive(id, false, currentUser(authentication));
        return Map.of("message", "Exam deactivated");
    }

    // ---- Questions ----

    @PostMapping("/exams/{id}/questions")
    public QuestionResponse addQuestion(@PathVariable Long id, @Valid @RequestBody QuestionRequest request, Authentication authentication) {
        return QuestionResponse.from(examService.addQuestion(id, request, currentUser(authentication)));
    }

    @GetMapping("/exams/{id}/questions")
    public List<QuestionResponse> getQuestions(@PathVariable Long id, Authentication authentication) {
        return examService.getQuestions(id, currentUser(authentication)).stream()
                .map(QuestionResponse::from)
                .toList();
    }

    @PutMapping("/questions/{questionId}")
    public QuestionResponse updateQuestion(@PathVariable Long questionId, @Valid @RequestBody QuestionRequest request, Authentication authentication) {
        return QuestionResponse.from(examService.updateQuestion(questionId, request, currentUser(authentication)));
    }

    @DeleteMapping("/questions/{questionId}")
    public Map<String, String> deleteQuestion(@PathVariable Long questionId, Authentication authentication) {
        examService.deleteQuestion(questionId, currentUser(authentication));
        return Map.of("message", "Question deleted successfully");
    }

    // ---- Reports ----

    @GetMapping("/exams/{id}/report")
    public PerformanceReportDto getReport(@PathVariable Long id, Authentication authentication) {
        return examService.getPerformanceReport(id, currentUser(authentication));
    }
}
