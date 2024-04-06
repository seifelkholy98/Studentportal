package com.example.projectSesc.UserService.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long studentId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password; // Ensure this is encrypted in practice

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String surname;



    public Student() {
    }

    public Student(Long studentId,String username , String password , String email , String surname  ) {
        this.studentId = studentId ;
        this.username = username;
        this.password = password;
        this.email = email;
        this.surname  = surname ;
    }

    // Getters and Setters
    public Long getId() {
        return studentId;
    }

    public void setId(Long studentId) {
        this.studentId = studentId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }



    // You might want to include methods for managing the student's course enrollments,
    // depending on how you decide to model the relationship between students and courses.
}
