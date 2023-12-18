package com.project.library.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;


import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.nio.file.*;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.sql.Timestamp;


@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/collections/{username}")
    public ResponseEntity<Map<String, Object>> getUserCollections(
        @PathVariable String username,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {

        Integer userId;
        try {
            userId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE username = ?", Integer.class, username);
        }
        catch (EmptyResultDataAccessException e) {
            userId = null;
        }

        int totalBooks = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM collections WHERE user_id = ?", Integer.class, userId);
        int totalPages = (int) Math.ceil((double) totalBooks / size);

        if (page >= totalPages) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Page out of range"));
        }

        int offset = page * size;
        List<Map<String, Object>> books = jdbcTemplate.queryForList(
            "SELECT b.*, c.COLLECTED_AT FROM books b JOIN collections c ON b.book_id = c.book_id WHERE c.user_id = ? LIMIT ? OFFSET ?",
            userId, size, offset);

        LocalDate now = LocalDate.now();
        books.forEach(book -> {
            LocalDate collectedAt = ((Timestamp) book.get("COLLECTED_AT")).toLocalDateTime().toLocalDate();
            LocalDate thirtyDaysAfterCollected = collectedAt.plusDays(30);
            long daysRemaining = ChronoUnit.DAYS.between(now, thirtyDaysAfterCollected);
            book.put("daysRemaining", daysRemaining);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("books", books);
        response.put("totalPages", totalPages);

        return ResponseEntity.ok(response);
    }




    // Get all books in the library
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllBooks(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {

        int totalBooks = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM books", Integer.class);
        int totalPages = (int) Math.ceil((double) totalBooks / size);

        if (page >= totalPages) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Page out of range"));
        }

        int offset = page * size;
        List<Map<String, Object>> books = jdbcTemplate.queryForList(
            "SELECT * FROM books LIMIT ? OFFSET ?", size, offset);

        Map<String, Object> response = new HashMap<>();
        response.put("books", books);
        response.put("totalPages", totalPages);

        return ResponseEntity.ok(response);
    }

    // Get content and cover of a specific book
    @GetMapping("/{bookId}")
    public Map<String, Object> getBookDetails(@PathVariable int bookId) {
        try {
            return jdbcTemplate.queryForMap(
                "SELECT cover_image_path, epub_path FROM books WHERE book_id = ?", bookId
            );
        } catch (EmptyResultDataAccessException e) {
            return null; 
        }
    }
    
    @PostMapping("/stock")
    public ResponseEntity<?> stockBook(@RequestParam String title, @RequestParam int quantity) {
        Integer bookId;
        try {
            bookId = jdbcTemplate.queryForObject(
                    "SELECT book_id FROM books WHERE title = ?", Integer.class, title);
        } catch (EmptyResultDataAccessException e) {
            
            bookId = null;
        }
        if (bookId == null) {
            jdbcTemplate.update(
                "INSERT INTO books (title, quantity) VALUES (?, ?)",
                title, quantity);
        } else {
            jdbcTemplate.update(
                "UPDATE books SET quantity = quantity + ? WHERE book_id = ?",
                quantity, bookId);
        }
        return ResponseEntity.ok().build();
    }

    
    
    @PostMapping("/borrow")
    public ResponseEntity<?> borrowBook(@RequestParam String title, @RequestParam String email) {
        //Find user ID based on email address
    	
    	Integer userId;
    	try {userId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE email = ?", Integer.class, email);
            	}
            catch (EmptyResultDataAccessException e) {
            	    
            	userId = null;
            }
    	
        //Find book ID based on book title
    	
    	Integer bookId;
        try {bookId = jdbcTemplate.queryForObject(
                "SELECT book_id FROM books WHERE title = ?", Integer.class, title);
        	}
        catch (EmptyResultDataAccessException e) {
        	    
        	    bookId = null;
        }
        
        if (userId == null || bookId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User or book not found");
        }
     //Check inventory and update borrowing records
        Integer availableQuantity;
        try {availableQuantity = jdbcTemplate.queryForObject(
                "SELECT quantity FROM books WHERE book_id = ?", Integer.class, bookId);
        	}
        catch (EmptyResultDataAccessException e) {
        	    
        	availableQuantity = null;
        }
        

        if (availableQuantity != null && availableQuantity >= 1) {
            jdbcTemplate.update("UPDATE books SET quantity = quantity - 1 WHERE book_id = ?", bookId);
            Boolean isBorrowed;
            try{jdbcTemplate.queryForObject(
            	    "SELECT COLLECTED_AT FROM collections WHERE user_id = ? AND book_id = ?", 
            	    java.sql.Timestamp.class, userId, bookId);
            		isBorrowed = true;
            }
            catch (EmptyResultDataAccessException e) {
        	    
            	isBorrowed = false;
            }
    		if (isBorrowed == true) {
    			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User has already borrowed this book and hasn't returned it yet");
    		} else {
    		    //Insert new entry
    		    jdbcTemplate.update(
    		        "INSERT INTO collections (user_id, book_id) VALUES (?, ?)", 
    		        userId, bookId);
    		}
            
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Insufficient stock");
        }
    }
    
    @PostMapping("/return")
    public ResponseEntity<?> returnBook(@RequestParam String title, @RequestParam String email) {
    	//Find user ID based on email address
    	
    	Integer userId;
    	try {userId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE email = ?", Integer.class, email);
            	}
            catch (EmptyResultDataAccessException e) {
            	    
            	userId = null;
            }
    	
        //Find book ID based on book title
    	
    	Integer bookId;
        try {bookId = jdbcTemplate.queryForObject(
                "SELECT book_id FROM books WHERE title = ?", Integer.class, title);
        	}
        catch (EmptyResultDataAccessException e) {
        	    
        	    bookId = null;
        }
        
        if (userId == null || bookId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User or book not found");
        }

        // Check if the book is borrowed by the user
        Boolean isBorrowed;
        try{
        	jdbcTemplate.queryForObject(
        	    "SELECT COLLECTED_AT FROM collections WHERE user_id = ? AND book_id = ?", 
        	    java.sql.Timestamp.class, userId, bookId);
        	isBorrowed = true;
        	}
        catch (EmptyResultDataAccessException e) {
    	    
        	isBorrowed = false;
        }
        
        
        if (isBorrowed != null && isBorrowed) {
            jdbcTemplate.update("UPDATE books SET quantity = quantity + 1 WHERE book_id = ?", bookId);
            jdbcTemplate.update("DELETE FROM collections WHERE user_id = ? AND book_id = ?", userId, bookId);

            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Book not borrowed by the user");
        }
    }

    
    @PostMapping("/upload")
    public ResponseEntity<?> uploadEbook(@RequestParam("file") MultipartFile file) {
    	if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File is empty");
        }
        

        try {
        	String fileName = file.getOriginalFilename();
            Path targetLocation = Paths.get("src/main/resources/static/epubs/" + fileName);
            

            
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Handle ebook logic
            handleEbookLogic(fileName, targetLocation);

            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file");
        }
    }
    
    private void handleEbookLogic(String fileName, Path filePath) throws FileNotFoundException, IOException {
        // Check if the ebook exists in the database
        Integer bookId;
        try {bookId = jdbcTemplate.queryForObject(
            "SELECT book_id FROM books WHERE epub_path = ?", Integer.class,filePath.toString());
        	}
        catch (EmptyResultDataAccessException e) {
        	    
        	    bookId = null;
        }

        if (bookId == null) {
            // Ebook does not exist in the database, create new book entry
            EpubReader epubReader = new EpubReader();
            Book ebook = epubReader.readEpub(new FileInputStream(filePath.toFile()));
            String title = ebook.getTitle();
            String coverImagePath = extractCoverImage(filePath);// Extract cover image

            jdbcTemplate.update(
                "INSERT INTO books (title, cover_image_path, epub_path, quantity) VALUES (?, ?, ?, 0)",
                title, coverImagePath, filePath.toString());
        } else {
            // Ebook already exists, update its path if necessary
            jdbcTemplate.update(
                "UPDATE books SET epub_path = ? WHERE book_id = ?", filePath.toString(), bookId);
        }
    }

    private String extractCoverImage(Path epubPath) {
        try {
            EpubReader epubReader = new EpubReader();
            Book ebook = epubReader.readEpub(new FileInputStream(epubPath.toFile()));
            String title = ebook.getTitle();
            // Extract cover imag
            Resource coverImage = ebook.getCoverImage();
            if (coverImage != null) {
                Path coverImagePath = Paths.get("src/main/resources/static/images/"+title+".jpg"); 
                Files.write(coverImagePath, coverImage.getData());
                return coverImagePath.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @PostMapping("/uploadCover")
    public ResponseEntity<?> uploadCoverImage(@RequestParam("file") MultipartFile file, @RequestParam String title) {
        //find book ID by title
    	Long bookId;
        try{bookId = jdbcTemplate.queryForObject(
            "SELECT book_id FROM books WHERE title = ?", Long.class, title);}
        catch (EmptyResultDataAccessException e) {
            
            bookId = null;
        }

        if (bookId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book not found");
        }
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File is empty");
        }

        try {
            String fileName = title;
            Path targetLocation = Paths.get("src/main/resources/static/images/" + fileName+".jpg");
            
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

           
            String imagePath = targetLocation.toString();
            updateBookCoverImagePath(bookId, imagePath);

            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not store file");
        }
    }

    private void updateBookCoverImagePath(Long bookId, String imagePath) {
        jdbcTemplate.update("UPDATE books SET cover_image_path = ? WHERE book_id = ?", imagePath.toString(), bookId);
    }






}

   
