package com.example.devtrack.controller;

import com.example.devtrack.model.Role;
import com.example.devtrack.model.User;
import com.example.devtrack.service.RoleService;
import com.example.devtrack.service.UserService;
import com.example.devtrack.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final RoleService roleService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    @GetMapping("/")
    public String enter(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    String token = cookie.getValue();
                    if (jwtUtil.isValidToken(token)) {
                        return "redirect:/home";
                    }
                }
            }
        }
        return "redirect:auth/login";
    }

    @GetMapping("/auth/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        HttpServletResponse response,
                        Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password.");
        }
        if (logout != null) {
            Cookie jwtCookie = new Cookie("jwt", null); // Встановити значення null для видалення
            jwtCookie.setMaxAge(0); // Зробити cookie негайно видимим для браузера
            jwtCookie.setPath("/"); // Вказати правильний шлях
            if (!"localhost".equalsIgnoreCase(response.getHeader("Host"))) {
                jwtCookie.setSecure(true);
            }
            response.addCookie(jwtCookie);
            model.addAttribute("logout", "You have been logged out.");
        }
        return "auth/login";
    }

    @PostMapping("/auth/login")
    public String authenticateUser(@RequestParam String username,
                                   @RequestParam String password,
                                   HttpServletResponse response,
                                   Model model) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            User user = userService.findByUsername(username);
            String token = jwtUtil.generateToken(userDetails, user.getId());

            System.out.println("User authenticated: " + user.getUsername());

            Cookie jwtCookie = new Cookie("jwt", token);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setMaxAge(60 * 60); // 1 hour
            jwtCookie.setPath("/");
            // Remove Secure flag for localhost development
            if (!"localhost".equalsIgnoreCase(response.getHeader("Host"))) {
                jwtCookie.setSecure(true);
            }
            response.addCookie(jwtCookie);

            return "redirect:/home";
        } catch (Exception e) {
            System.err.println("Authentication error: " + e.getMessage());
            model.addAttribute("error", "Invalid username or password.");
            return "auth/login";
        }
    }

    @GetMapping("/auth/registration")
    public String registration(Model model) {
        model.addAttribute("user", new User());
        return "auth/registration";
    }

    @PostMapping("/auth/registration")
    public String registerUser(User user, Model model, HttpServletResponse response) {
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
        user.setEnabled(true);

        userService.add(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtUtil.generateToken(userDetails, user.getId());

        Cookie jwtCookie = new Cookie("jwt", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setMaxAge(60 * 60);
        jwtCookie.setPath("/");
        if (!"localhost".equalsIgnoreCase(response.getHeader("Host"))) {
            jwtCookie.setSecure(true);
        }
        response.addCookie(jwtCookie);

        return "redirect:/home";
    }
}
