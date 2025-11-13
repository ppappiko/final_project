package com.example.myapplication.Home.Detail.Transcript;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

// STT 서버 API를 위한 Retrofit 인터페이스
public interface TranscriptApiService {
    @Multipart
    @POST("transcribe/")
    Call<ResponseBody> transcribeAudio(
            @Part MultipartBody.Part audio_file
    );
}
