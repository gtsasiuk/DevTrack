package com.example.devtrack.service;

import com.example.devtrack.model.User;
import com.example.devtrack.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@AllArgsConstructor
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User add(User user) {
        return userRepository.save(user);
    }

    public User findById(long id) {
        return userRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("User with ID " + id + " not found"));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public void update(User user) {
        userRepository.save(user);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException("User not found with username: " + username));
    }

    private void deleteById(long id) {
        userRepository.deleteById(id);
    }
}
