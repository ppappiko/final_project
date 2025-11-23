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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
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

import android.provider.OpenableColumns;


public class CreatePostActivity extends AppCompatActivity {

    private EditText etTitle, etContent;
    private Spinner spinnerCategory, spinnerMyQuiz;
    private Button btnSubmit, btnAttachFile;
    private TextView tvFileName; // 파일명 표시용 텍스트뷰 (XML에 추가 권장)

    private View layoutFileUpload;

    private UserService userService;
    private List<String> myQuizKeys = new ArrayList<>(); // 가져온 문제 목록
    private String selectedQuizKey = null; // 선택된 문제 키
    private Uri selectedFileUri = null;    // 선택된 파일 URI

    // 파일 선택기
    // 파일 선택기 결과 처리
    private final ActivityResultLauncher<String> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedFileUri = uri;

                    // 1. 파일 이름 가져오기 함수 호출
                    String fileName = getFileName(uri);

                    // 2. 화면에 표시
                    tvFileName.setText("📄 " + fileName);
                    tvFileName.setVisibility(View.VISIBLE);
                    btnAttachFile.setText("파일 변경하기");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        etTitle = findViewById(R.id.et_post_title);
        etContent = findViewById(R.id.et_post_content);
        spinnerCategory = findViewById(R.id.spinner_category);
        spinnerMyQuiz = findViewById(R.id.spinner_my_quiz);
        btnAttachFile = findViewById(R.id.btn_attach_file);
        btnSubmit = findViewById(R.id.btn_submit_post);
        tvFileName = findViewById(R.id.tv_file_name); // 필요 시 추가

        userService = ApiClient.getClient().create(UserService.class);

        layoutFileUpload = findViewById(R.id.layout_file_upload);

        // 1. 카테고리 선택 리스너
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // position 2: 정보공유(문제공유)
                if (position == 2) {
                    btnAttachFile.setVisibility(View.GONE);    // 파일 버튼 숨김
                    spinnerMyQuiz.setVisibility(View.VISIBLE); // 문제 스피너 보임
                    selectedFileUri = null; // 파일 선택 초기화
                    loadMyQuizList();       // 내 문제 목록 로드
                } else {
                    btnAttachFile.setVisibility(View.VISIBLE); // 파일 버튼 보임
                    spinnerMyQuiz.setVisibility(View.GONE);    // 문제 스피너 숨김
                    selectedQuizKey = null; // 문제 선택 초기화
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 2. 문제 선택 리스너
        spinnerMyQuiz.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!myQuizKeys.isEmpty()) {
                    selectedQuizKey = myQuizKeys.get(position);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedQuizKey = null;
            }
        });

        // 3. 파일 첨부 버튼
        btnAttachFile.setOnClickListener(v -> {
            filePickerLauncher.launch("*/*"); // 모든 파일 타입
        });

        // 4. 등록 버튼
        btnSubmit.setOnClickListener(v -> submitPost());
    }

    // 내 문제 목록 불러오기
    private void loadMyQuizList() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String token = "Bearer " + prefs.getString("jwt_token", "");

        userService.getMyQuizList(token).enqueue(new Callback<Map<String, List<String>>>() {
            @Override
            public void onResponse(Call<Map<String, List<String>>> call, Response<Map<String, List<String>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    myQuizKeys = response.body().get("keys");

                    if (myQuizKeys != null && !myQuizKeys.isEmpty()) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                CreatePostActivity.this,
                                android.R.layout.simple_spinner_item,
                                myQuizKeys
                        );
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerMyQuiz.setAdapter(adapter);
                    } else {
                        List<String> empty = new ArrayList<>();
                        empty.add("공유할 문제가 없습니다.");
                        spinnerMyQuiz.setAdapter(new ArrayAdapter<>(CreatePostActivity.this, android.R.layout.simple_spinner_item, empty));
                        selectedQuizKey = null;
                    }
                }
            }
            @Override
            public void onFailure(Call<Map<String, List<String>>> call, Throwable t) {
                Toast.makeText(CreatePostActivity.this, "문제 목록 로드 실패", Toast.LENGTH_SHORT).show();
            }
        });
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
        if (category.equals("INFO") && (selectedQuizKey == null || myQuizKeys.isEmpty())) {
            Toast.makeText(this, "공유할 문제를 선택해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. DTO(JSON) 데이터 준비
        // (INFO 카테고리면 selectedQuizKey가 들어가고, 아니면 null이 들어감)
        PostRequest requestDto = new PostRequest(title, content, category, selectedQuizKey);

        Gson gson = new Gson();
        String jsonString = gson.toJson(requestDto);
        // "application/json" 타입의 RequestBody 생성
        RequestBody dataPart = RequestBody.create(MediaType.parse("application/json"), jsonString);

        // 2. 파일 데이터 준비 (MultipartBody.Part)
        MultipartBody.Part filePart = null;

        // 일반 게시판이고 파일이 선택된 경우에만 처리
        if (!category.equals("INFO") && selectedFileUri != null) {
            File file = getFileFromUri(selectedFileUri); // Uri -> 임시 File 변환
            if (file != null) {
                // 파일의 MIME 타입 설정 (여기선 모든 타입 허용)
                RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                // "file"은 서버에서 @RequestPart("file")로 받는 이름과 같아야 함
                filePart = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
            }
        }

        // 3. 전송
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String token = "Bearer " + prefs.getString("jwt_token", "");

        // ApiService의 createPost 메서드는 (token, dataPart, filePart)를 받아야 함
        ApiClient.getClient().create(ApiService.class)
                .createPost(token, dataPart, filePart)
                .enqueue(new Callback<Void>() {
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
                        Toast.makeText(CreatePostActivity.this, "통신 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Uri를 File로 변환하는 헬퍼 메서드
    private File getFileFromUri(Uri uri) {
        try {
            String fileName = "temp_file";
            // 파일 이름 가져오기 시도
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) fileName = cursor.getString(nameIndex);
                }
            }

            File tempFile = new File(getCacheDir(), fileName);
            try (InputStream inputStream = getContentResolver().openInputStream(uri);
                 FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
            }
            return tempFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    // [헬퍼 메서드] Uri에서 파일 이름 추출하기
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if(index >= 0) result = cursor.getString(index);
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}