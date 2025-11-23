package com.example.myapplication.community;

import com.google.gson.annotations.SerializedName;

public class Comment {
    @SerializedName("content")
    private String content;
    @SerializedName("author")
    private String author;

    @SerializedName("createdAt")
    private String timestamp;

    public String getContent() { return content; }
    public String getAuthor() { return author; }
    public String getTimestamp() { return timestamp; } // 필요하면 포맷팅 메서드 추가
}
