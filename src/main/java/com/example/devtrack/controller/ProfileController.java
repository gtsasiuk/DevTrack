package com.example.devtrack.controller;

import com.example.devtrack.model.User;
import com.example.devtrack.service.ProjectService;
import com.example.devtrack.service.UserService;
import com.example.devtrack.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProjectService projectService;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
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
    public String updateProfile(@ModelAttribute("user") User updatedUser, HttpServletRequest request, HttpServletResponse response, Model model) {
        String token = getJwtToken(request);
        if (token == null || !jwtUtil.isValidToken(token)) {
            return "redirect:/auth/login";
        }

        String currentUsername = jwtUtil.extractUsername(token);
        User currentUser = userService.findByUsername(currentUsername);

        boolean usernameChanged = !updatedUser.getUsername().equals(currentUser.getUsername());
        boolean emailChanged = !updatedUser.getEmail().equals(currentUser.getEmail());

        if ((usernameChanged &&
                userService.existsByUsername(updatedUser.getUsername())) && (emailChanged &&
                userService.existsByEmail(updatedUser.getEmail()))) {
            model.addAttribute("usernameTaken", "Username already taken.");
            model.addAttribute("emailTaken", "Email already taken.");
            model.addAttribute("requestURI",request.getRequestURI());
            return "profile/change_profile";
        } else if (usernameChanged && userService.existsByUsername(updatedUser.getUsername())) {
            model.addAttribute("usernameTaken", "Username already taken.");
            model.addAttribute("requestURI",request.getRequestURI());
            return "profile/change_profile";
        } else if (emailChanged && userService.existsByEmail(updatedUser.getEmail())) {
            model.addAttribute("emailTaken", "Email already taken.");
            model.addAttribute("requestURI",request.getRequestURI());
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
            newJwtCookie.setMaxAge(24 * 60 * 60); // 1 доба

            response.addCookie(newJwtCookie);
        }

        return "redirect:/profile";
    }
}
