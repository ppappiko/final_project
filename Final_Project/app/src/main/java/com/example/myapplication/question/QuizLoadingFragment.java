package com.example.myapplication.question;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.ApiClient;
import com.example.myapplication.Home.Detail.Question.Question;
import com.example.myapplication.R;
import com.example.myapplication.User.LoginActivity;
import com.example.myapplication.User.UserService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuizLoadingFragment extends Fragment {

    private UserService userService;
    private ProgressBar progressBar;

    private String txtFilePath;
    private String recordingKey;
    private String authToken;
    private int questionCount = 5; // (기본값)
    private boolean forceRegenerate = false; // (다시 만들기 플래그)

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            txtFilePath = getArguments().getString("filePath");
            // "다시 만들기"로 진입 시, 개수와 플래그를 받음
            questionCount = getArguments().getInt("questionCount", 5);
            forceRegenerate = getArguments().getBoolean("forceRegenerate", false);
        }

        if (txtFilePath != null && !txtFilePath.isEmpty()) {
            // (1) .txt 경로에서 고유 키(파일 이름) 추출
            recordingKey = txtFilePath.substring(txtFilePath.lastIndexOf('/') + 1)
                    .replace(".txt", "");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz_loading, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progressBar);
        userService = ApiClient.getClient().create(UserService.class);

        if (recordingKey == null || recordingKey.isEmpty()) {
            Toast.makeText(getContext(), "파일 키를 받지 못했습니다.", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) getActivity().finish();
            return;
        }

        // (2) [500/403 해결] 토큰부터 로드
        if (!loadAuthToken()) {
            goToLogin(); // 토큰 없으면 로그인 화면으로
            return;
        }

        // (3) [핵심 로직] DB에 문제가 있는지 "먼저 확인"
        // [핵심 로직]
        if (forceRegenerate) {
            // 1. [다시 만들기] -> DB 조회(GET) 건너뛰고 바로 파일 읽기
            readAndGenerateQuestions(questionCount);
        } else {
            // 2. [일반 입장] -> DB 조회(GET)부터 시작
            checkQuestionsOnServer();
        }
    }

    /**
     * [신규] API 1: 서버에 저장된 문제가 있는지 확인 (Cache Check)
     */
    private void checkQuestionsOnServer() {
        progressBar.setVisibility(View.VISIBLE);

        Call<Map<String, List<Question>>> call = userService.getExistingQuestions(authToken, recordingKey);
        call.enqueue(new Callback<Map<String, List<Question>>>() {
            @Override
            public void onResponse(Call<Map<String, List<Question>>> call, Response<Map<String, List<Question>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 4-A. [Cache Hit] 200 OK. DB에 문제가 있었음
                    List<Question> questionList = response.body().get("questions");
                    Log.d("QuizLoading", "DB에서 " + questionList.size() + "개의 문제를 로드했습니다.");
                    goToSuccessScreen(questionList); // -> 바로 퀴즈 시작

                } else if (response.code() == 404) {
                    // 4-B. [Cache Miss] 404 Not Found. DB에 문제가 없음
                    Log.d("QuizLoading", "DB에 문제가 없어 새로 생성합니다.");
                    // -> "몇 문제 생성할지" 묻는 안내창 띄우기
                    showQuestionCountDialog();

                } else {
                    // 4-C. (403, 500 등) 기타 오류
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "문제 조회 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, List<Question>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * [신규] DB에 문제가 없을 때만 "문제 생성 개수" 묻는 안내창 띄우기
     */
    private void showQuestionCountDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("문제 생성");
        builder.setMessage("DB에 저장된 문제가 없습니다.\n새로 생성할 문제 개수를 입력하세요.");
        builder.setCancelable(false); // (뒤로가기/바깥 터치로 닫기 방지)

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("예: 5 (기본값)");
        builder.setView(input);

        builder.setPositiveButton("생성", (dialog, which) -> {
            int count;
            try { count = Integer.parseInt(input.getText().toString()); }
            catch (NumberFormatException e) { count = 5; }
            if (count <= 0) count = 5;

            // API 2 (POST) 호출
            readAndGenerateQuestions(count);
        });
        builder.setNegativeButton("취소", (dialog, which) -> {
            if (getActivity() != null) getActivity().finish();
        });
        progressBar.setVisibility(View.GONE);
        builder.show();
    }

    /**
     * [신규] API 2: .txt 파일을 읽고, 서버에 "새로운" 문제 생성을 요청
     */
    private void readAndGenerateQuestions(int count) {
        progressBar.setVisibility(View.VISIBLE); // 다시 로딩바 표시

        // [ANR 해결] 파일 읽기를 백그라운드 스레드에서 실행
        new Thread(() -> {
            String textFromFile = readTextFromFile(txtFilePath);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (textFromFile == null || textFromFile.isEmpty()) {
                        Toast.makeText(getContext(), "텍스트 파일을 읽는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                        if (getActivity() != null) getActivity().finish();
                        return;
                    }

                    // 6. [500 해결] requestBody에 count, text, key 모두 담기
                    HashMap<String, String> requestBody = new HashMap<>();
                    requestBody.put("text", textFromFile);
                    requestBody.put("recordingKey", recordingKey);
                    requestBody.put("count", String.valueOf(count));

                    // 7. API 2 (POST) 호출
                    Call<Map<String, List<Question>>> call = userService.generateQuestions(authToken, requestBody);
                    call.enqueue(new Callback<Map<String, List<Question>>>() {
                        @Override
                        public void onResponse(Call<Map<String, List<Question>>> call, Response<Map<String, List<Question>>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                List<Question> questionList = response.body().get("questions");
                                goToSuccessScreen(questionList); // -> 퀴즈 시작
                            } else {
                                Log.d("QuizLoading", "문제 생성에 실패했습니다 (오류코드:" + response.code() + ")");
                                Toast.makeText(getContext(), "문제 생성 실패 (오류: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<Map<String, List<Question>>> call, Throwable t) {
                            Toast.makeText(getContext(), "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            }
        }).start();
    }

    // --- (이하 헬퍼 메소드들은 기존과 동일) ---

    /** [수정됨] 퀴즈 시작 화면으로 이동할 때, "filePath"도 함께 전달 */
    private void goToSuccessScreen(List<Question> questionList) {
        if (questionList == null || questionList.isEmpty()) { /* (오류 처리) */ return; }

        Bundle bundle = new Bundle();
        bundle.putSerializable("questionList", (Serializable) questionList);

        // ▼▼▼ (1. "다시 만들기"를 위해 .txt 파일 경로를 넘겨줍니다) ▼▼▼
        bundle.putString("filePath", txtFilePath);

        QuizSuccessFragment successFragment = new QuizSuccessFragment();
        successFragment.setArguments(bundle);
        if (getActivity() instanceof QuizActivity) {
            ((QuizActivity) getActivity()).showSuccessScreen(successFragment);
        }
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

    private void goToLogin() {
        if (getActivity() == null) return;
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private String readTextFromFile(String path) {
        if (path == null) return null;
        File file = new File(path);
        if (!file.exists()) {
            Log.e(TAG, "파일이 존재하지 않습니다: " + path);
            return null;
        }
        StringBuilder text = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line).append('\n');
            }
        } catch (IOException e) {
            Log.e(TAG, "파일을 읽을 수 없습니다: " + path, e);
            return null;
        }
        return text.toString();
    }
}