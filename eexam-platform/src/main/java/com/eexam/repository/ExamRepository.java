package com.eexam.repository;

import com.eexam.model.Exam;
import com.eexam.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findByExaminer(User examiner);
    List<Exam> findByActiveTrue();
}
