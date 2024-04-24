package com.example.projectSesc.EnrollmentService.repository;

import com.example.projectSesc.CourseService.domain.Course;
import com.example.projectSesc.EnrollmentService.domain.Enrollment;
import com.example.projectSesc.UserService.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByStudentAndCourse(Student student, Course course);

}
