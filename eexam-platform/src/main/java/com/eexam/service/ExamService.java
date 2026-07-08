package com.eexam.service;

import com.eexam.dto.ExamRequest;
import com.eexam.dto.PerformanceReportDto;
import com.eexam.dto.QuestionRequest;
import com.eexam.model.Exam;
import com.eexam.model.ExamAttempt;
import com.eexam.model.Question;
import com.eexam.model.User;
import com.eexam.repository.ExamAttemptRepository;
import com.eexam.repository.ExamRepository;
import com.eexam.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExamService {

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ExamAttemptRepository examAttemptRepository;

    public ExamService(ExamRepository examRepository, QuestionRepository questionRepository,
                        ExamAttemptRepository examAttemptRepository) {
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.examAttemptRepository = examAttemptRepository;
    }

    public Exam createExam(ExamRequest request, User examiner) {
        Exam exam = new Exam();
        exam.setTitle(request.getTitle());
        exam.setDescription(request.getDescription());
        exam.setDurationMinutes(request.getDurationMinutes());
        exam.setScheduledStart(request.getScheduledStart());
        exam.setScheduledEnd(request.getScheduledEnd());
        exam.setExaminer(examiner);
        return examRepository.save(exam);
    }

    public List<Exam> getExamsByExaminer(User examiner) {
        return examRepository.findByExaminer(examiner);
    }

    public Exam getExamOwnedBy(Long examId, User examiner) {
        Exam exam = getExamById(examId);
        if (!exam.getExaminer().getId().equals(examiner.getId())) {
            throw new SecurityException("You do not own this exam");
        }
        return exam;
    }

    public Exam getExamById(Long id) {
        return examRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Exam not found with id: " + id));
    }

    public Exam updateExam(Long id, ExamRequest request, User examiner) {
        Exam exam = getExamOwnedBy(id, examiner);
        exam.setTitle(request.getTitle());
        exam.setDescription(request.getDescription());
        exam.setDurationMinutes(request.getDurationMinutes());
        exam.setScheduledStart(request.getScheduledStart());
        exam.setScheduledEnd(request.getScheduledEnd());
        return examRepository.save(exam);
    }

    public void deleteExam(Long id, User examiner) {
        Exam exam = getExamOwnedBy(id, examiner);
        examRepository.delete(exam);
    }

    public void setActive(Long id, boolean active, User examiner) {
        Exam exam = getExamOwnedBy(id, examiner);
        exam.setActive(active);
        examRepository.save(exam);
    }

    // ---- Questions ----

    public Question addQuestion(Long examId, QuestionRequest request, User examiner) {
        Exam exam = getExamOwnedBy(examId, examiner);
        Question question = new Question();
        question.setExam(exam);
        applyQuestionRequest(question, request);
        return questionRepository.save(question);
    }

    public List<Question> getQuestions(Long examId, User examiner) {
        Exam exam = getExamOwnedBy(examId, examiner);
        return questionRepository.findByExam(exam);
    }

    public Question updateQuestion(Long questionId, QuestionRequest request, User examiner) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found: " + questionId));
        if (!question.getExam().getExaminer().getId().equals(examiner.getId())) {
            throw new SecurityException("You do not own this question's exam");
        }
        applyQuestionRequest(question, request);
        return questionRepository.save(question);
    }

    public void deleteQuestion(Long questionId, User examiner) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found: " + questionId));
        if (!question.getExam().getExaminer().getId().equals(examiner.getId())) {
            throw new SecurityException("You do not own this question's exam");
        }
        questionRepository.delete(question);
    }

    private void applyQuestionRequest(Question question, QuestionRequest request) {
        question.setQuestionText(request.getQuestionText());
        question.setOptionA(request.getOptionA());
        question.setOptionB(request.getOptionB());
        question.setOptionC(request.getOptionC());
        question.setOptionD(request.getOptionD());
        question.setCorrectOption(request.getCorrectOption().toUpperCase());
        question.setMarks(request.getMarks() == null ? 1 : request.getMarks());
    }

    // ---- Reports ----

    public PerformanceReportDto getPerformanceReport(Long examId, User examiner) {
        Exam exam = getExamOwnedBy(examId, examiner);
        List<ExamAttempt> attempts = examAttemptRepository.findByExamAndSubmittedTrue(exam);

        int totalMarks = exam.totalMarks();
        double avg = attempts.stream().mapToInt(a -> a.getScore() == null ? 0 : a.getScore()).average().orElse(0);
        double max = attempts.stream().mapToInt(a -> a.getScore() == null ? 0 : a.getScore()).max().orElse(0);
        double min = attempts.isEmpty() ? 0 : attempts.stream().mapToInt(a -> a.getScore() == null ? 0 : a.getScore()).min().orElse(0);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        List<PerformanceReportDto.StudentResult> results = attempts.stream()
                .map(a -> new PerformanceReportDto.StudentResult(
                        a.getStudent().getUsername(),
                        a.getStudent().getFullName(),
                        a.getScore(),
                        a.getTotalMarks(),
                        a.getSubmittedAt() == null ? "-" : a.getSubmittedAt().format(fmt)
                ))
                .toList();

        return new PerformanceReportDto(exam.getId(), exam.getTitle(), totalMarks, attempts.size(), avg, max, min, results);
    }
}
