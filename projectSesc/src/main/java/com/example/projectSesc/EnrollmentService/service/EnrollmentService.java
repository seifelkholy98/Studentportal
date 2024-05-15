package com.example.projectSesc.EnrollmentService.service;

import com.example.projectSesc.CourseService.domain.Course;
import com.example.projectSesc.EnrollmentService.domain.Enrollment;
import com.example.projectSesc.EnrollmentService.repository.EnrollmentRepository;
import com.example.projectSesc.UserService.domain.Student;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final RestTemplate restTemplate;
    private String referenceNumber ;
    private static final Logger log = LoggerFactory.getLogger(EnrollmentService.class);

    @Autowired
    public EnrollmentService(EnrollmentRepository enrollmentRepository, RestTemplate restTemplate) {
        this.enrollmentRepository = enrollmentRepository;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public Enrollment enrollStudent(Student student, Course course) {
        // Verify student and course existence
        boolean studentExists = verifyStudentExists(student.getUsername());
        if (!studentExists) {
            throw new IllegalStateException("Student does not exist");
        }

        boolean courseExists = verifyCourseExists(course.getCourseId());
        if (!courseExists) {
            throw new IllegalStateException("Course does not exist");
        }
        if (enrollmentRepository.existsByStudentAndCourse(student, course)) {
            throw new IllegalStateException("Enrollment already exists for this student and course");
        }
        // i want to create an invoice here
        referenceNumber = createInvoice(student.getStudentCode(),course.getPrice());

        return createEnrollment(student, course,referenceNumber);
    }

    public Enrollment createEnrollment(Student student, Course course, String referenceNumber) {
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setEnrollmentDate(LocalDateTime.now());
        enrollment.setInvoiceReference(referenceNumber);
        student.getEnrollments().add(enrollment);
        return enrollmentRepository.save(enrollment);
    }

    public String createInvoice(String studentCode, BigDecimal price) {
        String url = "http://localhost:8081/invoices";

        LocalDate dueDate = LocalDate.now().plusMonths(1);  // Add one month to the current date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("amount", price.doubleValue());
        requestBody.put("type", "TUITION_FEES");
        requestBody.put("dueDate", formatter.format(dueDate));

        Map<String, String> accountDetails = new HashMap<>();
        accountDetails.put("studentId", studentCode);
        requestBody.put("account", accountDetails);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            try {
                Map<String, Object> responseMap = new HashMap<>();
                responseMap = new ObjectMapper().readValue(response.getBody(), Map.class);
                String reference = (String) responseMap.get("reference");
                System.out.println("Invoice successfully created for student: " + studentCode + " with reference: " + reference);
                return reference;
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            System.err.println("Failed to create invoice for student: " + studentCode + " Response: " + response.getBody());
            return null;
        }
    }

    private boolean verifyStudentExists(String username) {
        log.debug("Checking if student exists: {}", username);
        Boolean studentExists;
        try {
            studentExists = restTemplate.getForObject("http://localhost:8080/api/students/exists/"+username, Boolean.class, username);
            log.debug("Student existence check for '{}' returned: {}", username, studentExists);
        } catch (Exception e) {
            log.error("Error when checking if student exists: {}", username, e);
            return false;
        }
        return studentExists != null && studentExists;
    }

    private boolean verifyCourseExists(Long courseId) {
        try {
            String url = "http://localhost:8080/api/courses/exists/{courseId}";
            Boolean courseExists = restTemplate.getForObject(url, Boolean.class, courseId);
            return courseExists != null && courseExists;
        } catch (RestClientException e) {
            throw new RestClientException(e.getMessage());
        }

    }



    public void deleteEnrollment(Long enrollmentId) {
        enrollmentRepository.deleteById(enrollmentId);
    }

    public List<Enrollment> findAllEnrollments() {
        return enrollmentRepository.findAll();
    }
}
