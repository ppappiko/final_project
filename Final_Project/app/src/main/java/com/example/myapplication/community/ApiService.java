package com.example.myapplication.community;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("posts") // "posts"는 서버의 엔드포인트 경로입니다. 실제 경로에 맞게 수정해야 합니다.
    Call<Void> createPost(@Body PostRequest postRequest);
}
