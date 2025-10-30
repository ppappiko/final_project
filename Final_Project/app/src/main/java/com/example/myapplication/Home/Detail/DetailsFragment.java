package com.example.myapplication.Home.Detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class DetailsFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private TextView tvTitle, tvDate;
    private String recordingFilePath;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);
        tvTitle = view.findViewById(R.id.tv_detail_title);
        tvDate = view.findViewById(R.id.tv_detail_date);

        // HomeFragment에서 전달받은 데이터 표시
        if (getArguments() != null) {
            String title = getArguments().getString("recordingTitle");
            String date = getArguments().getString("recordingDate");
            recordingFilePath = getArguments().getString("recordingFilePath"); // 파일 경로 받기
            tvTitle.setText(title);
            tvDate.setText(date);
        }

        // ViewPager2 어댑터 설정 (파일 경로 전달)
        DetailsViewPagerAdapter adapter = new DetailsViewPagerAdapter(this, recordingFilePath);
        viewPager.setAdapter(adapter);

        // TabLayout과 ViewPager2 연결
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("대화 내용");
            } else {
                tab.setText("AI 요약");
            }
        }).attach();
    }
}
