package com.example.myapplication.Home.Detail.Summary;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

public class SummaryFragment extends Fragment {

    private Button btnSummarize;
    private ProgressBar progressBar;
    private TextView tvSummaryResult;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_summary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnSummarize = view.findViewById(R.id.btn_summarize);
        progressBar = view.findViewById(R.id.progress_bar);
        tvSummaryResult = view.findViewById(R.id.tv_summary_result);

        btnSummarize.setOnClickListener(v -> {
            // 버튼 숨기고 프로그레스바 보여주기
            btnSummarize.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            // AI가 요약하는 것처럼 2초 딜레이
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                // 프로그레스바 숨기기
                progressBar.setVisibility(View.GONE);
                // 요약 결과 텍스트 설정
                String summaryText = "애국가는 대한민국의 자연과 국민의 기상을 찬양하고, 나라의 번영과 보존을 기원하는 내용입니다.\n\n" +
                        "• 1절: 동해와 백두산이 변치 않고, 하느님의 보우 속에 나라가 영원하기를 바람.\n" +
                        "• 2절: 남산의 소나무처럼 굳세고 변치 않는 기상을 지닌 국민을 노래함.\n" +
                        "• 3절: 맑고 높은 가을 하늘과 밝은 달처럼 국민의 마음은 변함없는 충성을 지님을 표현함.";
                tvSummaryResult.setText(summaryText);
            }, 2000); // 2000ms = 2초
        });
    }
}
