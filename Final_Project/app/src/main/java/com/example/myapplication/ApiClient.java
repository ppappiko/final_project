package com.example.myapplication;

import com.example.myapplication.User.UserService;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit retrofit = null;
    private static ApiService apiService = null;
    private static UserService userService = null; // UserService 인스턴스 추가


    /**
     * 타임아웃을 60초로 늘린 OkHttpClient 객체를 생성합니다.
     */
    private static OkHttpClient createOkHttpClient() {
        return new OkHttpClient.Builder()
                // 1. 서버 연결 시간 (60초)
                .connectTimeout(300, TimeUnit.SECONDS)
                // 2. 서버가 데이터를 읽는 시간 (60초) - AI 응답 대기
                .readTimeout(300, TimeUnit.SECONDS)
                // 3. 앱이 서버로 데이터를 쓰는 시간 (60초)
                .writeTimeout(300, TimeUnit.SECONDS)
                .build();
    }

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
                    OkHttpClient okHttpClient = createOkHttpClient();



                    retrofit = new Retrofit.Builder()
                            .baseUrl(BuildConfig.BASE_URL) // Use the URL from BuildConfig
                            .client(okHttpClient)
                            .addConverterFactory(ScalarsConverterFactory.create())
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService() {
        if (apiService == null) {
            apiService = getClient().create(ApiService.class);
        }
        return apiService;
    }

    // UserService를 반환하는 공개 메소드 추가
    public static UserService getUserService() {
        if (userService == null) {
            userService = getClient().create(UserService.class);
        }
        return userService;
    }
}
