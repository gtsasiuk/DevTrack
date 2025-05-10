package com.example.devtrack.service;

import com.example.devtrack.model.User;
import com.example.devtrack.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserService userService;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sampleUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("hashed")
                .build();
    }

    @Test
    void testAdd() {
        when(userRepository.save(sampleUser)).thenReturn(sampleUser);
        User savedUser = userService.add(sampleUser);
        assertEquals(sampleUser, savedUser);
    }

    @Test
    void testFindById_UserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        User foundUser = userService.findById(1L);
        assertEquals("testuser", foundUser.getUsername());
    }

    @Test
    void testFindById_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> userService.findById(1L));
    }

    @Test
    void testFindAll() {
        List<User> users = List.of(sampleUser);
        when(userRepository.findAll()).thenReturn(users);
        List<User> result = userService.findAll();
        assertEquals(1, result.size());
    }

    @Test
    void testUpdate() {
        userService.update(sampleUser);
        verify(userRepository).save(sampleUser);
    }

    @Test
    void testExistsByUsername() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        assertTrue(userService.existsByUsername("testuser"));
    }

    @Test
    void testExistsByEmail() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        assertTrue(userService.existsByEmail("test@example.com"));
    }

    @Test
    void testFindByUsername_UserExists() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));
        User result = userService.findByUsername("testuser");
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void testFindByUsername_UserNotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userService.findByUsername("testuser"));
    }
}
