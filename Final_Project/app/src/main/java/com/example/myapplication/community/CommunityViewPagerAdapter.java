package com.example.myapplication.community;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class CommunityViewPagerAdapter extends FragmentStateAdapter {

    public CommunityViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new QuestionBoardFragment();
            case 1:
                return new ShareBoardFragment();
            case 2:
                return new FreeBoardFragment();
            default:
                return new QuestionBoardFragment(); // 기본값
        }
    }

    @Override
    public int getItemCount() {
        return 3; // 탭 개수
    }
}
