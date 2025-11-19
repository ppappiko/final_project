package com.example.myapplication;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

    // 글과 파일을 함께 업로드
    @Multipart
    @POST("community/posts") // 실제 서버의 게시물 생성 주소
    Call<Void> createPost(
        @Part("post") RequestBody postData, // JSON 데이터를 담을 파트
        @Part MultipartBody.Part audioFile       // 오디오 파일을 담을 파트
    );

    // 글만 업로드
    @POST("community/posts")
    Call<Void> createPost(@Body RequestBody postData);

}
