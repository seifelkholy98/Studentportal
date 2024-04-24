package com.example.projectSesc.UserService.service;

import com.example.projectSesc.EnrollmentService.domain.Enrollment;
import com.example.projectSesc.UserService.controller.StudentController;
import com.example.projectSesc.UserService.domain.Student;
import com.example.projectSesc.UserService.domain.UserRole;
import com.example.projectSesc.UserService.repository.StudentRepository;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;
    private static final Logger log = LoggerFactory.getLogger(StudentService.class);

    @Autowired
    public StudentService(StudentRepository studentRepository, PasswordEncoder passwordEncoder, RestTemplate restTemplate) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
        this.restTemplate = restTemplate;
    }

    public Student registerNewStudent(Student student) {
        if (studentRepository.findByUsername(student.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists.");
        }

        student.setPassword(passwordEncoder.encode(student.getPassword()));
        student.setRole(UserRole.ROLE_USER.name());
        return studentRepository.save(student);
    }

    public Student findStudentByUsername(String username) {
        return studentRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Student not found with username: " + username));
    }

    public Student updateStudentByUsername(String username, Student studentDetails) {
        return studentRepository.findByUsername(username)
                .map(student -> {
                    student.setEmail(studentDetails.getEmail());
                    student.setSurname(studentDetails.getSurname());
                    return studentRepository.save(student);
                }).orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    public void deleteStudentByUsername(String username) {
        Student student = studentRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        studentRepository.delete(student);
    }

    public boolean studentExists(Long studentId) {
        return studentRepository.findById(studentId).isPresent();
    }


    public List<String> checkOutstandingInvoices(String username) {
        Student student = studentRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        List<String> outstandingInvoices = new ArrayList<>();
        for (Enrollment enrollment : student.getEnrollments()) {
            String invoiceReference = enrollment.getInvoiceReference();
            String url = "http://localhost:8081/portal/invoice/";

            // Create form data
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("reference", invoiceReference);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Document doc = Jsoup.parse(response.getBody());
                String status = doc.select("dd.col-sm-3").get(5).text();
                if ("OUTSTANDING".equalsIgnoreCase(status)) {
                    outstandingInvoices.add(invoiceReference);
                }
            } else {
                System.err.println("Failed to fetch invoice status for invoice: " + invoiceReference);
            }
        }
        return outstandingInvoices;
    }

    public void notifyLibraryService(String studentCode) {
        String libraryUrl = "http://localhost/api/register";
        String jsonBody = "{\r\n    \"studentId\":\"" + studentCode + "\"\r\n}";

        try {
            Unirest.setTimeouts(0, 0);
            HttpResponse<String> response = Unirest.post(libraryUrl)
                    .header("Content-Type", "application/json")
                    .body(jsonBody)
                    .asString();

            // Logging the response status and body
            if (response.getStatus() == 200) {
                log.info("Response status: {}", response.getStatus());
                log.info("Response body: {}", response.getBody());
            } else {
                log.error("Failed to register student: HTTP {}", response.getStatus());
                log.error("Response body: {}", response.getBody());
            }
        } catch (UnirestException e) {
            log.error("Error during HTTP call to register student: ", e);
        }
    }
    public void notifyFinanceService(String studentCode) {
        String financeUrl = "http://localhost:8081/accounts";

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("studentId", studentCode);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(financeUrl, entity, String.class);

        if (response.getStatusCode() == HttpStatus.CREATED) {
            log.info("Account successfully created for student with code: {}", studentCode);
        } else {
            log.error("Failed to create account for student with code: {}. Status code: {}", studentCode, response.getStatusCode());
        }
    }




}
