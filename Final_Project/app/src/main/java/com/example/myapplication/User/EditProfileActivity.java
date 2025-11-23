package com.example.myapplication.User;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.ApiClient;
import com.example.myapplication.R;

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
        token = "Bearer " + prefs.getString("jwt_token", "");

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
                    etEmail.setText(user.getEmail()); // 이메일 표시
                    etUsername.setText(user.getUsername()); // 이름 표시
                }
            }
            @Override
            public void onFailure(Call<UserDto> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "정보 로드 실패", Toast.LENGTH_SHORT).show();
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

        userService.updateMyInfo(token, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EditProfileActivity.this, "정보가 수정되었습니다.", Toast.LENGTH_SHORT).show();
                    finish(); // 화면 닫기
                } else {
                    Toast.makeText(EditProfileActivity.this, "수정 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "통신 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
