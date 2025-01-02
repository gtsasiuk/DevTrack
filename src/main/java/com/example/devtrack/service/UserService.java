package com.example.devtrack.service;

import com.example.devtrack.model.User;
import com.example.devtrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class UserService {
    private UserRepository userRepository;

    private User add(User user) {
        return userRepository.save(user);
    }

    private User findById(long id) {
        return userRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("User with ID " + id + " not found"));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    private User update(User user) {
        return userRepository.save(user);
    }

    private void deleteById(long id) {
        userRepository.deleteById(id);
    }
}
