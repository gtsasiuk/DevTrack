package com.example.devtrack.controller;

import com.example.devtrack.model.User;
import com.example.devtrack.service.RoleService;
import com.example.devtrack.service.UserService;
import com.example.devtrack.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.Model;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class AuthControllerTest {
    @Mock
    private UserService userService;
    @Mock
    private RoleService roleService;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private MessageSource messageSource;
    @Mock
    private Model model;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @InjectMocks
    private AuthController authController;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authController = new AuthController(userService, roleService, passwordEncoder, authenticationManager, userDetailsService, messageSource, jwtUtil);
    }

    @Test
    void testEnter_WhenTokenIsValid_ShouldRedirectToHome() {
        Cookie validJwtCookie = new Cookie("jwt", "valid-token");
        when(request.getCookies()).thenReturn(new Cookie[] { validJwtCookie });
        when(jwtUtil.isValidToken(anyString())).thenReturn(true);

        String result = authController.enter(request);

        assertEquals("redirect:/home", result);
    }

    @Test
    void testLogin_WhenError_ShouldShowErrorMessage() {
        String error = "Invalid username or password.";
        when(messageSource.getMessage(eq("error.password.incorrect"), any(), any())).thenReturn(error);

        String result = authController.login("error", null, response, model);

        verify(model).addAttribute("error", error);
        assertEquals("auth/login", result);
    }

    @Test
    void testAuthenticateUser_WhenSuccessful_ShouldRedirectToHome() {
        String username = "user";
        String password = "password";
        String token = "jwt-token";
        UserDetails userDetails = mock(UserDetails.class);
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);

        when(userService.findByUsername(username)).thenReturn(user);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtUtil.generateToken(any(), any(), anyLong())).thenReturn(token);

        String result = authController.authenticateUser(username, password, false, response, model);

        verify(response).addCookie(any(Cookie.class));
        assertEquals("redirect:/home", result);
    }

    @Test
    void testRegisterUser_WhenUsernameExists_ShouldShowErrorMessage() {
        String username = "existingUser";
        String password = "validPassword123!";
        String confirmPassword = "validPassword123!";
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);

        when(userService.existsByUsername(username)).thenReturn(true);
        when(messageSource.getMessage(eq("error.username.exists"), any(), any())).thenReturn("Username already exists.");

        String result = authController.registerUser(user, model, response, confirmPassword, false);

        verify(model).addAttribute("usernameError", "Username already exists.");
        assertEquals("auth/registration", result);
    }

    @Test
    void testRegistration_WhenNoErrors_ShouldRedirectToHome() {
        String username = "newUser";
        String rawPassword = "validPassword123!";
        String encodedPassword = "encodedPassword";
        String confirmPassword = "validPassword123!";
        String token = "jwt-token";

        User user = new User();
        user.setUsername(username);
        user.setPassword(rawPassword);
        user.setEmail("newuser@example.com");

        UserDetails userDetails = mock(UserDetails.class);

        when(userService.existsByUsername(username)).thenReturn(false);
        when(userService.existsByEmail(user.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(roleService.findByName("ROLE_USER")).thenReturn(new com.example.devtrack.model.Role());
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtUtil.getExpiration()).thenReturn(86400000L); // 1 день
        when(jwtUtil.generateToken(userDetails, null, 86400000L)).thenReturn(token);

        String result = authController.registerUser(user, model, response, confirmPassword, false);

        verify(response).addCookie(any(Cookie.class));
        assertEquals("redirect:/home", result);
    }
}
