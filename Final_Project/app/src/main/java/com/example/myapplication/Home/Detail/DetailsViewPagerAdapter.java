package com.example.myapplication.Home.Detail;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.myapplication.Home.Detail.Summary.SummaryFragment;
import com.example.myapplication.Home.Detail.Transcript.TranscriptFragment;

public class DetailsViewPagerAdapter extends FragmentStateAdapter {

    public DetailsViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // 위치에 따라 다른 프래그먼트 반환
        if (position == 0) {
            return new TranscriptFragment();
        } else {
            return new SummaryFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // 탭 개수
    }
}
