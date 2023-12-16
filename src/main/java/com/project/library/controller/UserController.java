package com.project.library.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@RestController
public class UserController {
	 @Autowired
	    private JdbcTemplate jdbcTemplate;

	    @PostMapping("/register")
	    public ResponseEntity<String> register(@RequestParam String username,
	                                           @RequestParam String password,
	                                           @RequestParam String email) {
	        // 这里应该有一些验证逻辑
	    	BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	    	String hashedPassword = passwordEncoder.encode(password);
	        String sql = "INSERT INTO users (username, Password, email) VALUES (?, ?, ?)";
	        int result = jdbcTemplate.update(sql, username, hashedPassword, email);

	        if (result > 0) {
	            return ResponseEntity.ok("User registered successfully");
	        } else {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Registration failed");
	        }
	    }
}

