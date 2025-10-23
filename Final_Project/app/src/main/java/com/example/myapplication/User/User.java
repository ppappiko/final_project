package com.example.myapplication.User;

public class User {

    // 서버의 User 모델과 변수 이름을 '정확히' 일치시켜야 합니다.
    private String username;
    private String password;
    private String name;
    private String phone;
    private String email;

    // 생성자 (Constructor)
    public User(String username, String password, String name,String phone, String email) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.email = email;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}