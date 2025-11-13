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
     * [수정됨] POST: 문제 생성 요청 (인증 토큰 추가)
     */
    @POST("/ai/generate-questions")
    Call<Map<String, List<Question>>> generateQuestions(
            @Header("Authorization") String authToken, // ⬅️ (이 부분은 이미 수정했었음)
            @Body Map<String, String> requestBody
    );

    /**
     * [수정됨] GET: 저장된 요약본 조회 (인증 토큰 추가)
     */
    @GET("/ai/summary/{recordingKey}")
    Call<Map<String, String>> getSummary(
            @Header("Authorization") String authToken, // ⬅️ 2. 인증 토큰 파라미터 추가
            @Path("recordingKey") String recordingKey
    );

    @POST("/ai/ai/summarize")
    Call<Map<String, String>> summarizeText(
            @Header("Authorization") String authToken, // ⬅️ 3. 인증 토큰 파라미터 추가
            @Body Map<String, String> requestData
    );
}
