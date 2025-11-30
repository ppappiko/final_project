package com.example.myapplication;

import com.example.myapplication.User.UserService;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ApiClient {

    // ▼▼▼ (1. 여기에 서버 주소를 다시 적어주세요!) ▼▼▼
    // (안드로이드 에뮬레이터에서 로컬 서버 접속 시 10.0.2.2 사용)
    private static final String BASE_URL = "http://172.30.1.13:8080/";

    private static volatile Retrofit retrofit = null;
    private static ApiService apiService = null;
    private static UserService userService = null;

    /**
     * 타임아웃(300초)과 로깅 설정이 된 OkHttpClient 생성
     */
    private static OkHttpClient createOkHttpClient() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        if (BuildConfig.DEBUG) {
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        }

        return new OkHttpClient.Builder()
                .connectTimeout(300, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .writeTimeout(300, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .build();
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            synchronized (ApiClient.class) {
                if (retrofit == null) {
                    OkHttpClient okHttpClient = createOkHttpClient();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL) // ⬅️ (2. BuildConfig 대신 위에서 선언한 String 변수 사용)
                            .client(okHttpClient)
                            .addConverterFactory(ScalarsConverterFactory.create())
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return retrofit;
    }

    public static ApiService getApiService() {
        if (apiService == null) {
            apiService = getClient().create(ApiService.class);
        }
        return apiService;
    }

    public static UserService getUserService() {
        if (userService == null) {
            userService = getClient().create(UserService.class);
        }
        return userService;
    }
}