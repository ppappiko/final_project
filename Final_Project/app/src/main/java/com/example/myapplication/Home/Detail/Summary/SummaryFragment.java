package com.example.myapplication.Home.Detail.Summary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.example.myapplication.Home.Detail.DetailsFragment;
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

    // UI
    private Button btnSummarize;
    private ProgressBar progressBar;
    private TextView tvSummaryResult;
    private ScrollView scrollViewSummary;

    // Data
    private UserService userService;
    private String transcriptText = null;
    private String recordingKey = "";
    private String authToken = null;
    private boolean isShowingResult = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String audioFilePath = null;
        if (getArguments() != null) {
            audioFilePath = getArguments().getString("filePath");
        }

        if (audioFilePath != null && !audioFilePath.isEmpty()) {
            recordingKey = new File(audioFilePath).getName();
            String transcriptFilePath = audioFilePath.replaceAll("\\.m4a$", ".txt");

            new Thread(() -> {
                String text = readTextFromFile(transcriptFilePath);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        transcriptText = text;
                        if (transcriptText != null && !transcriptText.isEmpty()) {
                            if (loadAuthToken()) {
                                checkSummaryOnServer();
                            }
                        } else {
                            setInitialState(true);
                        }
                    });
                }
            }).start();
        } else {
            setInitialState(true);
        }
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

        userService = ApiClient.getClient().create(UserService.class);

        btnSummarize.setOnClickListener(v -> {
            if (transcriptText != null && !transcriptText.isEmpty()) {
                requestSummaryFromServer();
            }
        });

        setInitialState(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateParentActionButtons();
    }

    public void handleRegenerateRequest() {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
            .setTitle("AI 요약 다시 받기")
            .setMessage("기존 요약 내용은 사라지고 새로 생성됩니다.\n계속하시겠습니까?")
            .setPositiveButton("확인", (dialog, which) -> requestSummaryFromServer())
            .setNegativeButton("취소", null)
            .show();
    }

    private void requestSummaryFromServer() {
        if (transcriptText == null || transcriptText.isEmpty()) {
            Toast.makeText(getContext(), "요약할 텍스트가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!loadAuthToken()) return;

        setLoadingState();

        HashMap<String, String> requestBody = new HashMap<>();
        requestBody.put("text", transcriptText);
        requestBody.put("recordingKey", recordingKey);

        Call<Map<String, String>> call = userService.summarizeText(authToken, requestBody);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, String>> call, @NonNull Response<Map<String, String>> response) {
                if (getActivity() == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    String summary = response.body().get("summary");
                    displayResult(summary);
                    Toast.makeText(getContext(), "요약이 갱신되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    setInitialState(true);
                    Toast.makeText(getContext(), "요약 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, String>> call, @NonNull Throwable t) {
                if (getActivity() == null) return;
                setInitialState(true);
                Toast.makeText(getContext(), "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkSummaryOnServer() {
        if (!loadAuthToken()) return;

        Call<Map<String, String>> call = userService.getSummary(authToken, recordingKey);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, String>> call, @NonNull Response<Map<String, String>> response) {
                if (getActivity() == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    String summary = response.body().get("summary");
                    displayResult(summary);
                } else {
                    setInitialState(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, String>> call, @NonNull Throwable t) {
                if (getActivity() != null) setInitialState(false);
            }
        });
    }

    private void setInitialState(boolean noText) {
        isShowingResult = false;
        btnSummarize.setVisibility(View.VISIBLE);
        btnSummarize.setText(noText ? "원본 텍스트 없음" : "요약하기");
        btnSummarize.setEnabled(!noText);
        scrollViewSummary.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        updateParentActionButtons();
    }

    private void setLoadingState() {
        isShowingResult = false;
        progressBar.setVisibility(View.VISIBLE);
        btnSummarize.setVisibility(View.GONE);
        scrollViewSummary.setVisibility(View.GONE);
        updateParentActionButtons();
    }

    private void displayResult(String summary) {
        isShowingResult = true;
        tvSummaryResult.setText(summary);
        progressBar.setVisibility(View.GONE);
        btnSummarize.setVisibility(View.GONE);
        scrollViewSummary.setVisibility(View.VISIBLE);
        updateParentActionButtons();
    }

    public boolean isShowingResult() {
        return isShowingResult;
    }

    private void updateParentActionButtons() {
        if (getParentFragment() instanceof DetailsFragment) {
            ((DetailsFragment) getParentFragment()).updateActionButtonsVisibility();
        }
    }

    private boolean loadAuthToken() {
        if (getContext() == null) return false;
        SharedPreferences prefs = getContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);

        if (token == null || token.isEmpty()) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return false;
        }
        authToken = "Bearer " + token;
        return true;
    }

    private String readTextFromFile(String filePath) {
        if (filePath == null) return null;
        File file = new File(filePath);
        if (!file.exists()) return null;

        StringBuilder text = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) text.append(line).append('\n');
        } catch (IOException e) {
            return null;
        }
        return text.toString();
    }
}