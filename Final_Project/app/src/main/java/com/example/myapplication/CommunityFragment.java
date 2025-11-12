package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.community.CommunityViewPagerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class CommunityFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_community, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TabLayout tabLayout = view.findViewById(R.id.community_tab_layout);
        ViewPager2 viewPager = view.findViewById(R.id.community_view_pager);
        FloatingActionButton fab = view.findViewById(R.id.fab_create_post);

        // 어댑터 설정
        CommunityViewPagerAdapter adapter = new CommunityViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // 탭과 뷰페이저 연결
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("질문하기");
                    break;
                case 1:
                    tab.setText("음성/문제 공유");
                    break;
                case 2:
                    tab.setText("자유게시판");
                    break;
            }
        }).attach();

        // 플로팅 액션 버튼 클릭 리스너
        fab.setOnClickListener(v -> {
            // TODO: 실제 게시물 작성 화면으로 이동하는 로직 구현 예정
            Toast.makeText(getContext(), "새 게시물 작성", Toast.LENGTH_SHORT).show();
        });
    }
}
