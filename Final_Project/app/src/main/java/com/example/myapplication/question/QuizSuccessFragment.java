package com.example.myapplication.question;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.Home.Detail.Question.Question;
import com.example.myapplication.R;

import java.io.Serializable;
import java.util.List;

public class QuizSuccessFragment extends Fragment {

    private List<Question> questionList;
    private Button btnStartQuiz;
    private TextView tvQuestionCount;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 1. 로딩 화면(QuizLoadingFragment)으로부터 "questionList"를 받습니다.
        if (getArguments() != null) {
            questionList = (List<Question>) getArguments().getSerializable("questionList");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz_success, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnStartQuiz = view.findViewById(R.id.btn_start_quiz);
        tvQuestionCount = view.findViewById(R.id.tv_question_count);

        if (questionList != null) {
            tvQuestionCount.setText("총 " + questionList.size() + " 문제가 생성되었습니다.");
        }

        btnStartQuiz.setOnClickListener(v -> {
            if (questionList == null || questionList.isEmpty()) {
                Toast.makeText(getContext(), "문제 리스트가 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. [퀴즈 시작] -> QuizQuestionFragment로 "questionList"를 그대로 전달
            Bundle bundle = new Bundle();
            bundle.putSerializable("questionList", (Serializable) questionList);

            QuizQuestionFragment questionFragment = new QuizQuestionFragment();
            questionFragment.setArguments(bundle);

            if (getActivity() instanceof QuizActivity) {
                ((QuizActivity) getActivity()).showQuestionScreen(questionFragment);
            }
        });
    }
}