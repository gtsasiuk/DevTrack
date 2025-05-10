package com.example.devtrack.controller;

import com.example.devtrack.model.Project;
import com.example.devtrack.model.User;
import com.example.devtrack.service.ProjectService;
import com.example.devtrack.service.UserService;
import com.example.devtrack.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class HomeControllerTest {
    @Mock
    private ProjectService projectService;
    @Mock
    private UserService userService;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private HttpServletRequest request;
    @Mock
    private Model model;
    @InjectMocks
    private HomeController homeController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testHome_WhenTokenIsMissing_ShouldRedirectToLogin() {
        when(request.getCookies()).thenReturn(null);

        String result = homeController.home(model, request);

        assertEquals("redirect:/auth/login", result);
    }

    @Test
    void testHome_WhenTokenIsInvalid_ShouldRedirectToLogin() {
        Cookie[] cookies = { new Cookie("jwt", "invalid-token") };
        when(request.getCookies()).thenReturn(cookies);
        when(jwtUtil.isValidToken("invalid-token")).thenReturn(false);

        String result = homeController.home(model, request);

        assertEquals("redirect:/auth/login", result);
    }

    @Test
    void testHome_WhenTokenIsValid_ShouldReturnHomeView() {
        String token = "valid-token";
        String username = "testUser";
        User user = new User();
        List<Project> projects = List.of(new Project());
        Map<String, Object> achievements = Map.of("completed", 5);

        Cookie[] cookies = { new Cookie("jwt", token) };
        when(request.getCookies()).thenReturn(cookies);
        when(jwtUtil.isValidToken(token)).thenReturn(true);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(user);
        when(projectService.sortByDeadlines(user)).thenReturn(projects);
        when(projectService.getUserAchievements(user)).thenReturn(achievements);
        when(request.getRequestURI()).thenReturn("/home");

        String result = homeController.home(model, request);

        verify(model).addAttribute("projects", projects);
        verify(model).addAttribute("achievements", achievements);
        verify(model).addAttribute("requestURI", "/home");
        verify(model).addAttribute("username", username);
        assertEquals("home", result);
    }
}
