package com.example.projectSesc.UserService.service;

import com.example.projectSesc.UserService.domain.Student;
import com.example.projectSesc.UserService.domain.User;
import com.example.projectSesc.UserService.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final WebClient courseWebClient;
    private final WebClient financeWebClient;
    private final PasswordEncoder passwordEncoder; // Add PasswordEncoder

    @Autowired
    public StudentService(StudentRepository studentRepository, WebClient.Builder webClientBuilder, PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.courseWebClient = webClientBuilder.baseUrl("http://course-service").build();
        this.financeWebClient = webClientBuilder.baseUrl("http://finance-service").build();
        this.passwordEncoder = passwordEncoder;
    }

    public Student createStudent(Student student) {
        student.setPassword(passwordEncoder.encode(student.getPassword()));
        return studentRepository.save(student);
    }

    public Mono<Student> findStudentById(Long studentId) {
        return Mono.justOrEmpty(studentRepository.findById(studentId));
    }

    public Optional<Student> updateStudent(Long id, Student studentDetails) {
        return studentRepository.findById(id).map(student -> {
            student.setUsername(studentDetails.getUsername());
            student.setPassword(passwordEncoder.encode(studentDetails.getPassword()));
            student.setEmail(studentDetails.getEmail());
            student.setSurname(studentDetails.getSurname());
            student.setStudentId(studentDetails.getStudentId());
            return studentRepository.save(student);
        });
    }


    public boolean deleteStudent(Long studentId) {
        return studentRepository.findById(studentId)
                .map(student -> {
                    studentRepository.delete(student);
                    return true;
                }).orElse(false);
    }

    public List<Student> findAllStudents() {
        return studentRepository.findAll();
    }

    public Mono<Boolean> checkEligibilityToGraduate(Long studentId) {
        return financeWebClient.get()
                .uri("/finance/invoices/outstanding/{studentId}", studentId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .doOnError(error -> Mono.error(new RuntimeException("Error checking eligibility to graduate", error)));
    }

    public Mono<Void> enrollStudentInCourse(Long studentId, Long courseId) {
        return courseWebClient.post()
                .uri("/courses/enroll/{courseId}/{studentId}", courseId, studentId)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(error -> Mono.error(new RuntimeException("Error enrolling in course", error)));
    }

    public Flux<String> listStudentCourses(Long studentId) {
        return courseWebClient.get()
                .uri("/courses/student/{studentId}", studentId)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnError(error -> Flux.error(new RuntimeException("Error retrieving student courses", error)));
    }

    // Additional CRUD operations for Student management can be added here.
}
