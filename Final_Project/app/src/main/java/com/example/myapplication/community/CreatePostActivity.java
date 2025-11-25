package com.example.myapplication.community;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.ApiClient;
import com.example.myapplication.R;
import com.example.myapplication.User.UserService;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreatePostActivity extends AppCompatActivity {

    // UI 요소
    private EditText etTitle, etContent;
    private Spinner spinnerCategory, spinnerMyQuiz;
    private Button btnSubmit, btnAttachFile;
    private LinearLayout layoutFileUpload;
    private TextView tvFileName;

    // 데이터
    private UserService userService;
    private List<String> myQuizKeys = new ArrayList<>();
    private String selectedQuizKey = null;
    private Uri selectedFileUri = null;

    // 수정 모드 관련
    private boolean isEditMode = false;
    private Post editTargetPost;

    // 파일 선택기
    private final ActivityResultLauncher<String> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedFileUri = uri;
                    String fileName = getFileName(uri);
                    tvFileName.setText("📄 " + fileName);
                    tvFileName.setVisibility(View.VISIBLE);
                    btnAttachFile.setText("파일 변경하기");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        initViews();
        userService = ApiClient.getClient().create(UserService.class);

        // 1. 수정 모드인지 확인하고 데이터 채우기
        checkEditMode();

        // 2. 리스너 설정
        setupListeners();
    }

    private void initViews() {
        etTitle = findViewById(R.id.et_post_title);
        etContent = findViewById(R.id.et_post_content);
        spinnerCategory = findViewById(R.id.spinner_category);
        spinnerMyQuiz = findViewById(R.id.spinner_my_quiz);
        layoutFileUpload = findViewById(R.id.layout_file_upload);
        btnAttachFile = findViewById(R.id.btn_attach_file);
        tvFileName = findViewById(R.id.tv_file_name);
        btnSubmit = findViewById(R.id.btn_submit_post);
    }

    private void checkEditMode() {
        isEditMode = getIntent().getBooleanExtra("is_edit_mode", false);

        if (isEditMode) {
            editTargetPost = (Post) getIntent().getSerializableExtra("post_data");

            if (editTargetPost != null) {
                etTitle.setText(editTargetPost.getTitle());
                etContent.setText(editTargetPost.getContentPreview());
                btnSubmit.setText("수정 완료");
                // (카테고리 자동 선택 로직 추가 가능)
            }
        }
    }

    private void setupListeners() {
        // 카테고리 선택 리스너
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 2) { // 정보공유(문제공유)
                    layoutFileUpload.setVisibility(View.GONE);
                    spinnerMyQuiz.setVisibility(View.VISIBLE);
                    selectedFileUri = null;
                    loadMyQuizList();
                } else { // 일반
                    layoutFileUpload.setVisibility(View.VISIBLE);
                    spinnerMyQuiz.setVisibility(View.GONE);
                    selectedQuizKey = null;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 문제 선택 리스너
        spinnerMyQuiz.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!myQuizKeys.isEmpty()) selectedQuizKey = myQuizKeys.get(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { selectedQuizKey = null; }
        });

        // 파일 첨부
        btnAttachFile.setOnClickListener(v -> filePickerLauncher.launch("*/*"));

        // 등록/수정 버튼
        btnSubmit.setOnClickListener(v -> submitPost());
    }

    private void submitPost() {
        String title = etTitle.getText().toString();
        String content = etContent.getText().toString();

        int pos = spinnerCategory.getSelectedItemPosition();
        String category = "FREE";
        if (pos == 1) category = "QNA";
        else if (pos == 2) category = "INFO";

        // 유효성 검사
        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "제목과 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 토큰 가져오기
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String token = "Bearer " + prefs.getString("jwt_token", "");
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        // --- [분기점] 수정하기 vs 새로 만들기 ---
        if (isEditMode) {
            // [수정 요청] (간단하게 제목, 내용만 수정하는 것으로 가정)
            PostRequest request = new PostRequest(title, content, category, null);
            apiService.updatePost(token, editTargetPost.getId(), request).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(CreatePostActivity.this, "수정되었습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        String errorMessage = "수정 실패"; // 기본 메시지

                        try {
                            if (response.errorBody() != null) {
                                // 서버가 보낸 "본인의 글만 수정할 수 있습니다." 텍스트 읽기
                                errorMessage = response.errorBody().string();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // 토스트로 띄우기
                        Toast.makeText(CreatePostActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(CreatePostActivity.this, "오류 발생", Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            // [신규 작성 요청]
            // 1. DTO 생성
            PostRequest requestDto = new PostRequest(title, content, category, selectedQuizKey);
            Gson gson = new Gson();
            String jsonString = gson.toJson(requestDto);
            RequestBody dataPart = RequestBody.create(MediaType.parse("application/json"), jsonString);

            // 2. 파일 생성
            MultipartBody.Part filePart = null;
            if (!category.equals("INFO") && selectedFileUri != null) {
                File file = getFileFromUri(selectedFileUri);
                if (file != null) {
                    RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                    filePart = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
                }
            }

            // 3. 전송
            apiService.createPost(token, dataPart, filePart).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(CreatePostActivity.this, "작성 완료", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(CreatePostActivity.this, "작성 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(CreatePostActivity.this, "통신 오류", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadMyQuizList() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String token = "Bearer " + prefs.getString("jwt_token", "");

        userService.getMyQuizList(token).enqueue(new Callback<Map<String, List<String>>>() {
            @Override
            public void onResponse(Call<Map<String, List<String>>> call, Response<Map<String, List<String>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    myQuizKeys = response.body().get("keys");
                    if (myQuizKeys != null && !myQuizKeys.isEmpty()) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(CreatePostActivity.this, android.R.layout.simple_spinner_item, myQuizKeys);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerMyQuiz.setAdapter(adapter);
                    } else {
                        List<String> empty = new ArrayList<>(); empty.add("문제 없음");
                        spinnerMyQuiz.setAdapter(new ArrayAdapter<>(CreatePostActivity.this, android.R.layout.simple_spinner_item, empty));
                    }
                }
            }
            @Override
            public void onFailure(Call<Map<String, List<String>>> call, Throwable t) {}
        });
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) result = cursor.getString(index);
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) result = result.substring(cut + 1);
        }
        return result;
    }

    private File getFileFromUri(Uri uri) {
        try {
            String fileName = getFileName(uri);
            File tempFile = new File(getCacheDir(), fileName);
            try (InputStream inputStream = getContentResolver().openInputStream(uri);
                 FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) outputStream.write(buffer, 0, length);
            }
            return tempFile;
        } catch (Exception e) {
            return null;
        }
    }
}