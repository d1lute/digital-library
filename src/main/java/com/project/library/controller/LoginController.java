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

        // 根据 userType 参数选择不同的表
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
            // 登录成功
            return ResponseEntity.ok().build();
        } else {
            // 登录失败
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid password");
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedPassword = md.digest(password.getBytes());
            return bytesToHex(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
