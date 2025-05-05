package com.example.devtrack.controller;

import com.example.devtrack.model.User;
import com.example.devtrack.service.ProjectService;
import com.example.devtrack.service.UserService;
import com.example.devtrack.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProjectService projectService;
    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final MessageSource messageSource;
    private final JwtUtil jwtUtil;

    private String getJwtToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    @GetMapping
    public String profile(Model model, HttpServletRequest request) {
        String token = getJwtToken(request);

        if (token == null || !jwtUtil.isValidToken(token)) {
            return "redirect:/auth/login";
        }

        String username = jwtUtil.extractUsername(token);
        User currentUser = userService.findByUsername(username);
        model.addAttribute("user", currentUser);
        model.addAttribute("requestURI",request.getRequestURI());
        Map<String, Object> stats = projectService.getFullProfileStatistics(currentUser);
        model.addAttribute("stats", stats);
        return "profile/profile";
    }

    @GetMapping("/change_profile")
    public String changeProfile(Model model, HttpServletRequest request) {
        String token = getJwtToken(request);

        if (token == null || !jwtUtil.isValidToken(token)) {
            return "redirect:/auth/login";
        }

        String username = jwtUtil.extractUsername(token);
        User currentUser = userService.findByUsername(username);
        model.addAttribute("user", currentUser);
        model.addAttribute("requestURI",request.getRequestURI());
        return "profile/change_profile";
    }

    @PostMapping("/change_profile")
    public String updateProfile(@ModelAttribute("user") User updatedUser,
                                HttpServletRequest request,
                                HttpServletResponse response,
                                Model model) {
        String token = getJwtToken(request);
        if (token == null || !jwtUtil.isValidToken(token)) {
            return "redirect:/auth/login";
        }

        String currentUsername = jwtUtil.extractUsername(token);
        User currentUser = userService.findByUsername(currentUsername);

        boolean usernameChanged = !updatedUser.getUsername().equals(currentUser.getUsername());
        boolean emailChanged = !updatedUser.getEmail().equals(currentUser.getEmail());
        boolean hasError = false;

        if (usernameChanged && userService.existsByUsername(updatedUser.getUsername())) {
            model.addAttribute("usernameTaken", "Username already taken.");
            hasError = true;
        }

        if (emailChanged && userService.existsByEmail(updatedUser.getEmail())) {
            model.addAttribute("emailTaken", "Email already taken.");
            hasError = true;
        }

        if (hasError) {
            model.addAttribute("requestURI", request.getRequestURI());
            return "profile/change_profile";
        }

        currentUser.setUsername(updatedUser.getUsername());
        currentUser.setEmail(updatedUser.getEmail());

        userService.update(currentUser);

        if (usernameChanged) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(currentUser.getUsername());
            String newToken = jwtUtil.generateToken(userDetails, currentUser.getId(), jwtUtil.getExpiration());

            Cookie newJwtCookie = new Cookie("jwt", newToken);
            newJwtCookie.setHttpOnly(true);
            newJwtCookie.setPath("/");
            newJwtCookie.setMaxAge(24 * 60 * 60);

            response.addCookie(newJwtCookie);
        }

        return "redirect:/profile";
    }

    @GetMapping("/change_password")
    public String changePassword(Model model, HttpServletRequest request) {
        String token = getJwtToken(request);

        if (token == null || !jwtUtil.isValidToken(token)) {
            return "redirect:/auth/login";
        }

        String username = jwtUtil.extractUsername(token);
        User currentUser = userService.findByUsername(username);
        model.addAttribute("user", currentUser);
        model.addAttribute("requestURI",request.getRequestURI());
        return "profile/change_password";
    }

    @PostMapping("/change_password")
    public String updatePassword(@ModelAttribute("user") User updatedUser,
                                 @RequestParam String confirmPassword,
                                 HttpServletRequest request,
                                 Model model) {
        String token = getJwtToken(request);
        if (token == null || !jwtUtil.isValidToken(token)) {
            return "redirect:/auth/login";
        }

        String newPassword = updatedUser.getPassword();
        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$";
        boolean hasError = false;

        if (!newPassword.matches(passwordPattern)) {
            model.addAttribute("passwordError", "Password must be at least 8 characters long, include one uppercase letter, one lowercase letter, one digit, and one special character.");
            hasError = true;
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("confirmPasswordError", "Passwords do not match.");
            hasError = true;
        }

        String username = jwtUtil.extractUsername(token);
        User currentUser = userService.findByUsername(username);

        if (passwordEncoder.matches(newPassword, currentUser.getPassword())) {
            model.addAttribute("passwordError", "New password must be different from the current password.");
            hasError = true;
        }

        if (hasError) {
            model.addAttribute("requestURI", request.getRequestURI());
            return "profile/change_password";
        }

        currentUser.setPassword(passwordEncoder.encode(newPassword));
        userService.update(currentUser);

        return "redirect:/profile";
    }
}
