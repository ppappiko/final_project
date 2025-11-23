package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.myapplication.User.EditProfileActivity;
import com.example.myapplication.User.LoginActivity;
import com.example.myapplication.User.UserDto;
import com.example.myapplication.User.UserService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private TextView tvProfileName;
    private UserService userService;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // UI 요소들과 클릭 리스너 연결
        CardView cardMemberInfo = view.findViewById(R.id.card_member_info);
        CardView cardFaq = view.findViewById(R.id.card_faq);
        CardView cardTerms = view.findViewById(R.id.card_terms);
        TextView btnLogout = view.findViewById(R.id.btn_logout);
        tvProfileName = view.findViewById(R.id.tv_user_name);

        userService = ApiClient.getClient().create(UserService.class);

        // 각 메뉴 클릭 시 간단한 토스트 메시지를 보여주는 예시
        cardMemberInfo.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });
        cardFaq.setOnClickListener(v -> Toast.makeText(getContext(), "공지사항 및 FAQ 클릭됨", Toast.LENGTH_SHORT).show());
        cardTerms.setOnClickListener(v -> Toast.makeText(getContext(), "이용 약관 클릭됨", Toast.LENGTH_SHORT).show());
        btnLogout.setOnClickListener(v -> {
            // Toast.makeText(getContext(), "로그아웃 클릭됨", Toast.LENGTH_SHORT).show(); // (기존 코드)

            // 1. 로그아웃 확인 다이얼로그 띄우기
            new AlertDialog.Builder(getContext())
                    .setTitle("로그아웃")
                    .setMessage("정말 로그아웃 하시겠습니까?")
                    .setPositiveButton("로그아웃", (dialog, which) -> {
                        // "예"를 누르면 실제 로그아웃 실행
                        performLogout();
                    })
                    .setNegativeButton("취소", null)
                    .show();
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        // 3. 화면이 보일 때마다 내 정보(닉네임)를 서버에서 새로 불러옴
        // (회원정보 수정 후 돌아왔을 때 갱신되도록 onResume에 작성)
        loadUserProfile();
    }

    /**
     * 서버에서 내 정보를 가져와 닉네임을 표시하는 메서드
     */
    private void loadUserProfile() {
        if (getContext() == null) return;

        // 1. 토큰 가져오기
        SharedPreferences prefs = getContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);

        if (token == null) return; // 토큰 없으면 패스

        // 2. 서버에 내 정보 요청 (GET /api/users/me)
        userService.getMyInfo("Bearer " + token).enqueue(new Callback<UserDto>() {
            @Override
            public void onResponse(Call<UserDto> call, Response<UserDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserDto myInfo = response.body();

                    // 3. 받아온 닉네임으로 텍스트 변경
                    if (myInfo.getUsername() != null) {
                        tvProfileName.setText(myInfo.getUsername());
                    }
                }
            }

            @Override
            public void onFailure(Call<UserDto> call, Throwable t) {
                // 네트워크 오류 시 조용히 넘어감 (또는 토스트 메시지)
            }
        });
    }

        /**
         * 실제 로그아웃 로직 (토큰 삭제 및 화면 이동)
         */
        private void performLogout() {
            if (getContext() == null) return;

            // 1. SharedPreferences에서 토큰을 삭제합니다.
            // (LoginActivity에서 사용한 이름 "app_prefs"와 "jwt_token" 사용)
            SharedPreferences prefs = getContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            editor.remove("jwt_token"); // ⬅️ 저장된 토큰 삭제
            editor.apply(); // 변경사항 저장

            Toast.makeText(getContext(), "로그아웃되었습니다.", Toast.LENGTH_SHORT).show();

            // 2. LoginActivity로 이동합니다.
            Intent intent = new Intent(getActivity(), LoginActivity.class);

            // 3. [중요] MainActivity로 돌아올 수 없도록 "백 스택"을 모두 제거합니다.
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);

            // 4. 현재 Activity(MainActivity)를 즉시 종료합니다.
            if (getActivity() != null) {
                getActivity().finish();
            }
        }
    }

