package com.eexam.controller;

import com.eexam.dto.*;
import com.eexam.model.Question;
import com.eexam.model.User;
import com.eexam.repository.UserRepository;
import com.eexam.service.StudentExamService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final StudentExamService studentExamService;
    private final UserRepository userRepository;

    public StudentController(StudentExamService studentExamService, UserRepository userRepository) {
        this.studentExamService = studentExamService;
        this.userRepository = userRepository;
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }

    // ---- Exams ----

    @GetMapping("/exams")
    public List<ExamListResponse> availableExams() {
        return studentExamService.getAvailableExams().stream()
                .map(ExamListResponse::from)
                .toList();
    }

    @PostMapping("/exams/{id}/start")
    public ExamAttemptResponse startExam(@PathVariable Long id, Authentication authentication) {
        return ExamAttemptResponse.from(studentExamService.startAttempt(id, currentUser(authentication)));
    }

    @GetMapping("/exams/{id}/questions")
    public List<StudentQuestionView> examQuestions(@PathVariable Long id) {
        return studentExamService.getQuestionsForExam(id).stream()
                .map(StudentQuestionView::from)
                .toList();
    }

    @PostMapping("/exams/{id}/submit")
    public ExamAttemptResponse submitExam(@PathVariable Long id, @RequestBody SubmitExamRequest request, Authentication authentication) {
        return ExamAttemptResponse.from(studentExamService.submitAttempt(id, currentUser(authentication), request));
    }

    @GetMapping("/results")
    public List<ExamAttemptResponse> myResults(Authentication authentication) {
        return studentExamService.getResultsForStudent(currentUser(authentication)).stream()
                .map(ExamAttemptResponse::from)
                .toList();
    }

    // ---- Practice ----

    @GetMapping("/practice")
    public List<PracticeQuestionView> practiceQuestions() {
        List<Question> questions = studentExamService.getPastExams().stream()
                .flatMap(exam -> studentExamService.getQuestionsForExam(exam.getId()).stream())
                .toList();
        return questions.stream().map(PracticeQuestionView::from).toList();
    }

    // ---- Bookmarks ----

    @PostMapping("/questions/{id}/bookmark")
    public Map<String, String> bookmark(@PathVariable Long id, Authentication authentication) {
        studentExamService.bookmarkQuestion(currentUser(authentication), id);
        return Map.of("message", "Question bookmarked");
    }

    @DeleteMapping("/questions/{id}/bookmark")
    public Map<String, String> removeBookmark(@PathVariable Long id, Authentication authentication) {
        studentExamService.removeBookmark(currentUser(authentication), id);
        return Map.of("message", "Bookmark removed");
    }

    @GetMapping("/bookmarks")
    public List<BookmarkView> bookmarks(Authentication authentication) {
        return studentExamService.getBookmarks(currentUser(authentication)).stream()
                .map(BookmarkView::from)
                .toList();
    }
}
