package com.example.myapplication;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ApiClient {

    // ⚠️ 중요: 이 주소는 테스트 환경에 맞게 변경해야 합니다.
    // 1. PC의 내부 IP 주소 사용: "http://192.168.x.x:8080/"
    // 2. ngrok 사용: "https://xxxx-xxxx.ngrok-free.app/"
    private static final String BASE_URL = "http://175.204.21.19:8080/"; // ⬅️ 본인 환경에 맞게 수정!

    private static volatile Retrofit retrofit = null;

    public static Retrofit getClient() {
        // Use double-checked locking for thread safety.
        if (retrofit == null) {
            synchronized (ApiClient.class) {
                if (retrofit == null) {
                    // Create a logging interceptor to see request and response logs.
                    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                    // Show logs only in debug builds.
                    if (BuildConfig.DEBUG) {
                        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                    } else {
                        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
                    }

                    // Create a custom OkHttpClient and add the logging interceptor.
                    OkHttpClient okHttpClient = new OkHttpClient.Builder()
                            .addInterceptor(loggingInterceptor)
                            .build();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(BuildConfig.BASE_URL) // Use the URL from BuildConfig
                            .client(okHttpClient)
                            .addConverterFactory(ScalarsConverterFactory.create())
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return retrofit;
    }
}
