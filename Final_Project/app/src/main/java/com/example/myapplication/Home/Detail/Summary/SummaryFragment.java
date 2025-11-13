package com.example.myapplication.Home.Detail.Summary;

import android.content.Context;
import android.content.Intent; // ⬅️ 1. 임포트 추가
import android.content.SharedPreferences; // ⬅️ 2. 임포트 추가
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
import com.example.myapplication.User.LoginActivity; // ⬅️ 3. 임포트 추가
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

    // UI 요소
    private Button btnSummarize;
    private ProgressBar progressBar;
    private TextView tvSummaryResult;
    private ScrollView scrollViewSummary;

    // 데이터
    private UserService userService;
    private String transcriptText = null; // 원본 텍스트
    private String recordingKey = "";   // 파일 이름 키
    private String authToken = null; // ⬅️ 4. 로그인 토큰을 저장할 변수

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String audioFilePath = null;
        if (getArguments() != null) {
            audioFilePath = getArguments().getString("filePath"); // DetailsFragment에서 .m4a 경로를 받음
        }

        if (audioFilePath == null || audioFilePath.isEmpty()) {
            Log.e(TAG, "Bundle에서 filePath를 받지 못했습니다.");
            return;
        }

        // 1. 고유 키(파일 이름) 생성
        recordingKey = new File(audioFilePath).getName();

        // 2. .txt 파일 경로 생성
        String transcriptFilePath = audioFilePath.replaceAll("\\.m4a$", ".txt");

        // ▼▼▼ (ANR 해결) 파일 읽기를 백그라운드 스레드에서 실행 ▼▼▼
        new Thread(() -> {
            String text = readTextFromFile(transcriptFilePath);

            // 3. 파일 읽기 완료 후, 메인 스레드에서 UI 작업(API 호출) 시작
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    transcriptText = text; // 읽어온 텍스트 저장
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
                    // 4. 텍스트 읽기 성공 -> (500 오류 해결) 토큰 로드
                    if (loadAuthToken()) {
                        // 5. 토큰 로드 성공 -> 서버에 저장된 요약본이 있는지 확인
                        checkSummaryOnServer();
                    } else {
                        // 6. 토큰 로드 실패 -> 로그인 화면으로
                        if(progressBar != null) progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "로그인 정보가 필요합니다.", Toast.LENGTH_SHORT).show();
                        goToLogin();
                    }
                });
            }
        }).start();
        // ▲▲▲ (ANR 해결 완료) ▲▲▲
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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

        // '요약하기' 버튼 리스너
        btnSummarize.setOnClickListener(v -> {
            requestSummaryFromServer(); // 4. 버튼 클릭 시 서버에 요약 생성 요청
        });

        // 1. 화면이 켜지면, 일단 로딩바를 보여줌 (파일 읽기가 끝날 때까지)
        progressBar.setVisibility(View.VISIBLE);
        btnSummarize.setVisibility(View.GONE);
        scrollViewSummary.setVisibility(View.GONE);

        // (파일 읽기와 토큰 로드가 완료되면 onCreate -> runOnUiThread에서 checkSummaryOnServer()가 호출됨)
    }

    /** [신규] 1. SharedPreferences에서 토큰을 가져오는 헬퍼 메소드 */
    private boolean loadAuthToken() {
        if (getContext() == null) return false;

        // 5. [500 오류 해결] LoginActivity와 동일한 키 사용
        SharedPreferences prefs = getContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);

        if (token == null || token.isEmpty()) {
            return false;
        }

        authToken = "Bearer " + token;
        return true;
    }

    /** [수정됨] 2. 서버에 저장된 요약본이 있는지 확인 (토큰 전송) */
    private void checkSummaryOnServer() {
        if (recordingKey.isEmpty() || authToken == null) {
            progressBar.setVisibility(View.GONE);
            btnSummarize.setVisibility(View.VISIBLE);
            return;
        }

        // 6. [500 오류 해결] 수정된 getSummary (토큰 포함) 호출
        Call<Map<String, String>> call = userService.getSummary(authToken, recordingKey);
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
                    // 3-B. 요약본이 DB에 없음 (404 Not Found) -> '요약하기' 버튼 표시
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

    /** [수정됨] 3. '요약하기' 버튼 클릭 시, 서버에 요약 생성을 요청 (토큰 전송) */
    private void requestSummaryFromServer() {
        if (transcriptText == null || transcriptText.isEmpty()) {
            Toast.makeText(getContext(), "원본 텍스트가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 7. [500 오류 해결] 버튼 클릭 시에도 토큰 확인
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

        // 8. [500 오류 해결] 수정된 summarizeText (토큰 포함) 호출
        Call<Map<String, String>> call = userService.summarizeText(authToken, requestBody);
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    // 3-A. 요약 성공
                    String summary = response.body().get("summary");
                    tvSummaryResult.setText(summary);
                    scrollViewSummary.setVisibility(View.VISIBLE);
                } else {
                    // 3-B. 서버 500 오류 (NPE)가 여기서 잡힙니다.
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

    /** [수정됨] 4. 파일 읽기 헬퍼 메소드 (null 반환) */
    private String readTextFromFile(String filePath) {
        if (filePath == null) return null;
        File file = new File(filePath);
        if (!file.exists()) {
            Log.e("FileReadError", "파일이 존재하지 않습니다: " + filePath);
            return null; // ⬅️ 파일이 없으면 null 반환
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
            return null; // ⬅️ 읽기 실패 시 null 반환
        }
        return text.toString();
    }

    /** [신규] 5. 로그인 화면으로 이동 */
    private void goToLogin() {
        if (getActivity() == null) return;
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}