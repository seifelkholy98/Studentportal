package com.example.projectSesc.UserService.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Define the password encoder
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // Disable CSRF (not recommended for production)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register").permitAll() // Permit all for login and register
                        .anyRequest().authenticated()) // All other requests require authentication
                .formLogin(form -> form
                        .loginPage("/login") // Custom login page
                        .defaultSuccessUrl("/")) // Redirect after successful login
                .logout(logout -> logout
                        .permitAll()); // Permit all for logout
        return http.build();
    }
}
