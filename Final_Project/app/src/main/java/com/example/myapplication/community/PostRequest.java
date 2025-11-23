package com.example.myapplication.community;

public class PostRequest {
    private String title;
    private String content;
    private String category; // "FREE", "QNA", "INFO"

    private String attachedQuizKey;

    public PostRequest(String title, String content, String category, String attachedQuizKey) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.attachedQuizKey = attachedQuizKey;
    }
}