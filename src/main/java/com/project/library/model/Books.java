package com.project.library.model;

public class Books {
    private Long id; 
    private String title; 
    private String coverImagePath; 
    private String epubPath; 
    private int quantity; 
    
    public Books(Long id, String title, String coverImagePath, String epubPath) {
        this.id = id;
        this.title = title;
        this.coverImagePath = coverImagePath;
        this.epubPath = epubPath;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCoverImagePath() {
        return coverImagePath;
    }

    public void setCoverImagePath(String coverImagePath) {
        this.coverImagePath = coverImagePath;
    }

    public String getEpubPath() {
        return epubPath;
    }

    public void setEpubPath(String epubPath) {
        this.epubPath = epubPath;
    }
    
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }


    @Override
    public String toString() {
        return "Book{" +
               "id=" + id +
               ", title='" + title + '\'' +
               ", coverImagePath='" + coverImagePath + '\'' +
               ", epubPath='" + epubPath + '\'' +
               '}';
    }
}
