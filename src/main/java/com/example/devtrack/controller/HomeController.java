package com.example.devtrack.controller;

import com.example.devtrack.model.Project;
import com.example.devtrack.model.User;
import com.example.devtrack.service.ProjectService;
import com.example.devtrack.service.UserService;
import com.example.devtrack.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class HomeController {
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

    @GetMapping("/home")
    public String home(Model model, HttpServletRequest request) {
        String token = getJwtToken(request);

        if (token == null || !jwtUtil.isValidToken(token)) {
            return "redirect:/auth/login";
        }

        String username = jwtUtil.extractUsername(token);
        User currentUser = userService.findByUsername(username);
        List<Project> projects = projectService.sortByDeadlines(currentUser);
        Map<String, Object> achievements = projectService.getUserAchievements(currentUser);

        model.addAttribute("projects", projects);
        model.addAttribute("achievements", achievements);
        model.addAttribute("requestURI", request.getRequestURI());
        model.addAttribute("username", username);
        return "home";
    }
}
