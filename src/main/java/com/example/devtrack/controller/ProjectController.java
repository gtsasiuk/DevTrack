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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {
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
    public String getAllProjects(Model model, HttpServletRequest request) {
        String token = getJwtToken(request);

        if (token == null || !jwtUtil.isValidToken(token)) {
            return "redirect:/auth/login";
        }

        String username = jwtUtil.extractUsername(token);
        User currentUser = userService.findByUsername(username);
        List<Project> projects = projectService.findAllByUser(currentUser);
        model.addAttribute("projects", projects);
        return "project/projects";
    }

    @GetMapping("/create")
    public String showCreateProjectForm(Model model, HttpServletRequest request) {
        String token = getJwtToken(request);


        if (token == null || !jwtUtil.isValidToken(token)) {
            return "redirect:/auth/login";
        }

        String username = jwtUtil.extractUsername(token);
        User currentUser = userService.findByUsername(username);

        model.addAttribute("project", new Project());
        model.addAttribute("user", currentUser);
        return "project/create_project";
    }

    @PostMapping("/create")
    public String createProject(@ModelAttribute Project project, HttpServletRequest request) {
        String token = getJwtToken(request);

        if (token == null || !jwtUtil.isValidToken(token)) {
            return "redirect:/auth/login";
        }

        String username = jwtUtil.extractUsername(token);
        User currentUser = userService.findByUsername(username);

        project.setUser(currentUser);
        project.setStatus(Project.Status.ACTIVE);
        projectService.add(project);

        return "redirect:/projects";
    }

    @GetMapping("/edit/{id}")
    public String editProject(@PathVariable Long id, Model model, HttpServletRequest request) {
        String token = getJwtToken(request);
        if (token == null || !jwtUtil.isValidToken(token)) {
            return "redirect:/auth/login";
        }

        Project project = projectService.findById(id);
        model.addAttribute("project", project);
        return "project/edit_project";
    }

    @PostMapping("/edit/{id}")
    public String updateProject(@PathVariable Long id, @ModelAttribute Project project, HttpServletRequest request) {
        String token = getJwtToken(request);
        if (token == null || !jwtUtil.isValidToken(token)) {
            return "redirect:/auth/login";
        }

        String username = jwtUtil.extractUsername(token);
        User currentUser = userService.findByUsername(username);

        project.setUser(currentUser);
        project.setId(id);
        projectService.update(project);
        return "redirect:/projects";
    }

    @GetMapping("/delete/{id}")
    public String deleteProject(@PathVariable Long id, HttpServletRequest request) {
        String token = getJwtToken(request);
        if (token == null || !jwtUtil.isValidToken(token)) {
            return "redirect:/auth/login";
        }

        projectService.delete(id);
        return "redirect:/projects";
    }

}
