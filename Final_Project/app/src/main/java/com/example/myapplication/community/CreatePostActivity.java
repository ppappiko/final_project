package com.example.myapplication.community;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.ApiClient;
import com.example.myapplication.R;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreatePostActivity extends AppCompatActivity {

    private Spinner categorySpinner;
    private TextInputEditText titleEditText;
    private TextInputEditText contentEditText;
    private Button submitButton;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        // 툴바 설정
        Toolbar toolbar = findViewById(R.id.toolbar_create_post);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 뒤로가기 버튼 활성화

        // 뷰 초기화
        categorySpinner = findViewById(R.id.spinner_post_category);
        titleEditText = findViewById(R.id.et_post_title);
        contentEditText = findViewById(R.id.et_post_content);
        submitButton = findViewById(R.id.btn_submit_post);

        // 스피너 설정
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.post_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // Retrofit 서비스 초기화
        apiService = ApiClient.getClient().create(ApiService.class);

        // 작성 완료 버튼 클릭 리스너
        submitButton.setOnClickListener(v -> submitPost());
    }

    private void submitPost() {
        String category = categorySpinner.getSelectedItem().toString();
        String title = titleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();

        // 입력 유효성 검사
        if (title.isEmpty()) {
            Toast.makeText(this, "제목을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (content.isEmpty()) {
            Toast.makeText(this, "내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 서버로 전송할 데이터 생성
        PostRequest postRequest = new PostRequest(category, title, content);

        // API 호출
        Call<Void> call = apiService.createPost(postRequest);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CreatePostActivity.this, "게시물이 성공적으로 등록되었습니다.", Toast.LENGTH_SHORT).show();
                    finish(); // 액티비티 종료
                } else {
                    Toast.makeText(CreatePostActivity.this, "오류가 발생했습니다: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(CreatePostActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // 뒤로가기 버튼 클릭 시 액티비티 종료
        return true;
    }
}
