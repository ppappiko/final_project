package com.example.myapplication.community;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner; // 스피너 추가 필요 (xml에도)
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.ApiClient;
import com.example.myapplication.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreatePostActivity extends AppCompatActivity {

    private EditText etTitle, etContent;
    private Button btnSubmit;
    // private Button btnAttachFile; // 파일 첨부는 나중에 서버 지원 시 활성화

    // 카테고리를 선택할 수 있는 UI가 필요합니다 (예: Spinner)
    // 만약 없다면 기본값으로 설정해야 합니다.
    private Spinner spinnerCategory;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        etTitle = findViewById(R.id.et_post_title);
        etContent = findViewById(R.id.et_post_content);
        btnSubmit = findViewById(R.id.btn_submit_post);
        spinnerCategory = findViewById(R.id.spinner_category); // XML에 추가 필요!

        btnSubmit.setOnClickListener(v -> submitPost());
    }

    private void submitPost() {
        String title = etTitle.getText().toString();
        String content = etContent.getText().toString();

        // 스피너에서 선택된 카테고리 가져오기 (예시 로직)
        // XML에 스피너가 없다면 "FREE" 등으로 고정하세요.
        String category = "FREE";
        if (spinnerCategory != null && spinnerCategory.getSelectedItem() != null) {
            // 스피너 아이템 순서가 [자유, 질문, 공유] 라고 가정
            int pos = spinnerCategory.getSelectedItemPosition();
            if (pos == 1) category = "QNA";
            else if (pos == 2) category = "INFO";
        }

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "제목과 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. 토큰 가져오기
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);
        if (token == null) return;

        // 2. 요청 객체 생성
        PostRequest request = new PostRequest(title, content, category);

        // 3. API 호출
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.createPost("Bearer " + token, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CreatePostActivity.this, "작성 완료", Toast.LENGTH_SHORT).show();
                    finish(); // 액티비티 종료 -> 목록 화면으로 복귀
                } else {
                    Toast.makeText(CreatePostActivity.this, "작성 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(CreatePostActivity.this, "통신 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }
}