package com.example.myapplication;

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

public class ProfileFragment extends Fragment {



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
        CardView cardReportError = view.findViewById(R.id.card_report_error);
        TextView btnLogout = view.findViewById(R.id.btn_logout);

        // 각 메뉴 클릭 시 간단한 토스트 메시지를 보여주는 예시
        cardMemberInfo.setOnClickListener(v -> Toast.makeText(getContext(), "회원 정보 클릭됨", Toast.LENGTH_SHORT).show());
        cardFaq.setOnClickListener(v -> Toast.makeText(getContext(), "공지사항 및 FAQ 클릭됨", Toast.LENGTH_SHORT).show());
        cardTerms.setOnClickListener(v -> Toast.makeText(getContext(), "이용 약관 클릭됨", Toast.LENGTH_SHORT).show());
        cardReportError.setOnClickListener(v -> Toast.makeText(getContext(), "오류 제보 클릭됨", Toast.LENGTH_SHORT).show());
        btnLogout.setOnClickListener(v -> Toast.makeText(getContext(), "로그아웃 클릭됨", Toast.LENGTH_SHORT).show());
    }
}
