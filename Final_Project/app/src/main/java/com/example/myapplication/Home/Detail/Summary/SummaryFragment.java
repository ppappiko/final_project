package com.example.myapplication.Home.Detail.Summary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.ApiClient;
import com.example.myapplication.R;
import com.example.myapplication.User.LoginActivity;
import com.example.myapplication.User.UserService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SummaryFragment extends Fragment {

    private static final String TAG = "SummaryFragment";

    private Button btnSummarize;
    private ProgressBar progressBar;
    private TextView tvSummaryResult;
    private ScrollView scrollViewSummary;

    private UserService userService;
    private String transcriptText = null;
    private String recordingKey = "";
    private String authToken = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String audioFilePath = null;
        if (getArguments() != null) {
            audioFilePath = getArguments().getString("filePath");
        }

        if (audioFilePath == null || audioFilePath.isEmpty()) {
            Log.e(TAG, "Bundle에서 filePath를 받지 못했습니다.");
            return;
        }

        recordingKey = new File(audioFilePath).getName();

        String transcriptFilePath = audioFilePath.replaceAll("\\.m4a$", ".txt");

        new Thread(() -> {
            String text = readTextFromFile(transcriptFilePath);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    transcriptText = text;
                    if (transcriptText == null || transcriptText.isEmpty()) {
                        Log.e(TAG, ".txt 파일 읽기 실패 또는 파일이 비어있음");
                        if(progressBar != null) progressBar.setVisibility(View.GONE);
                        if(btnSummarize != null) {
                            btnSummarize.setVisibility(View.VISIBLE);
                            btnSummarize.setEnabled(false);
                            btnSummarize.setText("원본 텍스트 없음");
                        }
                        return;
                    }
                    if (loadAuthToken()) {
                        checkSummaryOnServer();
                    } else {
                        if(progressBar != null) progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "로그인 정보가 필요합니다.", Toast.LENGTH_SHORT).show();
                        goToLogin();
                    }
                });
            }
        }).start();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_summary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnSummarize = view.findViewById(R.id.btn_summarize);
        progressBar = view.findViewById(R.id.progress_bar);
        tvSummaryResult = view.findViewById(R.id.tv_summary_result);
        scrollViewSummary = view.findViewById(R.id.scroll_view_summary);

        // ApiClient의 공개 메소드를 사용하여 UserService 초기화
        userService = ApiClient.getUserService();

        btnSummarize.setOnClickListener(v -> {
            requestSummaryFromServer();
        });

        progressBar.setVisibility(View.VISIBLE);
        btnSummarize.setVisibility(View.GONE);
        scrollViewSummary.setVisibility(View.GONE);
    }

    private boolean loadAuthToken() {
        if (getContext() == null) return false;

        SharedPreferences prefs = getContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);

        if (token == null || token.isEmpty()) {
            return false;
        }

        authToken = "Bearer " + token;
        return true;
    }

    private void checkSummaryOnServer() {
        if (recordingKey.isEmpty() || authToken == null) {
            progressBar.setVisibility(View.GONE);
            btnSummarize.setVisibility(View.VISIBLE);
            return;
        }

        Call<Map<String, String>> call = userService.getSummary(authToken, recordingKey);
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    String summary = response.body().get("summary");
                    tvSummaryResult.setText(summary);
                    scrollViewSummary.setVisibility(View.VISIBLE);
                    btnSummarize.setVisibility(View.GONE);
                } else if (response.code() == 404) {
                    btnSummarize.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getContext(), "요약본 조회 오류: " + response.code(), Toast.LENGTH_SHORT).show();
                    btnSummarize.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnSummarize.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void requestSummaryFromServer() {
        if (transcriptText == null || transcriptText.isEmpty()) {
            Toast.makeText(getContext(), "원본 텍스트가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (authToken == null || !loadAuthToken()) {
            Toast.makeText(getContext(), "로그인 정보가 만료되었습니다.", Toast.LENGTH_SHORT).show();
            goToLogin();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSummarize.setVisibility(View.GONE);

        HashMap<String, String> requestBody = new HashMap<>();
        requestBody.put("text", transcriptText);
        requestBody.put("recordingKey", recordingKey);

        Call<Map<String, String>> call = userService.summarizeText(authToken, requestBody);
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    String summary = response.body().get("summary");
                    tvSummaryResult.setText(summary);
                    scrollViewSummary.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getContext(), "요약 생성 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                    btnSummarize.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnSummarize.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String readTextFromFile(String filePath) {
        if (filePath == null) return null;
        File file = new File(filePath);
        if (!file.exists()) {
            Log.e("FileReadError", "파일이 존재하지 않습니다: " + filePath);
            return null;
        }

        StringBuilder text = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("FileReadError", "파일을 읽을 수 없습니다: " + filePath);
            return null;
        }
        return text.toString();
    }

    private void goToLogin() {
        if (getActivity() == null) return;
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
