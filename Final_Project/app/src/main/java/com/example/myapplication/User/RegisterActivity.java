package com.example.myapplication.User;

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

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "GenerateFragment";

    private EditText etEmail, etPassword;
    private Button btnRegister;
    private UserService userService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etEmail = findViewById(R.id.et_email_register);
        etPassword = findViewById(R.id.et_password_register);
        btnRegister = findViewById(R.id.btn_register);

        // Retrofit 클라이언트를 통해 UserService 인터페이스 구현체 생성
        userService = ApiClient.getClient().create(UserService.class);

        btnRegister.setOnClickListener(v -> {
            String username = etEmail.getText().toString();
            String password = etPassword.getText().toString();

            // User 객체 생성
            User user = new User(username, password);

            // 서버에 회원가입 요청
            Call<Map<String, String>> call = userService.registerUser(user); // Call 타입 변경
            call.enqueue(new Callback<Map<String, String>>() { // Callback 타입 변경
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        // 성공 시 JSON에서 "message" 키로 값을 꺼내옴
                        String message = response.body().get("message");
                        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        // 실패 처리
                        Toast.makeText(RegisterActivity.this, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    // 네트워크 오류 처리
                    Toast.makeText(RegisterActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("GenerateFragment", "네트워크 오류: " + t.getMessage());
                }
            });
        });
    }
}
