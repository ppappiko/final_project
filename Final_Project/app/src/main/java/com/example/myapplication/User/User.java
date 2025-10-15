package com.example.myapplication.User;

public class User {

    // 서버의 User 모델과 변수 이름을 '정확히' 일치시켜야 합니다.
    private String username;
    private String password;

    // 생성자 (Constructor)
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getter와 Setter (선택 사항이지만 추가하는 것이 좋습니다)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}