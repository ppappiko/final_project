package com.example.myapplication.community;

public class PostRequest {
    private final String category;
    private final String title;
    private final String content;

    public PostRequest(String category, String title, String content) {
        this.category = category;
        this.title = title;
        this.content = content;
    }

    // Getter 메소드들은 Retrofit이 JSON으로 변환할 때 필요할 수 있습니다.
    public String getCategory() {
        return category;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
