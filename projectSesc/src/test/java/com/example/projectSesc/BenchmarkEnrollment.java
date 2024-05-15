package com.example.projectSesc;

import com.example.projectSesc.CourseService.domain.Course;
import com.example.projectSesc.CourseService.service.CourseService;
import com.example.projectSesc.EnrollmentService.service.EnrollmentService;
import com.example.projectSesc.UserService.domain.Student;
import com.example.projectSesc.UserService.service.StudentService;
import org.openjdk.jmh.annotations.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@State(Scope.Thread)
public class BenchmarkEnrollment {

    private StudentService studentService;
    private EnrollmentService enrollmentService;
    private CourseService courseService;
    private Student registeredStudent;
    private Course course;
    private  AnnotationConfigApplicationContext context ;

    @Setup
    public void initialize() {
        context = new AnnotationConfigApplicationContext();
        try {
            // Load the custom property file
            ConfigurableEnvironment env = context.getEnvironment();
            MutablePropertySources sources = env.getPropertySources();
            PropertiesPropertySource benchmarkProperties = new PropertiesPropertySource(
                    "benchmarkProperties",
                    PropertiesLoaderUtils.loadProperties(new EncodedResource(new ClassPathResource("application-benchmark.properties")))
            );
            sources.addFirst(benchmarkProperties);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load benchmark configuration", e);
        }

        context.scan("com.example.projectSesc");
        context.refresh();
        studentService = context.getBean(StudentService.class);
        courseService = context.getBean(CourseService.class);
        enrollmentService = context.getBean(EnrollmentService.class);
    }
    @Setup(Level.Invocation)
    public void setupStudentAndCourse() {
        // Generate a unique username for every invocation
        String username = "john_doe_" + UUID.randomUUID().toString();
        String courseName = "Intro to Java " + UUID.randomUUID().toString();

        registeredStudent = new Student(1L, username, "password123", "john.doe@example.com", "Doe", "ROLE_USER", new ArrayList<>(), "c1234567");
        course = new Course(null, courseName, "Comprehensive Java course", new BigDecimal("20.0"));

        course = courseService.createCourse(course);
        studentService.registerNewStudent(registeredStudent);
    }

    @Benchmark
    public void benchMarkEnrollStudent() {
        enrollmentService.createEnrollment(registeredStudent,course,null);
    }


}
