package com.example.projectSesc.EnrollmentService.controller;

import com.example.projectSesc.CourseService.domain.Course;
import com.example.projectSesc.CourseService.repository.CourseRepository;
import com.example.projectSesc.EnrollmentService.domain.Enrollment;
import com.example.projectSesc.EnrollmentService.service.EnrollmentService;
import com.example.projectSesc.UserService.domain.Student;
import com.example.projectSesc.UserService.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final StudentRepository StudentRepository;
    private CourseRepository courseRepository;
    @Autowired
    public EnrollmentController(EnrollmentService enrollmentService, StudentRepository studentRepository, CourseRepository courseRepository) {
        this.enrollmentService = enrollmentService;
        this.StudentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    @PostMapping
    public ResponseEntity<Enrollment> enrollStudent(@RequestParam Long courseId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Student student = StudentRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("Student not found"));;
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new RuntimeException("Course not found"));

        Enrollment enrollment = enrollmentService.enrollStudent(student, course);
        StudentRepository.save(student);
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollment);
    }



    @GetMapping
    public ResponseEntity<List<Enrollment>> listAllEnrollments() {
        List<Enrollment> enrollments = enrollmentService.findAllEnrollments();
        return ResponseEntity.ok(enrollments);
    }

    @DeleteMapping("/{enrollmentId}")
    public ResponseEntity<Void> deleteEnrollment(@PathVariable Long enrollmentId) {
        enrollmentService.deleteEnrollment(enrollmentId);
        return ResponseEntity.noContent().build();
    }
}
