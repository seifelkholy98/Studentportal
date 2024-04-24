package com.example.projectSesc.CourseService.service;

import com.example.projectSesc.CourseService.domain.Course;
import com.example.projectSesc.CourseService.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    @Autowired
    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public Course createCourse(Course course) {
        return courseRepository.save(course);
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public Course updateCourse(Long courseId, Course courseDetails) {
        return courseRepository.findById(courseId)
                .map(course -> {
                    course.setCourseName(courseDetails.getCourseName());
                    course.setDescription(courseDetails.getDescription());
                    course.setPrice(courseDetails.getPrice());
                    return courseRepository.save(course);
                }).orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));
    }
    public boolean courseExists(Long courseId) {
        return courseRepository.findById(courseId).isPresent();
    }

    public void deleteCourse(Long courseId) {
        courseRepository.deleteById(courseId);
    }

    public Course getCourseById(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));
    }
}
