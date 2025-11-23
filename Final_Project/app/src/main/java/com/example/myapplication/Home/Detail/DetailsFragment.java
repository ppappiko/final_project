package com.example.myapplication.Home.Detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.Home.Detail.Summary.SummaryFragment;
import com.example.myapplication.Home.Detail.Transcript.TranscriptFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class DetailsFragment extends Fragment {

    private ViewPager2 viewPager;
    private String recordingFilePath;
    private TabLayout tabLayout;

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
                updateActionButtonsVisibility();
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
        updateActionButtonsVisibility();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showActionButtons(false, false, false);
        }
    }

    public void updateActionButtonsVisibility() {
        if (getActivity() instanceof MainActivity && isAdded()) {
            viewPager.post(() -> {
                Fragment currentItem = getChildFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
                boolean showRefresh = false;
                boolean showRegenerate = false;
                boolean showGenerateQuiz = false;

                if (currentItem instanceof TranscriptFragment) {
                    showRefresh = ((TranscriptFragment) currentItem).isShowingResult();
                    showGenerateQuiz = ((TranscriptFragment) currentItem).isShowingResult();
                } else if (currentItem instanceof SummaryFragment) {
                    showRegenerate = ((SummaryFragment) currentItem).isShowingResult();
                }

                ((MainActivity) getActivity()).showActionButtons(showRefresh, showRegenerate, showGenerateQuiz);
            });
        }
    }

    public void requestRefreshToChild() {
        Fragment currentItem = getChildFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
        if (currentItem instanceof TranscriptFragment) {
            ((TranscriptFragment) currentItem).handleRefreshRequest();
        }
    }

    public void requestRegenerateSummaryToChild() {
        Fragment currentItem = getChildFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
        if (currentItem instanceof SummaryFragment) {
            ((SummaryFragment) currentItem).handleRegenerateRequest();
        }
    }

    public void requestGenerateQuizToChild() {
        Toast.makeText(getContext(), "문제 생성 버튼이 눌렸습니다.", Toast.LENGTH_SHORT).show();
    }
}
