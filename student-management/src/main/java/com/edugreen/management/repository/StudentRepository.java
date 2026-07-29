package com.edugreen.management.repository;

import com.edugreen.management.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    
    // Custom database lookup query based on Spring Data naming conventions
    Optional<Student> findByUsername(String username);
    
    // Custom database lookup query to fetch individual profiles by Student ID
    Optional<Student> findByStudentId(String studentId);
    java.util.Optional<Student> findByEmail(String email);
}