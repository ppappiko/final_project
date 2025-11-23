package com.example.myapplication.User;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class UserDto implements Serializable {

    // 서버에서 보내주는 JSON 키 값("email")과 매핑
    @SerializedName("email")
    private String email;

    // 서버의 "username" (닉네임/이름)
    @SerializedName("username")
    private String username;

    // 비밀번호 변경 시에만 사용 (서버로 보낼 때)
    @SerializedName("password")
    private String password;

    // --- 생성자 ---

    // 1. 기본 생성자 (Retrofit/Gson이 사용)
    public UserDto() {
    }

    // 2. 데이터 전송용 생성자 (수정 요청 보낼 때 편하게 사용)
    public UserDto(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // --- Getter / Setter ---

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

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
