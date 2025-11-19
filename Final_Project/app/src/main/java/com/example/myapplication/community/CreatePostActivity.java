package com.example.myapplication.community;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.ApiClient;
import com.example.myapplication.R;
import com.google.gson.Gson;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreatePostActivity extends AppCompatActivity {

    private EditText etTitle, etContent;
    private Button btnAttachFile, btnSubmit;
    private TextView tvAttachedFileName;

    private String attachedFilePath = null;

    private final ActivityResultLauncher<Intent> selectFileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    attachedFilePath = result.getData().getStringExtra("selected_file_path");
                    String attachedFileName = result.getData().getStringExtra("selected_file_name");
                    tvAttachedFileName.setText(attachedFileName);
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        etTitle = findViewById(R.id.et_post_title);
        etContent = findViewById(R.id.et_post_content);
        btnAttachFile = findViewById(R.id.btn_attach_file);
        btnSubmit = findViewById(R.id.btn_submit_post);
        tvAttachedFileName = findViewById(R.id.tv_attached_file_name);

        btnAttachFile.setOnClickListener(v -> {
            Intent intent = new Intent(this, SelectAttachmentActivity.class);
            selectFileLauncher.launch(intent);
        });

        btnSubmit.setOnClickListener(v -> submitPost());
    }

    private void submitPost() {
        String title = etTitle.getText().toString();
        String content = etContent.getText().toString();

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "제목과 내용을 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> postData = new HashMap<>();
        postData.put("title", title);
        postData.put("content", content);
        postData.put("author", "test_user");

        Gson gson = new Gson();
        String postJson = gson.toJson(postData);
        RequestBody postBody = RequestBody.create(postJson, MediaType.parse("application/json; charset=utf-8"));

        Call<Void> call;

        if (attachedFilePath != null) {
            File file = new File(attachedFilePath);
            String fileType = attachedFilePath.endsWith(".txt") ? "text/plain" : "audio/m4a";
            RequestBody fileReqBody = RequestBody.create(file, MediaType.parse(fileType));
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", file.getName(), fileReqBody);
            call = ApiClient.getApiService().createPost(postBody, filePart);
        } else {
            call = ApiClient.getApiService().createPost(postBody);
        }

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CreatePostActivity.this, "게시물이 성공적으로 등록되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(CreatePostActivity.this, "게시물 등록에 실패했습니다. 코드: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(CreatePostActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
