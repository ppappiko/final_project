package com.example.myapplication;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ApiClient {

    // ⚠️ 중요: 이 주소는 테스트 환경에 맞게 변경해야 합니다.
    // 1. PC의 내부 IP 주소 사용: "http://192.168.x.x:8080/"
    // 2. ngrok 사용: "https://xxxx-xxxx.ngrok-free.app/"
    private static final String BASE_URL = "http://222.114.74.217:8080/"; // ⬅️ 본인 환경에 맞게 수정!

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }


}