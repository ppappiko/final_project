package com.example.myapplication.Home.Detail.Transcript;

import com.example.myapplication.BuildConfig;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class SttApiClient {

    private static final String STT_BASE_URL = "http://34.50.41.99:8000/";
    private static volatile Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            synchronized (SttApiClient.class) {
                if (retrofit == null) {
                    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                    if (BuildConfig.DEBUG) {
                        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                    } else {
                        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
                    }

                    OkHttpClient okHttpClient = new OkHttpClient.Builder()
                            .connectTimeout(300, TimeUnit.SECONDS) // 타임아웃 5분으로 증가
                            .writeTimeout(300, TimeUnit.SECONDS)   // 타임아웃 5분으로 증가
                            .readTimeout(300, TimeUnit.SECONDS)    // 타임아웃 5분으로 증가
                            .addInterceptor(loggingInterceptor)
                            .build();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(STT_BASE_URL)
                            .client(okHttpClient)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return retrofit;
    }
}
