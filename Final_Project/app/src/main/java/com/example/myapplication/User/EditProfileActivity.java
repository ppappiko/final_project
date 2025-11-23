package com.example.myapplication.User; // 패키지명 확인

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.ApiClient;
import com.example.myapplication.R;

import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etEmail, etUsername, etPassword;
    private Button btnSave;
    private UserService userService;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        etEmail = findViewById(R.id.et_profile_email);
        etUsername = findViewById(R.id.et_profile_username);
        etPassword = findViewById(R.id.et_profile_password);
        btnSave = findViewById(R.id.btn_save_profile);

        userService = ApiClient.getClient().create(UserService.class);

        // 토큰 가져오기
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String rawToken = prefs.getString("jwt_token", "");
        token = "Bearer " + rawToken;

        // 1. 기존 정보 불러오기
        loadMyInfo();

        // 2. 저장 버튼 클릭
        btnSave.setOnClickListener(v -> updateInfo());
    }

    private void loadMyInfo() {
        userService.getMyInfo(token).enqueue(new Callback<UserDto>() {
            @Override
            public void onResponse(Call<UserDto> call, Response<UserDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserDto user = response.body();
                    etEmail.setText(user.getEmail());
                    etUsername.setText(user.getUsername());
                } else {
                    Toast.makeText(EditProfileActivity.this, "정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<UserDto> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "통신 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateInfo() {
        String newName = etUsername.getText().toString();
        String newPass = etPassword.getText().toString();

        UserDto request = new UserDto();
        request.setUsername(newName);
        if (!newPass.isEmpty()) {
            request.setPassword(newPass);
        }

        // 서버 요청
        userService.updateMyInfo(token, request).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, String> body = response.body();
                    String message = body.get("message");

                    // ▼▼▼ [핵심 로직] 새 토큰이 있으면 교체합니다 ▼▼▼
                    String newToken = body.get("newToken");
                    if (newToken != null && !newToken.isEmpty()) {
                        // 1. SharedPreferences 갱신
                        SharedPreferences prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("jwt_token", newToken);
                        editor.apply();

                        // 2. 현재 메모리의 토큰 변수 갱신 (혹시 몰라서)
                        token = "Bearer " + newToken;

                        Log.d("EditProfile", "닉네임 변경으로 토큰이 갱신되었습니다.");
                    }

                    Toast.makeText(EditProfileActivity.this, message, Toast.LENGTH_SHORT).show();
                    finish(); // 수정 완료 후 화면 닫기
                } else {
                    Toast.makeText(EditProfileActivity.this, "수정 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "통신 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }
}