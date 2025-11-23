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
import java.util.regex.Pattern;

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

        // Retrofit 클라이언트를 통해 UserService 인터페이스 구현체 생성 (올바른 메소드 사용)
        userService = ApiClient.getUserService();

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
            Toast.makeText(this, "인증 이메일이 10초이내에 발송됩니다.", Toast.LENGTH_SHORT).show();
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

            String username = etNickname.getText().toString();
            String password = etPassword.getText().toString();
            String name = etName.getText().toString();
            String phone = etPhone.getText().toString();
            String email = etEmail.getText().toString();
            User user = new User(username, password, name, phone, email);

            // ▼▼▼ [추가] 비밀번호 유효성 검사 ▼▼▼
            // 규칙: 영문자 포함 + 숫자 포함 + 9자 이상
            if (!isValidPassword(password)) {
                Toast.makeText(this, "비밀번호는 영문과 숫자를 포함하여 9자 이상이어야 합니다.", Toast.LENGTH_LONG).show();
                return; // 서버로 요청 보내지 않고 종료
            }
            if (!isValidPhoneNumber(phone)) {
                Toast.makeText(this, "올바른 휴대전화 번호를 입력해주세요. (010으로 시작, 11자리)", Toast.LENGTH_SHORT).show();
                return;
            }

            userService.registerUser(user).enqueue(createCallback(
                    "회원가입 성공!",
                    "회원가입 실패",
                    this::finish, // 회원가입 성공 시 액티비티 종료
                    null
            ));
        });
    }

    private boolean isValidPassword(String password) {
        // 정규식 설명:
        // ^                 : 문자열 시작
        // (?=.*[A-Za-z])    : 최소 하나의 영문자가 포함되어야 함
        // (?=.*[0-9])       : 최소 하나의 숫자가 포함되어야 함
        // .{9,}             : 길이가 최소 9자 이상이어야 함
        // $                 : 문자열 끝
        String passwordPattern = "^(?=.*[A-Za-z])(?=.*[0-9]).{9,}$";

        return Pattern.matches(passwordPattern, password);
    }
    /**
     * [신규] 전화번호 정규식 검사
     * 규칙: 010으로 시작 + 숫자 8자리 (총 11자리, 하이픈 없음)
     */
    private boolean isValidPhoneNumber(String phone) {
        // 1. 하이픈(-)이 있다면 제거해주는 센스 (선택 사항)
        // phone = phone.replace("-", "");

        // 정규식: ^010 (010으로 시작) + [0-9]{8} (숫자 8개) + $ (끝)
        String phonePattern = "^010[0-9]{8}$";

        return Pattern.matches(phonePattern, phone);
    }

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