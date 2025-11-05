package com.example.myapplication.User;

import com.example.myapplication.Home.Detail.Question.Question;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

import java.util.List;
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

    /**
     * 문제 생성 요청
     * 응답이 {"questions": [ ... ]} 형태이므로
     * Map<String, List<Question>>으로 받습니다.
     */
    @POST("/ai/generate-questions")
    Call<Map<String, List<Question>>> generateQuestions(
            @Header("Authorization") String authToken, // ⬅️ 인증 토큰을 헤더에 추가
            @Body Map<String, String> text
    );

    /** GET: 저장된 요약본 조회 */
    @GET("/ai/summary/{recordingKey}")
    Call<Map<String, String>> getSummary(@Path("recordingKey") String recordingKey);

    @POST("/ai/ai/summarize")
    Call<Map<String, String>> summarizeText(@Body Map<String, String> requestData);
}
