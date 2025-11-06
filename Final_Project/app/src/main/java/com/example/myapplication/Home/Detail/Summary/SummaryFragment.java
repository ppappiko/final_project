package com.example.myapplication.Home.Detail.Summary; // 패키지 경로는 실제 위치에 맞게 수정하세요

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

    // UI 요소 변수
    private Button btnSummarize;
    private ProgressBar progressBar;
    private TextView tvSummaryResult;
    private ScrollView scrollViewSummary;

    // 네트워크 및 데이터 변수
    private UserService userService;
    private String transcriptText = ""; // 원본 텍스트 (요약 요청 시 사용)
    private String recordingKey = "";   // 파일 이름 (DB 조회를 위한 고유 키)

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. DetailsFragment로부터 전달받은 Bundle에서 파일 경로를 꺼냅니다.
        if (getArguments() != null) {
            String audioFilePath = getArguments().getString("filePath");
            if (audioFilePath != null && !audioFilePath.isEmpty()) {

                // 2. 녹음 파일 이름(예: "녹음_123.m4a")을 고유 키로 사용합니다.
                recordingKey = new File(audioFilePath).getName();

                // 3. STT 텍스트 파일(.txt)의 경로를 생성합니다.
                String transcriptFilePath = audioFilePath.replaceAll("\\.m4a$", ".txt");

                // 4. STT 텍스트 파일을 읽어와 변수에 저장합니다.
                transcriptText = readTextFromFile(transcriptFilePath);
            } else {
                Log.e(TAG, "Bundle에서 filePath를 받지 못했습니다.");
            }
        } else {
            Log.e(TAG, "Bundle이 null입니다.");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // XML 레이아웃 파일을 화면으로 만듭니다.
        return inflater.inflate(R.layout.fragment_summary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // UI 요소 초기화
        btnSummarize = view.findViewById(R.id.btn_summarize);
        progressBar = view.findViewById(R.id.progress_bar);
        tvSummaryResult = view.findViewById(R.id.tv_summary_result);
        scrollViewSummary = view.findViewById(R.id.scroll_view_summary);

        // Retrofit 서비스 초기화
        userService = ApiClient.getClient().create(UserService.class);

        // '요약하기' 버튼 리스너 (먼저 설정)
        btnSummarize.setOnClickListener(v -> {
            requestSummaryFromServer(); // 4. 버튼 클릭 시 서버에 요약 생성 요청
        });

        // 1. 화면이 켜지면, 일단 로딩바를 보여줌
        progressBar.setVisibility(View.VISIBLE);
        btnSummarize.setVisibility(View.GONE);
        scrollViewSummary.setVisibility(View.GONE);

        // 2. DB에 저장된 요약본이 있는지 서버에 확인
        checkSummaryOnServer();
    }

    /** 1. 서버에 저장된 요약본이 있는지 확인하는 메소드 */
    private void checkSummaryOnServer() {
        if (recordingKey.isEmpty()) {
            Log.e(TAG, "recordingKey가 비어있어 조회를 스킵합니다.");
            progressBar.setVisibility(View.GONE);
            btnSummarize.setVisibility(View.VISIBLE); // 조회할 키가 없으니 '요약하기' 버튼 표시
            return;
        }

        Call<Map<String, String>> call = userService.getSummary(recordingKey);
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    // 3-A. 요약본이 DB에 있음 (200 OK)
                    String summary = response.body().get("summary");
                    tvSummaryResult.setText(summary);
                    scrollViewSummary.setVisibility(View.VISIBLE);
                    btnSummarize.setVisibility(View.GONE);
                } else if (response.code() == 404) {
                    // 3-B. 요약본이 DB에 없음 (404 Not Found)
                    Log.d(TAG, "저장된 요약본이 없습니다. '요약하기' 버튼을 표시합니다.");
                    btnSummarize.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getContext(), "요약본 조회 중 오류 발생: " + response.code(), Toast.LENGTH_SHORT).show();
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

    /** 2. '요약하기' 버튼 클릭 시, 서버에 요약 생성을 요청하는 메소드 */
    private void requestSummaryFromServer() {
        if (transcriptText.isEmpty()) {
            Toast.makeText(getContext(), "원본 텍스트가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSummarize.setVisibility(View.GONE);

        HashMap<String, String> requestBody = new HashMap<>();
        requestBody.put("text", transcriptText);
        requestBody.put("recordingKey", recordingKey); // DB에 저장할 키(파일 이름) 전송

        Call<Map<String, String>> call = userService.summarizeText(requestBody);
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    // 요약 성공 및 DB 저장 완료
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

    /** 3. 파일 경로를 받아 텍스트 내용을 읽어오는 헬퍼 메소드 */
    private String readTextFromFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            Log.e("FileReadError", "파일이 존재하지 않습니다: " + filePath);
            return ""; // 파일이 없으면 빈 문자열 반환
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
        }
        return text.toString();
    }
}