package com.example.myapplication.User;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.ApiClient;
import com.example.myapplication.R;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FindAccountActivity extends AppCompatActivity {

    private EditText etIdName, etIdPhone;
    private EditText etPwEmail, etPwName, etPwPhone;
    private Button btnFindId, btnFindPw;
    private UserService userService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_account);

        // 뷰 연결
        etIdName = findViewById(R.id.et_find_id_name);
        etIdPhone = findViewById(R.id.et_find_id_phone);
        btnFindId = findViewById(R.id.btn_find_id);

        etPwEmail = findViewById(R.id.et_find_pw_email);
        etPwName = findViewById(R.id.et_find_pw_name);
        etPwPhone = findViewById(R.id.et_find_pw_phone);
        btnFindPw = findViewById(R.id.btn_find_pw);

        userService = ApiClient.getClient().create(UserService.class);

        // [아이디 찾기] 버튼 리스너
        btnFindId.setOnClickListener(v -> findId());

        // [비밀번호 찾기] 버튼 리스너 (작성하신 로직 적용)
        btnFindPw.setOnClickListener(v -> {
            String email = etPwEmail.getText().toString().trim();
            String name = etPwName.getText().toString().trim();
            String phone = etPwPhone.getText().toString().trim();

            // 1. 유효성 검사
            if (email.isEmpty() || name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "정보를 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. 데이터 준비
            HashMap<String, String> map = new HashMap<>();
            map.put("email", email);
            map.put("name", name);
            map.put("phone", phone);

            // 3. 서버 요청 (이메일 전송)
            // (로딩 표시를 원한다면 여기서 showLoading() 호출)
            userService.findPassword(map).enqueue(createCallback(
                    "임시 비밀번호가 이메일로 전송되었습니다.\n메일함을 확인해주세요.", // 성공 메시지
                    "일치하는 회원 정보가 없습니다.", // 실패 메시지
                    () -> {
                        // 성공 시 동작: 현재 화면 종료 (로그인 화면으로 돌아감)
                        finish();
                    },
                    null // 실패 시 추가 동작 없음
            ));
        });
    }

    // [아이디 찾기] 구현
    private void findId() {
        String name = etIdName.getText().toString();
        String phone = etIdPhone.getText().toString();

        Map<String, String> map = new HashMap<>();
        map.put("name", name);
        map.put("phone", phone);

        userService.findId(map).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String email = response.body().get("email");
                    // (선택) 아이디 찾기도 이메일로 보내고 싶으면 서버 로직 수정 필요.
                    // 현재는 화면에 보여주는 방식입니다.
                    showDialog("아이디 찾기 성공", "회원님의 아이디는\n" + email + "\n입니다.");
                } else {
                    Toast.makeText(FindAccountActivity.this, "정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(FindAccountActivity.this, "통신 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ▼▼▼ [핵심] 재사용 가능한 콜백 생성 메서드 (이게 꼭 있어야 합니다!) ▼▼▼
    private <T> Callback<T> createCallback(String successMsg, String failMsg, Runnable onSuccess, Runnable onFail) {
        return new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(FindAccountActivity.this, successMsg, Toast.LENGTH_LONG).show();
                    if (onSuccess != null) onSuccess.run();
                } else {
                    // 에러 메시지 처리 (서버가 보낸 메시지가 있다면 우선 표시)
                    String message = failMsg;
                    // (여기에 errorBody 파싱 로직을 넣을 수도 있음)

                    Toast.makeText(FindAccountActivity.this, message, Toast.LENGTH_SHORT).show();
                    if (onFail != null) onFail.run();
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                Toast.makeText(FindAccountActivity.this, "통신 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                if (onFail != null) onFail.run();
            }
        };
    }

    // 다이얼로그 헬퍼 메서드
    private void showDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("확인", null)
                .show();
    }
}