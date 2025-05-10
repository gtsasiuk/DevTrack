package com.example.devtrack.security;

import com.example.devtrack.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.TestPropertySource;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = JwtUtil.class)
@TestPropertySource(properties = {
        "jwt.secret=TXlTdXBlclNlY3JldEtleUZvckNSTVN5c3RlbVRoYXRJc0F0TGVhc3QzMkNoYXJzTG9uZw==",
        "jwt.expiration=3600000"
})
public class JwtUtilTest {
    @Autowired
    private JwtUtil jwtUtil;
    private UserDetails userDetails;
    private final Long userId = 42L;
    private final Long expirationTime = 3600000L;
    private String token;

    @BeforeEach
    void setUp() {
        userDetails = User.withUsername("testuser")
                .password("password")
                .roles("USER", "ADMIN")
                .build();

        token = jwtUtil.generateToken(userDetails, userId, expirationTime);
    }

    @Test
    void testGenerateToken_NotNull() {
        assertNotNull(token);
    }

    @Test
    void testExtractUsername() {
        String username = jwtUtil.extractUsername(token);
        assertEquals("testuser", username);
    }

    @Test
    void testExtractExpiration() {
        Date expiration = jwtUtil.extractExpiration(token);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void testValidateToken_ValidToken() {
        assertTrue(jwtUtil.validateToken(token, userDetails));
    }

    @Test
    void testIsTokenExpired_False() {
        assertFalse(jwtUtil.isTokenExpired(token));
    }

    @Test
    void testIsValidToken_True() {
        assertTrue(jwtUtil.isValidToken(token));
    }

    @Test
    void testInvalidToken_ShouldReturnFalse() {
        String invalidToken = token + "tampered";
        assertFalse(jwtUtil.isValidToken(invalidToken));
    }

    @Test
    void testClaimsContainUserIdAndRoles() {
        var claims = jwtUtil.extractClaim(token, c -> c);

        assertEquals(userId.intValue(), claims.get("userId"));
        List<String> roles = (List<String>) claims.get("roles");
        assertTrue(roles.contains("ROLE_USER"));
        assertTrue(roles.contains("ROLE_ADMIN"));
    }
}
