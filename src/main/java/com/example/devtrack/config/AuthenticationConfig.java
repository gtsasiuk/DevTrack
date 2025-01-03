package com.example.devtrack.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.sql.DataSource;

@Configuration
public class AuthenticationConfig {
    private final DataSource dataSource;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationConfig(DataSource dataSource, BCryptPasswordEncoder passwordEncoder) {
        this.dataSource = dataSource;
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    public void configureAuthentication(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication()
                .dataSource(dataSource)
                .usersByUsernameQuery(
                        "SELECT username, password_hash, enabled FROM users WHERE username = ?")
                .authoritiesByUsernameQuery(
                        "SELECT u.username, r.name " +
                                "FROM user_roles ur " +
                                "JOIN users u ON ur.user_id = u.id " +
                                "JOIN roles r ON ur.role_id = r.id " +
                                "WHERE u.username = ?")
                .passwordEncoder(passwordEncoder);
    }
}
