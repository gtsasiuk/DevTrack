package com.example.devtrack.controller;

import com.example.devtrack.model.User;
import com.example.devtrack.service.ProjectService;
import com.example.devtrack.service.UserService;
import com.example.devtrack.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.Model;

import java.util.Locale;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
class ProfileControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private UserService userService;

    @MockBean
    private BCryptPasswordEncoder passwordEncoder;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private MessageSource messageSource;

    @MockBean
    private JwtUtil jwtUtil;

    private User mockUser;

    @BeforeEach
    void setup() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setEmail("test@example.com");
        mockUser.setPassword("hashedPassword");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void profile_shouldRedirectToLoginIfTokenIsInvalid() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void profile_shouldReturnProfilePageIfTokenIsValid() throws Exception {
        Cookie cookie = new Cookie("jwt", "valid.token");

        when(jwtUtil.isValidToken("valid.token")).thenReturn(true);
        when(jwtUtil.extractUsername("valid.token")).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(mockUser);
        when(projectService.getFullProfileStatistics(mockUser)).thenReturn(Map.of());

        mockMvc.perform(get("/profile").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/profile"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("stats"))
                .andExpect(model().attributeExists("requestURI"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void updateProfile_shouldRejectIfUsernameExists() throws Exception {
        Cookie cookie = new Cookie("jwt", "valid.token");
        User updatedUser = new User();
        updatedUser.setUsername("newuser");
        updatedUser.setEmail("test@example.com");

        when(jwtUtil.isValidToken("valid.token")).thenReturn(true);
        when(jwtUtil.extractUsername("valid.token")).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(mockUser);
        when(userService.existsByUsername("newuser")).thenReturn(true);
        when(messageSource.getMessage(eq("error.username.exists"), any(), any(Locale.class)))
                .thenReturn("Username already exists");

        mockMvc.perform(post("/profile/change_profile")
                        .with(csrf())
                        .cookie(cookie)
                        .param("username", "newuser")
                        .param("email", "test@example.com")
                )
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("usernameTaken"))
                .andExpect(view().name("profile/change_profile"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void updatePassword_shouldRejectIfPasswordsDoNotMatch() throws Exception {
        Cookie cookie = new Cookie("jwt", "valid.token");

        when(jwtUtil.isValidToken("valid.token")).thenReturn(true);
        when(jwtUtil.extractUsername("valid.token")).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(mockUser);
        when(messageSource.getMessage(eq("error.password.mismatch"), any(), any(Locale.class)))
                .thenReturn("Passwords do not match");

        mockMvc.perform(post("/profile/change_password")
                        .with(csrf())
                        .cookie(cookie)
                        .param("password", "NewPassword1!")
                        .param("confirmPassword", "DifferentPassword")
                )
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("confirmPasswordError"))
                .andExpect(view().name("profile/change_password"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void updatePassword_shouldRedirectOnSuccess() throws Exception {
        Cookie cookie = new Cookie("jwt", "valid.token");

        when(jwtUtil.isValidToken("valid.token")).thenReturn(true);
        when(jwtUtil.extractUsername("valid.token")).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(mockUser);
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        mockMvc.perform(post("/profile/change_password")
                        .with(csrf())
                        .cookie(cookie)
                        .param("password", "NewPassword1!")
                        .param("confirmPassword", "NewPassword1!")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));
    }
}
