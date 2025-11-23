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

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.Home.Detail.Transcript.TranscriptFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class DetailsFragment extends Fragment {

    private ViewPager2 viewPager;
    private String recordingFilePath;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);
        TextView tvTitle = view.findViewById(R.id.tv_detail_title);
        TextView tvDate = view.findViewById(R.id.tv_detail_date);

        if (getArguments() != null) {
            String title = getArguments().getString("recordingTitle");
            String date = getArguments().getString("recordingDate");
            recordingFilePath = getArguments().getString("recordingFilePath");
            tvTitle.setText(title);
            tvDate.setText(date);
        }

        DetailsViewPagerAdapter adapter = new DetailsViewPagerAdapter(this, recordingFilePath);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("대화 내용");
            } else {
                tab.setText("AI 요약");
            }
        }).attach();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateActionButtonsVisibility(); // 탭 선택 시 버튼 상태 업데이트
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }
            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateActionButtonsVisibility(); // 화면에 다시 나타날 때 버튼 상태 업데이트
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showActionButtons(false); // 화면을 벗어나면 버튼 숨김
        }
    }

    // 상단 액션 버튼들의 노출 여부를 결정하는 메소드
    public void updateActionButtonsVisibility() {
        if (getActivity() instanceof MainActivity) {
            viewPager.post(() -> {
                Fragment currentItem = getChildFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
                // 현재 탭이 0번째(대화 내용)이고, 받아쓰기 결과가 화면에 표시되고 있을 때만 버튼들을 보여줌
                boolean shouldShow = viewPager.getCurrentItem() == 0 && currentItem instanceof TranscriptFragment && ((TranscriptFragment) currentItem).isShowingResult();
                ((MainActivity) getActivity()).showActionButtons(shouldShow);
            });
        }
    }

    // MainActivity로부터 새로고침 요청을 받았을 때 자식 Fragment로 전달
    public void requestRefreshToChild() {
        Fragment currentItem = getChildFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
        if (currentItem instanceof TranscriptFragment) {
            ((TranscriptFragment) currentItem).handleRefreshRequest();
        }
    }
}
