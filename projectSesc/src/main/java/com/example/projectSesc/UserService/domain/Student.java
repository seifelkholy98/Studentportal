package com.example.projectSesc.UserService.domain;

import com.example.projectSesc.CourseService.domain.Course;
import com.example.projectSesc.EnrollmentService.domain.Enrollment;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Random;
import java.util.Set;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long studentId;

    @Column(nullable = false, unique = true)
    @NotEmpty(message = "Username cannot be empty")
    protected String username;

    @Column(nullable = false)
    @NotEmpty(message = "Password cannot be empty")
    private String password;

    @Column(nullable = false)
    @NotEmpty(message = "Email cannot be empty")
    protected String email;

    @Column(nullable = false)
    protected String surname;

    @Column(name = "role", nullable = false, columnDefinition = "VARCHAR(255) DEFAULT 'ROLE_USER'")
    private String role;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Enrollment> enrollments;

    @Column(nullable = false, unique = true)
    private String studentCode;

    @PrePersist
    private void initializeStudentCode() {
        this.studentCode = generateStudentCode();
    }

    private String generateStudentCode() {
        Random random = new Random();
        return "c" + String.format("%07d", random.nextInt(10000000));
    }
}
