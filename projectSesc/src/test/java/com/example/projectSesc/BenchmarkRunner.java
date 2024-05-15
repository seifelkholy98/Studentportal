package com.example.projectSesc;

import com.example.projectSesc.EnrollmentService.service.EnrollmentService;
import com.example.projectSesc.UserService.domain.Student;
import com.example.projectSesc.UserService.service.StudentService;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

@State(Scope.Thread)
public class BenchmarkRunner {

    private StudentService studentService;
    private EnrollmentService enrollmentService;

    private Student registeredStudent;

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
        enrollmentService = context.getBean(EnrollmentService.class);

        String presistentUsername = "john_doe_" + UUID.randomUUID().toString();
        registeredStudent = new Student(1L, presistentUsername, "password123", "john.doe@example.com", "Doe", "ROLE_USER", new ArrayList<>(), "c1234567");
        studentService.registerNewStudent(registeredStudent);
    }

    @Benchmark
    public void benchMarkRegisterStudent() {
        String username = "john_doe_" + UUID.randomUUID().toString();
        Student student = new Student(1L, username, "password123", "john.doe@example.com", "Doe", "ROLE_USER", new ArrayList<>(), "c1234567");
        studentService.registerNewStudent(student);
    }



    @Benchmark
    public void benchMarkUpdatingStudentDetails(){
        Student updatedDetails = new Student(registeredStudent.getStudentId(), registeredStudent.getUsername(), "newpassword123", "updated@example.com", "UpdatedSurname", "ROLE_USER", new ArrayList<>(), "c1234567");
        studentService.updateStudentByUsername(registeredStudent.getUsername(), updatedDetails);
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        studentService.deleteStudentByUsername(registeredStudent.getUsername());
        context.close();
    }

    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(BenchmarkEnrollment.class.getSimpleName())
                .include(BenchmarkRunner.class.getSimpleName())
                .include(BenchmarkCourses.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
