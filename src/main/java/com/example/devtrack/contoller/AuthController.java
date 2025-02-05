package com.example.devtrack.contoller;

import com.example.devtrack.model.Role;
import com.example.devtrack.model.User;
import com.example.devtrack.service.RoleService;
import com.example.devtrack.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/")
    public String enter() {
        return "redirect:auth/login";
    }

    @GetMapping("/auth/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password.");
        }
        if (logout != null) {
            model.addAttribute("logout", "You have been logged out.");
        }
        return "auth/login";
    }

    @GetMapping("/auth/registration")
    public String registration(Model model) {
        model.addAttribute("user", new User());
        return "auth/registration";
    }

    @PostMapping("/auth/registration")
    public String registerUser(User user, Model model) {
        if (userService.existsByUsername(user.getUsername())) {
            model.addAttribute("error", "Username is already taken.");
            return "auth/registration";
        }
        if (userService.existsByEmail(user.getEmail())) {
            model.addAttribute("error", "Email is already in use.");
            return "auth/registration";
        }

        String rawPassword = user.getPassword();

        user.setPassword(passwordEncoder.encode(rawPassword));
        Role userRole = roleService.findByName("ROLE_USER");

        user.getRoles().add(userRole);

        userService.add(user);

        return "redirect:/home";
    }
}
