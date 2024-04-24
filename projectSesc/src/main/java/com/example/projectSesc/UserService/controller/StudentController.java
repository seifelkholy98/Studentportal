package com.example.projectSesc.UserService.controller;

import com.example.projectSesc.UserService.domain.Student;
import com.example.projectSesc.UserService.service.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private static final Logger log = LoggerFactory.getLogger(StudentController.class);

    @Autowired
    public StudentController( StudentService studentService, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
        this.studentService = studentService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }


    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("student") Student student, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        String rawPassword = student.getPassword();
        Student registeredStudent = studentService.registerNewStudent(student);
        try {

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(student.getUsername(), rawPassword)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            studentService.notifyFinanceService(registeredStudent.getStudentCode());
            studentService.notifyLibraryService(registeredStudent.getStudentCode());

            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

            redirectAttributes.addFlashAttribute("success", "Registration successful. Welcome!");
            return "redirect:/courses";
        } catch (AuthenticationException e) {
            redirectAttributes.addFlashAttribute("error", "Authentication failed: " + e.getMessage());
            return "redirect:/register";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Registration failed: " + e.getMessage());
            return "redirect:/register";
        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam("username") String username, @RequestParam("password") String password) {
        try {
            log.debug("Attempting to authenticate user: {}", username);
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("Authentication successful for user: {}", username);

            Student student = studentService.findStudentByUsername(username);
            return ResponseEntity.ok(student);
        } catch (AuthenticationException e) {
            log.error("Authentication failed for user: {}", username, e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed: " + e.getMessage());
        }
    }



    @GetMapping("/profile")
    public ResponseEntity<?> getAuthenticatedStudent() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Student student = studentService.findStudentByUsername(username);
        return student != null ? ResponseEntity.ok(student) : ResponseEntity.notFound().build();
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateAuthenticatedStudent(@Valid @RequestBody Student studentDetails) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Student updatedStudent = studentService.updateStudentByUsername(username, studentDetails);
        return ResponseEntity.ok(updatedStudent);
    }

    @DeleteMapping("/profile")
    public ResponseEntity<Void> deleteAuthenticatedStudent() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        studentService.deleteStudentByUsername(username);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exists/{username}")
    public ResponseEntity<Boolean> studentExists(@PathVariable String username) {
        boolean exists = studentService.studentExists(studentService.findStudentByUsername(username).getStudentId());
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/eligibility-to-graduate")
    public String checkEligibilityToGraduate(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<String> outstandingInvoices = studentService.checkOutstandingInvoices(username);
        model.addAttribute("outstandingInvoices", outstandingInvoices);
        model.addAttribute("isEligible", outstandingInvoices.isEmpty());
        return "eligibility"; // Name of the Thymeleaf template
    }

}
