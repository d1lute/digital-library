package com.project.library.model;

public class Book {
    private Long id;
    private String title;
    private String author;
    private String summary;
    private String coverUrl;
    private String contentUrl;

    // 构造函数，包括所有字段
    public Book(Long id, String title, String author, String summary, String coverUrl, String contentUrl) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.summary = summary;
        this.coverUrl = coverUrl;
        this.contentUrl = contentUrl;
    }

    // Getter和Setter方法
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getContentUrl() {
        return contentUrl;
    }

    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }

    // toString方法，方便打印和调试
    @Override
    public String toString() {
        return "Book{" +
               "id=" + id +
               ", title='" + title + '\'' +
               ", author='" + author + '\'' +
               ", summary='" + summary + '\'' +
               ", coverUrl='" + coverUrl + '\'' +
               ", contentUrl='" + contentUrl + '\'' +
               '}';
    }
}

