package com.example.projectSesc.UserService.service;

import com.example.projectSesc.UserService.domain.User;
import com.example.projectSesc.UserService.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Add PasswordEncoder

    @Autowired
    public UserService(UserRepository userRepository,PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found for username: " + username);
        }
        return null ;
//        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), new ArrayList<>()); // Add appropriate authorities
    }

    /**
     * Register a new user with encrypted password
     */
    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    /**
     * Find a user by ID.
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Retrieve all users.
     */
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Update a user's information.
     */
    public Optional<User> updateUser(Long id, User userDetails) {
        return userRepository.findById(id).map(user -> {
            user.setUsername(userDetails.getUsername());
            user.setName(userDetails.getName());
            user.setSurname(userDetails.getSurname());
            userRepository.save(user);
            return user;
        });
    }

    /**
     * Delete a user by ID.
     */
    public boolean deleteUser(Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    userRepository.delete(user);
                    return true;
                }).orElse(false);
    }


}
