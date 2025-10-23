package com.example.myapplication.User;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.ApiClient;
import com.example.myapplication.R;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "GenerateFragment";

    private EditText etEmail, etPassword, etNickname, etVerifyCode,etName, etPhone;
    private Button btnRegister, btnVerifyEmail, btnCheckNickname, btnConfirmCode;
    private UserService userService;
    private LinearLayout layoutEmailVerify;
    private boolean isEmailVerified = false;
    private boolean isNicknameChecked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etEmail = findViewById(R.id.et_email_register);
        etPassword = findViewById(R.id.et_password_register);
        btnRegister = findViewById(R.id.btn_register);
        btnVerifyEmail = findViewById(R.id.btn_verify_email);
        btnCheckNickname = findViewById(R.id.btn_check_nickname);
        etNickname = findViewById(R.id.et_nickname);
        layoutEmailVerify = findViewById(R.id.layout_email_verify);
        etVerifyCode = findViewById(R.id.et_verify_code);
        btnConfirmCode = findViewById(R.id.btn_confirm_code);
        etName = findViewById(R.id.et_name);
        etPhone = findViewById(R.id.et_phone);

        // Retrofit 클라이언트를 통해 UserService 인터페이스 구현체 생성
        userService = ApiClient.getClient().create(UserService.class);



        // 1. 닉네임 중복 확인 버튼
        btnCheckNickname.setOnClickListener(v -> {
            String nickname = etNickname.getText().toString();
            if (nickname.isEmpty()) {
                Toast.makeText(this, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            HashMap<String, String> map = new HashMap<>();
            map.put("username", nickname);
            userService.checkNickname(map).enqueue(createCallback(
                    "사용 가능한 닉네임입니다.",
                    "이미 사용 중인 닉네임입니다.",
                    () -> isNicknameChecked = true,
                    () -> isNicknameChecked = false
            ));
        });

        // 2. 이메일 인증 요청 버튼
        btnVerifyEmail.setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            if (email.isEmpty()) {
                Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            HashMap<String, String> map = new HashMap<>();
            map.put("email", email);
            userService.requestEmailVerification(map).enqueue(createCallback(
                    "인증 코드가 발송되었습니다.",
                    "이메일 발송에 실패했습니다.",
                    () -> layoutEmailVerify.setVisibility(View.VISIBLE),
                    null
            ));
        });

        // 3. 이메일 인증 확인 버튼
        btnConfirmCode.setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            String code = etVerifyCode.getText().toString();

            HashMap<String, String> map = new HashMap<>();
            map.put("email", email);
            map.put("code", code);
            userService.confirmEmailVerification(map).enqueue(createCallback(
                    "이메일 인증이 완료되었습니다.",
                    "인증 코드가 올바르지 않습니다.",
                    () -> isEmailVerified = true,
                    () -> isEmailVerified = false
            ));
        });

        // 4. 최종 회원가입 버튼
        btnRegister.setOnClickListener(v -> {
            if (!isNicknameChecked) {
                Toast.makeText(this, "닉네임 중복 확인을 해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isEmailVerified) {
                Toast.makeText(this, "이메일 인증을 완료해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            // TODO: 비밀번호와 비밀번호 확인이 일치하는지 등 추가 검증 로직 필요

            // 모든 검증 통과 시, 실제 회원가입 요청
            String username = etNickname.getText().toString(); // 닉네임을 username으로 사용
            String password = etPassword.getText().toString();
            String name = etName.getText().toString();
            String phone = etPhone.getText().toString();
            String email = etEmail.getText().toString();
            User user = new User(username, password, name, phone, email);

            userService.registerUser(user).enqueue(createCallback(
                    "회원가입 성공!",
                    "회원가입 실패",
                    this::finish, // 회원가입 성공 시 액티비티 종료
                    null
            ));
        });
    }

    // 반복적인 Callback 코드를 줄이기 위한 헬퍼 메소드
    private Callback<Map<String, String>> createCallback(String successMsg, String errorMsg, Runnable onSuccess, Runnable onFailure) {
        return new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, successMsg, Toast.LENGTH_SHORT).show();
                    if (onSuccess != null) onSuccess.run();
                } else {
                    Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    if (onFailure != null) onFailure.run();
                }
            }
            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
            }
        };
    }
}
