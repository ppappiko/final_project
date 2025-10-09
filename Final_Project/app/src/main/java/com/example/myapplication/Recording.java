package com.example.myapplication;

// Recording.java
public class Recording {
    private String title;
    private String date;
    private int problemCount;

    // 생성자
    public Recording(String title, String date, int problemCount) {
        this.title = title;
        this.date = date;
        this.problemCount = problemCount;
    }

    // Getter 메소드들
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public int getProblemCount() { return problemCount; }
}
