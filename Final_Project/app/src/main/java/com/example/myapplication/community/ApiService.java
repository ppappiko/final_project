package com.example.myapplication.community;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    // 게시글 목록 조회 (카테고리별)
    // 예: GET /api/posts/FREE
    @GET("/api/posts/{category}")
    Call<List<Post>> getPosts(
            @Header("Authorization") String token,
            @Path("category") String category
    );

    // 게시글 작성
    // 예: POST /api/posts
    @POST("/api/posts")
    Call<Void> createPost(
            @Header("Authorization") String token,
            @Body PostRequest postRequest
    );

    // 댓글 목록 조회
    @GET("/api/posts/{postId}/comments")
    Call<List<Comment>> getComments(
            @Header("Authorization") String token, // ⬅️ 파라미터 추가
            @Path("postId") Long postId
    );

    // 댓글 작성 (Map으로 간단히 전송)
    @POST("/api/posts/{postId}/comments")
    Call<Void> createComment(
            @Header("Authorization") String token,
            @Path("postId") Long postId,
            @Body java.util.Map<String, String> content
    );
}