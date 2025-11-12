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

            // --- 누락되었던 코드 추가 ---
            tvTitle.setText(title);
            tvDate.setText(date);
            // --------------------------
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
                updateRefreshButtonVisibility();
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
        updateRefreshButtonVisibility();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showRefreshButton(false);
        }
    }

    public void updateRefreshButtonVisibility() {
        if (getActivity() instanceof MainActivity) {
            viewPager.post(() -> {
                Fragment currentItem = getChildFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
                boolean shouldShow = viewPager.getCurrentItem() == 0 && currentItem instanceof TranscriptFragment && ((TranscriptFragment) currentItem).isShowingResult();
                ((MainActivity) getActivity()).showRefreshButton(shouldShow);
            });
        }
    }

    public void requestRefreshToChild() {
        Fragment currentItem = getChildFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
        if (currentItem instanceof TranscriptFragment) {
            ((TranscriptFragment) currentItem).handleRefreshRequest();
        }
    }
}
