package com.example.projectSesc.UserService.security;

import com.example.projectSesc.UserService.domain.Student;
import com.example.projectSesc.UserService.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final StudentRepository studentRepository;

    @Autowired
    public CustomUserDetailsService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Student student = studentRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Student not found with username: " + username));

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(student.getRole());
        return new org.springframework.security.core.userdetails.User(
                student.getUsername(),
                student.getPassword(),
                Collections.singletonList(authority)
        );
    }
}

