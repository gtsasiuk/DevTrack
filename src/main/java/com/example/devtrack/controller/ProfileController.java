package com.example.devtrack.controller;

import com.example.devtrack.model.User;
import com.example.devtrack.service.ProjectService;
import com.example.devtrack.service.UserService;
import com.example.devtrack.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProjectService projectService;
    private final UserService userService;
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
}
