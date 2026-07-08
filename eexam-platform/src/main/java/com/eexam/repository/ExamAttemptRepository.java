package com.eexam.repository;

import com.eexam.model.Exam;
import com.eexam.model.ExamAttempt;
import com.eexam.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamAttemptRepository extends JpaRepository<ExamAttempt, Long> {
    Optional<ExamAttempt> findByExamAndStudent(Exam exam, User student);
    List<ExamAttempt> findByStudent(User student);
    List<ExamAttempt> findByExam(Exam exam);
    List<ExamAttempt> findByExamAndSubmittedTrue(Exam exam);
}
