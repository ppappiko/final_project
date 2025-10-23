package com.example.myapplication.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import java.util.Map;

public interface UserService {

    // 회원가입 요청
    @POST("/register")
    Call<Map<String, String>> registerUser(@Body User user);

    // 로그인 요청
    @POST("/login")
    Call<Map<String, String>> loginUser(@Body Map<String, String> credentials);

    @GET("/test")
    Call<String> testConnection();

    @POST("/verify/email-request")
    Call<Map<String, String>> requestEmailVerification(@Body Map<String, String> email);

    @POST("/verify/email-confirm")
    Call<Map<String, String>> confirmEmailVerification(@Body Map<String, String> verificationData);

    @POST("/check-nickname")
    Call<Map<String, String>> checkNickname(@Body Map<String, String> nickname);
}
