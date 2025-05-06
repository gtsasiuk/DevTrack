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
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Locale;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final RoleService roleService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final MessageSource messageSource;
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
            Locale locale = LocaleContextHolder.getLocale();
            Cookie jwtCookie = new Cookie("jwt", null);
            jwtCookie.setMaxAge(0);
            jwtCookie.setPath("/");
            if (!"localhost".equalsIgnoreCase(response.getHeader("Host"))) {
                jwtCookie.setSecure(true);
            }
            response.addCookie(jwtCookie);
            model.addAttribute("logout", messageSource.getMessage("logout.success", null, locale));
        }
        return "auth/login";
    }

    @PostMapping("/auth/login")
    public String authenticateUser(@RequestParam String username, @RequestParam String password,
                                   @RequestParam(value = "rememberMe", required = false) Boolean rememberMe,
                                   HttpServletResponse response, Model model) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            User user = userService.findByUsername(username);
            Long expirationTime = (rememberMe != null && rememberMe) ? 30 * 24 * 60 * 60 * 1000L : jwtUtil.getExpiration();

            String token = jwtUtil.generateToken(userDetails, user.getId(), expirationTime);

            Cookie jwtCookie = new Cookie("jwt", token);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setMaxAge(rememberMe != null && rememberMe ? 30 * 24 * 60 * 60 : 24 * 60 * 60);
            jwtCookie.setPath("/");
            if (!"localhost".equalsIgnoreCase(response.getHeader("Host"))) {
                jwtCookie.setSecure(true);
            }
            response.addCookie(jwtCookie);
            model.addAttribute("success", true);

            return "redirect:/home";
        } catch (Exception e) {
            Locale locale = LocaleContextHolder.getLocale();
            System.err.println("Authentication error: " + e.getMessage());
            if (!userService.existsByUsername(username)) {
                model.addAttribute("usernameError", messageSource.getMessage("error.username.notfound", null, locale));
            } else {
                model.addAttribute("passwordError", messageSource.getMessage("error.password.incorrect", null, locale));
            }
            return "auth/login";
        }
    }

    @GetMapping("/auth/registration")
    public String registration(Model model) {
        model.addAttribute("user", new User());
        return "auth/registration";
    }

    @PostMapping("/auth/registration")
    public String registerUser(User user, Model model,
                               HttpServletResponse response,
                               @RequestParam String confirmPassword,
                               @RequestParam(value = "rememberMe", required = false) Boolean rememberMe) {
        try {
            boolean hasError = false;
            Locale locale = LocaleContextHolder.getLocale();

            String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$";

            if (!user.getPassword().matches(passwordPattern)) {
                model.addAttribute("passwordError", messageSource.getMessage("error.password.pattern", null, locale));
                hasError = true;
            }

            if (!user.getPassword().equals(confirmPassword)) {
                model.addAttribute("confirmPasswordError", messageSource.getMessage("error.password.mismatch", null, locale));
                hasError = true;
            }

            if (userService.existsByUsername(user.getUsername())) {
                model.addAttribute("usernameError", messageSource.getMessage("error.username.exists", null, locale));
                hasError = true;
            }

            if (userService.existsByEmail(user.getEmail())) {
                model.addAttribute("emailError", messageSource.getMessage("error.email.exists", null, locale));
                hasError = true;
            }

            if (hasError) {
                return "auth/registration";
            }

            String rawPassword = user.getPassword();
            user.setPassword(passwordEncoder.encode(rawPassword));

            Role userRole = roleService.findByName("ROLE_USER");
            user.getRoles().add(userRole);
            user.setEnabled(true);

            userService.add(user);

            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            Long expirationTime = (rememberMe != null && rememberMe) ? 30 * 24 * 60 * 60 * 1000L : jwtUtil.getExpiration();
            String token = jwtUtil.generateToken(userDetails, user.getId(), expirationTime);

            Cookie jwtCookie = new Cookie("jwt", token);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setMaxAge(rememberMe != null && rememberMe ? 30 * 24 * 60 * 60 : 24 * 60 * 60);
            jwtCookie.setPath("/");
            if (!"localhost".equalsIgnoreCase(response.getHeader("Host"))) {
                jwtCookie.setSecure(true);
            }
            response.addCookie(jwtCookie);

            return "redirect:/home";
        } catch (Exception e) {
            System.err.println("Authentication error: " + e.getMessage());
            return "auth/registration";
        }
    }
}
