package com.example.projectSesc.UserService.controller;

import com.example.projectSesc.UserService.domain.Student;
import com.example.projectSesc.UserService.domain.User;
import com.example.projectSesc.UserService.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    @Autowired
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    // Create a new student profile
    @PostMapping("/register")
    public ResponseEntity<Student> registerUser(@RequestBody Student student) {
        Student registeredUser = studentService.createStudent(student);
        return ResponseEntity.ok(registeredUser);
    }

    // Retrieve a student by ID
    @GetMapping("/{studentId}")
    public Mono<ResponseEntity<Student>> getStudentById(@PathVariable Long studentId) {
        return studentService.findStudentById(studentId)
                .map(student -> ResponseEntity.ok(student))  // Directly mapping Student to ResponseEntity<Student>
                .defaultIfEmpty(ResponseEntity.notFound().build());  // Handling case where Student is not found
    }


    // Update a student profile
    @PutMapping("/{studentId}")
    public ResponseEntity<Student> updateStudent(@PathVariable Long studentId, @RequestBody Student studentDetails) {
        return studentService.updateStudent(studentId, studentDetails)
                .map(updatedStudent -> ResponseEntity.ok(updatedStudent))
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete a student profile
    @DeleteMapping("/{studentId}")
    public Mono<ResponseEntity<Void>> deleteStudent(@PathVariable Long studentId) {
        if (studentService.deleteStudent(studentId)) {
            return Mono.just(ResponseEntity.ok().<Void>build());
        } else {
            return Mono.just(ResponseEntity.notFound().build());
        }
    }

    // List all student profiles
    @GetMapping
    public Mono<List<Student>> getAllStudents() {
        return Mono.just(studentService.findAllStudents());
    }

    @GetMapping("/{studentId}/eligibility-to-graduate")
    public Mono<ResponseEntity<String>> checkEligibilityToGraduate(@PathVariable Long studentId) {
        return studentService.checkEligibilityToGraduate(studentId)
                .map(isEligible -> {
                    if (Boolean.TRUE.equals(isEligible)) {
                        return ResponseEntity.ok("Student is eligible to graduate.");
                    } else {
                        return ResponseEntity.ok("Student has outstanding invoices and is not eligible to graduate.");
                    }
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // Additional methods and logic as needed
}
