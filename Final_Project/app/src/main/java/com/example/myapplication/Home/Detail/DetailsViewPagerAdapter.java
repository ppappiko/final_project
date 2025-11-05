package com.example.myapplication.Home.Detail;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.myapplication.Home.Detail.Summary.SummaryFragment;
import com.example.myapplication.Home.Detail.Transcript.TranscriptFragment;

public class DetailsViewPagerAdapter extends FragmentStateAdapter {

    private Bundle bundle;

    public DetailsViewPagerAdapter(@NonNull Fragment fragment, Bundle bunble) {
        super(fragment);
        this.bundle = bunble;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // 위치에 따라 다른 프래그먼트 반환
        Fragment fragment;
        if (position == 0) {
            fragment = new TranscriptFragment();
        } else {
            fragment = new SummaryFragment();
        }

        // 프래그먼트에 파일 경로를 Argument로 전달
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2; // 탭 개수
    }
}
