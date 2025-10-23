package com.example.myapplication.User;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.ApiClient;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnTest;
    private UserService userService;

    private TextView btnToRegister; // 1. 회원가입 버튼 변수 선언

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // activity_login.xml 레이아웃 사용

        // 1. UI 요소 초기화
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnToRegister = findViewById(R.id.btn_to_register); // 2. 회원가입 버튼 초기화)

        // 2. Retrofit 클라이언트를 통해 UserService 인터페이스 구현체 생성
        userService = ApiClient.getClient().create(UserService.class);

        // 3. 로그인 버튼 클릭 리스너 설정
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();

            // 입력값 검증 (비어있는지 확인)
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "아이디와 비밀번호를 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 4. 서버에 보낼 데이터를 Map 형태로 만듦
            HashMap<String, String> credentials = new HashMap<>();
            credentials.put("email", email);
            credentials.put("password", password);

            // 5. 서버에 로그인 요청 보내기
            Call<Map<String, String>> call = userService.loginUser(credentials);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        // 로그인 성공 (HTTP 2xx)
                        String token = response.body().get("token");
                        Toast.makeText(LoginActivity.this, "로그인 성공!", Toast.LENGTH_SHORT).show();
                        Log.d("LOGIN_SUCCESS", "Token: " + token);

                        // 1. SharedPreferences를 이용해 토큰 저장하기
                        SharedPreferences sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("jwt_token", token);
                        editor.apply(); // 비동기적으로 저장

                        // 2. MainActivity로 이동하기
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);

                        // 3. 로그인 화면은 종료하여 뒤로가기 버튼으로 돌아올 수 없게 함
                        finish();

                        // --- ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲ ---

                    } else {
                        // 로그인 실패 (HTTP 4xx, 5xx)
                        Toast.makeText(LoginActivity.this, "아이디 또는 비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show();
                        Log.e("LOGIN_FAILURE", "Error Code: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    // 네트워크 통신 자체에 실패
                    Toast.makeText(LoginActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    Log.e("LOGIN_NETWORK_ERROR", "Error: " + t.getMessage());
                }
            });
        });
        // 3. '회원가입' 텍스트 버튼 클릭 리스너 설정
        btnToRegister.setOnClickListener(v -> {
            // RegisterActivity로 이동하기 위한 Intent(신호) 생성
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent); // RegisterActivity 시작
        });


    }
}
