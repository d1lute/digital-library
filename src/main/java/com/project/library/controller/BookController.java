package com.project.library.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.library.model.Book;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.ArrayList;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.file.*;
@RestController
@RequestMapping("/api/books")
public class BookController {
	private static final String UPLOAD_DIR = "/path/to/upload/dir";
	private JdbcTemplate jdbcTemplate;
 
    private final Path rootLocation = Paths.get("file-storage"); // 定义文件存储位置

    @PostMapping("/upload")
    public String uploadBook(@RequestParam("cover") MultipartFile cover,
                             @RequestParam("content") MultipartFile content,
                             @RequestParam("title") String title,
                             @RequestParam(required = false) String author,
                             @RequestParam(required = false) String summary) {
        try {
            // 存储封面图片
            String coverFilename = storeFile(cover);
            // 存储书籍内容
            String contentFilename = storeFile(content);

            // 插入书籍信息到数据库
            String sql = "INSERT INTO book (title, author, summary, cover_url, content_url) VALUES (?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, title, author, summary, coverFilename, contentFilename);

            return "Book uploaded successfully";
        } catch (Exception e) {
            throw new RuntimeException("Failed to store files", e);
        }
    }

    private String storeFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalStateException("Cannot store empty file.");
        }
        String filename = file.getOriginalFilename();
        Path destinationFile = rootLocation.resolve(filename).normalize().toAbsolutePath();
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        }
        return destinationFile.toString();
    }

    

}
    
    
   
