package com.example.projectSesc.CourseService.controller;


import com.example.projectSesc.CourseService.domain.Course;
import com.example.projectSesc.CourseService.service.CourseService;
import com.example.projectSesc.UserService.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
public class mainCourseController {


    private final CourseService courseService;



    @Autowired
    public mainCourseController(CourseService courseService) {
        this.courseService = courseService;
    }


    @RequestMapping("/courses")
    public String getAllCourses(Model model) {
        List<Course> courses = courseService.getAllCourses();
        model.addAttribute("courses", courses);
        return "courses";
    }
}
