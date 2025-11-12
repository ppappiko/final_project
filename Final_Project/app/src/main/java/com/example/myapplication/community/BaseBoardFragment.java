package com.example.myapplication.community;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

// 모든 게시판 프래그먼트의 부모 클래스
public abstract class BaseBoardFragment extends Fragment {

    protected RecyclerView recyclerView;
    protected PostAdapter adapter;
    protected List<Post> postList = new ArrayList<>();
    protected TextView tvEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 공통 레이아웃 사용
        return inflater.inflate(R.layout.fragment_board, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.board_recycler_view);
        tvEmpty = view.findViewById(R.id.tv_board_empty);

        setupRecyclerView();
        loadDummyData();
    }

    private void setupRecyclerView() {
        adapter = new PostAdapter(postList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // TODO: 아이템 클릭 시 게시물 상세 화면으로 이동하는 로직 추가 예정
        adapter.setOnItemClickListener(post -> {
            // Intent to PostDetailActivity
        });
    }

    // 임시 더미 데이터 로드
    private void loadDummyData() {
        postList.clear();
        for (int i = 1; i <= 10; i++) {
            postList.add(new Post(
                    getBoardName() + " 게시물 제목 " + i,
                    "이것은 " + getBoardName() + " 게시물의 내용 미리보기입니다.",
                    "작성자 " + i,
                    i + "시간 전"
            ));
        }
        updateEmptyView();
        adapter.notifyDataSetChanged();
    }

    private void updateEmptyView() {
        if (postList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }

    // 각 자식 프래그먼트가 자신의 게시판 이름을 반환하도록 함
    protected abstract String getBoardName();
}
