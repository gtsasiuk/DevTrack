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
    void testHome_WhenJwtCookieIsMissing_ShouldReturnHomeWithNullData() {
        when(request.getCookies()).thenReturn(null);
        
        String result = homeController.home(model, request);

        verify(jwtUtil).extractUsername(null);
        verify(userService).findByUsername(null);
        verify(projectService).sortByDeadlines(null);
        verify(projectService).getUserAchievements(null);
        verify(model).addAttribute(eq("projects"), any());
        verify(model).addAttribute(eq("achievements"), any());
        verify(model).addAttribute("requestURI", null);
        verify(model).addAttribute("username", null);

        assertEquals("home", result);
    }

    @Test
    void testHome_WhenJwtCookieExists_ShouldPopulateModelAndReturnHome() {
        String token = "valid-token";
        String username = "testUser";
        User user = new User();
        List<Project> projects = List.of(new Project());
        Map<String, Object> achievements = Map.of("completed", 5);

        Cookie[] cookies = { new Cookie("jwt", token) };
        when(request.getCookies()).thenReturn(cookies);
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
