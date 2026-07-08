package com.eexam.service;

import com.eexam.dto.SubmitExamRequest;
import com.eexam.model.*;
import com.eexam.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StudentExamService {

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final AnswerRepository answerRepository;
    private final BookmarkRepository bookmarkRepository;

    public StudentExamService(ExamRepository examRepository, QuestionRepository questionRepository,
                               ExamAttemptRepository examAttemptRepository, AnswerRepository answerRepository,
                               BookmarkRepository bookmarkRepository) {
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.answerRepository = answerRepository;
        this.bookmarkRepository = bookmarkRepository;
    }

    // Exams currently within their scheduled window and marked active
    public List<Exam> getAvailableExams() {
        LocalDateTime now = LocalDateTime.now();
        return examRepository.findByActiveTrue().stream()
                .filter(e -> !now.isBefore(e.getScheduledStart()) && !now.isAfter(e.getScheduledEnd()))
                .toList();
    }

    // Past exams (window closed) - used for "practice questions"
    public List<Exam> getPastExams() {
        LocalDateTime now = LocalDateTime.now();
        return examRepository.findAll().stream()
                .filter(e -> now.isAfter(e.getScheduledEnd()))
                .toList();
    }

    public Exam getExamById(Long id) {
        return examRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Exam not found with id: " + id));
    }

    public List<Question> getQuestionsForExam(Long examId) {
        return questionRepository.findByExamId(examId);
    }

    public ExamAttempt startAttempt(Long examId, User student) {
        Exam exam = getExamById(examId);
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(exam.getScheduledStart()) || now.isAfter(exam.getScheduledEnd()) || !exam.isActive()) {
            throw new IllegalStateException("This exam is not currently open for attempts");
        }

        return examAttemptRepository.findByExamAndStudent(exam, student)
                .orElseGet(() -> {
                    ExamAttempt attempt = new ExamAttempt();
                    attempt.setExam(exam);
                    attempt.setStudent(student);
                    return examAttemptRepository.save(attempt);
                });
    }

    public ExamAttempt submitAttempt(Long examId, User student, SubmitExamRequest request) {
        Exam exam = getExamById(examId);
        ExamAttempt attempt = examAttemptRepository.findByExamAndStudent(exam, student)
                .orElseThrow(() -> new IllegalStateException("You have not started this exam"));

        if (attempt.isSubmitted()) {
            throw new IllegalStateException("This exam has already been submitted");
        }

        Map<Long, Question> questionsById = questionRepository.findByExam(exam).stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        int score = 0;
        int total = exam.totalMarks();

        if (request.getAnswers() != null) {
            for (SubmitExamRequest.AnswerSubmission sub : request.getAnswers()) {
                Question question = questionsById.get(sub.getQuestionId());
                if (question == null) continue;

                boolean isCorrect = question.getCorrectOption().equalsIgnoreCase(sub.getSelectedOption());
                if (isCorrect) score += question.getMarks();

                Answer answer = new Answer();
                answer.setAttempt(attempt);
                answer.setQuestion(question);
                answer.setSelectedOption(sub.getSelectedOption().toUpperCase());
                answer.setCorrect(isCorrect);
                answerRepository.save(answer);
            }
        }

        attempt.setScore(score);
        attempt.setTotalMarks(total);
        attempt.setSubmitted(true);
        attempt.setSubmittedAt(LocalDateTime.now());
        return examAttemptRepository.save(attempt);
    }

    public List<ExamAttempt> getResultsForStudent(User student) {
        return examAttemptRepository.findByStudent(student).stream()
                .filter(ExamAttempt::isSubmitted)
                .toList();
    }

    // ---- Bookmarks ----

    public Bookmark bookmarkQuestion(User student, Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found: " + questionId));
        return bookmarkRepository.findByStudentAndQuestionId(student, questionId)
                .orElseGet(() -> {
                    Bookmark b = new Bookmark();
                    b.setStudent(student);
                    b.setQuestion(question);
                    return bookmarkRepository.save(b);
                });
    }

    public void removeBookmark(User student, Long questionId) {
        bookmarkRepository.deleteByStudentAndQuestionId(student, questionId);
    }

    public List<Bookmark> getBookmarks(User student) {
        return bookmarkRepository.findByStudent(student);
    }
}
