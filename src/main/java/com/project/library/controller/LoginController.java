package com.project.library.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
@RestController
public class LoginController {

    private final JdbcTemplate jdbcTemplate;

    public LoginController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/login/{userType}")
    public ResponseEntity<?> login(@PathVariable String userType, @RequestParam String username, @RequestParam String password) {
        String tableName;

        // Select different tables based on the userType parameter.
        if ("admin".equals(userType)) {
            tableName = "admin";
        } else if ("user".equals(userType)) {
            tableName = "users";
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid user type");
        }

        String sql = "SELECT password FROM " + tableName + " WHERE username = ?";
        List<String> storedPasswords = jdbcTemplate.query(sql, new Object[]{username}, (rs, rowNum) -> rs.getString("password"));

        if (storedPasswords.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }

        String storedPassword = storedPasswords.get(0);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (passwordEncoder.matches(password, storedPassword)) {
            // Login successful.
            return ResponseEntity.ok().build();
        } else {
            // Login failed.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid password");
        }
    }


}
