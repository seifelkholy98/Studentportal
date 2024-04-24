package com.example.projectSesc.UserService.controller;
import com.example.projectSesc.UserService.service.StudentService;

import com.example.projectSesc.UserService.domain.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class mainController {// Login form

    private final StudentService studentService;
    @Autowired
    public mainController(StudentService studentService) {
        this.studentService = studentService;

    }
    @GetMapping("/login")
    public String login() {
        return "loginpage.html";
    }

    @RequestMapping("/coursesforguests")
    public String coursesforguests() {
        return "coursesforguests.html";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("student", new Student());
        return "register";
    }

    @RequestMapping("/home")
    public String showDashboard(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Student student = studentService.findStudentByUsername(username);
            model.addAttribute("student", student);
            return "home.html";
        } else {
            return "redirect:/login";
        }
    }

    @GetMapping("/profile")
    public String getAuthenticatedStudent(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Student student = studentService.findStudentByUsername(username);
            model.addAttribute("student", student);
            return "profile.html";
        } else {
            return "redirect:/login";
        }
    }
}
