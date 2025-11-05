package com.example.myapplication.question;

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

import com.example.myapplication.Home.Detail.Question.Question;
import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class QuizAnswerListFragment extends Fragment {

    private List<Question> resultList = new ArrayList<>();
    private RecyclerView recyclerView;
    private QuizAnswerListAdapter adapter;
    private TextView tvScoreTitle;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 1. QuizResultFragment로부터 "resultList"를 받습니다.
        if (getArguments() != null) {
            resultList = (List<Question>) getArguments().getSerializable("resultList");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz_answer_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.rv_answer_list);
        tvScoreTitle = view.findViewById(R.id.tv_answer_list_title);

        // 2. 점수 계산
        int score = 0;
        for (Question q : resultList) {
            if (q.getUserAnswer() == q.getCorrectAnswerIndex()) {
                score++;
            }
        }
        tvScoreTitle.setText("상세 결과 (" + resultList.size() + "문제 중 " + score + "개 정답)");

        // 3. 어댑터 설정
        adapter = new QuizAnswerListAdapter(resultList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }
}