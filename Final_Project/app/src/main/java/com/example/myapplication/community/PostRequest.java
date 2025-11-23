package com.example.myapplication.community;

public class PostRequest {
    private String title;
    private String content;
    private String category; // "FREE", "QNA", "INFO"

    public PostRequest(String title, String content, String category) {
        this.title = title;
        this.content = content;
        this.category = category;
    }
}