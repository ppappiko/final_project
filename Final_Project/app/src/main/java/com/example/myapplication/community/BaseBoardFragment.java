package com.example.myapplication.community;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.ApiClient;
import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseBoardFragment extends Fragment {

    protected RecyclerView recyclerView;
    protected PostAdapter adapter;
    protected List<Post> postList = new ArrayList<>();
    protected TextView tvEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_board, container, false); // fragment_board.xml 필요
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.board_recycler_view); // XML ID 확인 필요
        tvEmpty = view.findViewById(R.id.tv_board_empty);

        setupRecyclerView();
    }

    @Override
    public void onResume() {
        super.onResume();
        // 화면이 보일 때마다 서버에서 최신 글을 불러옴
        loadPostsFromServer();
    }

    private void setupRecyclerView() {
        adapter = new PostAdapter(postList); // PostAdapter는 기존 것 사용 (Post 객체 필드명만 맞으면 됨)
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // 클릭 이벤트 등은 여기서 처리
    }

    // ★ 서버에서 데이터 로드 ★
    private void loadPostsFromServer() {
        // 1. 토큰 가져오기
        SharedPreferences prefs = getActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);

        if (token == null) {
            // 토큰이 없으면(비로그인) 처리
            return;
        }

        // 2. API 호출
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        // getCategory()를 통해 현재 프래그먼트의 카테고리(FREE, QNA 등)를 보냄
        Call<List<Post>> call = apiService.getPosts("Bearer " + token, getCategory());

        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    postList.clear();
                    postList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    updateEmptyView();
                }
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                Toast.makeText(getContext(), "목록 불러오기 실패", Toast.LENGTH_SHORT).show();
            }
        });
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

    // ★ 중요: 자식 프래그먼트들이 구현해야 할 메소드 (서버용 카테고리 코드 반환) ★
    protected abstract String getCategory();
}