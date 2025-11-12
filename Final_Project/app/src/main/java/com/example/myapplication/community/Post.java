package com.example.myapplication.community;

// 게시물 데이터 모델
public class Post {
    private String title;
    private String contentPreview;
    private String author;
    private String timestamp;

    public Post(String title, String contentPreview, String author, String timestamp) {
        this.title = title;
        this.contentPreview = contentPreview;
        this.author = author;
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public String getContentPreview() {
        return contentPreview;
    }

    public String getAuthor() {
        return author;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
