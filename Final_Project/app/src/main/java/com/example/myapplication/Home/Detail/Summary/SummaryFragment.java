package com.example.myapplication.Home.Detail.Summary;

import android.app.AlertDialog;
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

    private static final String TAG = "SummaryFragment";

    // UI
    private Button btnSummarize;
    private ProgressBar progressBar;
    private TextView tvSummaryResult;
    private ScrollView scrollViewSummary;

    // Data
    private UserService userService;
    private String transcriptText = null; // ★ 원본 텍스트 (메모리에 보관) ★
    private String recordingKey = "";
    private String authToken = null;

    private boolean isShowingResult = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. 파일 경로 가져오기
        String audioFilePath = null;
        if (getArguments() != null) {
            audioFilePath = getArguments().getString("filePath");
        }

        if (audioFilePath != null && !audioFilePath.isEmpty()) {
            recordingKey = new File(audioFilePath).getName();
            String transcriptFilePath = audioFilePath.replaceAll("\\.m4a$", ".txt");

            // 2. [최초 1회] 파일 읽기 (백그라운드)
            new Thread(() -> {
                String text = readTextFromFile(transcriptFilePath);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // 읽은 내용을 변수에 저장해둡니다. (이후엔 이걸 계속 재사용)
                        transcriptText = text;

                        if (transcriptText != null && !transcriptText.isEmpty()) {
                            // 텍스트가 있으면 -> 서버에 기존 요약본이 있는지 확인(GET)
                            if (loadAuthToken()) {
                                checkSummaryOnServer();
                            }
                        } else {
                            if (btnSummarize != null) {
                                btnSummarize.setText("원본 텍스트 없음");
                                btnSummarize.setEnabled(false);
                            }
                        }
                    });
                }
            }).start();
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

        // UI 초기화
        btnSummarize = view.findViewById(R.id.btn_summarize);
        progressBar = view.findViewById(R.id.progress_bar);
        tvSummaryResult = view.findViewById(R.id.tv_summary_result);
        scrollViewSummary = view.findViewById(R.id.scroll_view_summary);

        userService = ApiClient.getClient().create(UserService.class);

        setLoadingState(false); // 초기 상태

        // [요약하기] 버튼 (처음 생성용)
        btnSummarize.setOnClickListener(v -> {
            if (transcriptText != null && !transcriptText.isEmpty()) {
                requestSummaryFromServer();
            }
        });
    }

    /**
     * ▼▼▼ [상단 툴바 버튼 클릭 시 호출됨] ▼▼▼
     */
    public void handleRegenerateRequest() {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("AI 요약 다시 받기")
                .setMessage("기존 요약 내용은 사라지고 새로 생성됩니다.\n계속하시겠습니까?")
                .setPositiveButton("확인", (dialog, which) -> {
                    // ★ 파일을 다시 읽지 않고, 메모리에 있는 transcriptText를 바로 사용합니다. ★
                    requestSummaryFromServer();
                })
                .setNegativeButton("취소", null)
                .show();
    }

    /** * 서버에 요약 생성 요청 (POST)
     * - 파일을 읽지 않고 멤버 변수(transcriptText)를 사용함
     */
    private void requestSummaryFromServer() {
        // 1. 메모리에 텍스트가 있는지 확인
        if (transcriptText == null || transcriptText.isEmpty()) {
            Toast.makeText(getContext(), "요약할 텍스트가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!loadAuthToken()) return;

        setLoadingState(true); // 로딩 시작

        // 2. 서버로 보낼 데이터 준비
        HashMap<String, String> requestBody = new HashMap<>();
        requestBody.put("text", transcriptText); // ★ 메모리에 있는 텍스트 사용 ★
        requestBody.put("recordingKey", recordingKey);

        // 3. 서버 요청 (POST)
        Call<Map<String, String>> call = userService.summarizeText(authToken, requestBody);
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (getActivity() == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    String summary = response.body().get("summary");
                    displayResult(summary); // 결과 표시
                    Toast.makeText(getContext(), "요약이 갱신되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    setLoadingState(false);
                    Toast.makeText(getContext(), "요약 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                if (getActivity() == null) return;
                setLoadingState(false);
                Toast.makeText(getContext(), "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** 서버에 저장된 요약본 조회 (GET) */
    private void checkSummaryOnServer() {
        if (!loadAuthToken()) return;

        Call<Map<String, String>> call = userService.getSummary(authToken, recordingKey);
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (getActivity() == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    // DB에 있으면 보여줌
                    String summary = response.body().get("summary");
                    displayResult(summary);
                } else {
                    // 없으면 버튼 대기 상태
                    setLoadingState(false);
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                if (getActivity() != null) setLoadingState(false);
            }
        });
    }

    // --- UI 상태 관리 ---

    private void setLoadingState(boolean isLoading) {
        if (getActivity() == null) return;

        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);

        if (isLoading) {
            scrollViewSummary.setVisibility(View.GONE);
            btnSummarize.setVisibility(View.GONE);
        } else {
            // 로딩 끝났는데 결과가 없으면 -> 요약하기 버튼 표시
            if (!isShowingResult) {
                btnSummarize.setVisibility(View.VISIBLE);
                scrollViewSummary.setVisibility(View.GONE);
            }
        }
        updateParentMenu();
    }

    private void displayResult(String summary) {
        if (getActivity() == null) return;

        isShowingResult = true;
        tvSummaryResult.setText(summary);

        progressBar.setVisibility(View.GONE);
        btnSummarize.setVisibility(View.GONE);
        scrollViewSummary.setVisibility(View.VISIBLE);

        updateParentMenu();
    }

    public boolean isShowingResult() {
        return isShowingResult;
    }

    private void updateParentMenu() {
        if (getParentFragment() instanceof DetailsFragment) {
            getActivity().invalidateOptionsMenu();
        }
    }

    // --- 유틸리티 ---

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