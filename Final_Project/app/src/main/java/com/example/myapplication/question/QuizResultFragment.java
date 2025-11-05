package com.example.myapplication.question;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.Home.Detail.Question.Question;
import com.example.myapplication.R;

import java.io.Serializable;
import java.util.List;

public class QuizResultFragment extends Fragment {

    private List<Question> resultList;
    private TextView tvScore;
    private Button btnRetry, btnViewAnswers;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 1. 퀴즈 풀이 화면(QuizQuestionFragment)으로부터 "resultList"를 받습니다.
        if (getArguments() != null) {
            resultList = (List<Question>) getArguments().getSerializable("resultList");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvScore = view.findViewById(R.id.tv_score);
        btnRetry = view.findViewById(R.id.btn_retry);
        btnViewAnswers = view.findViewById(R.id.btn_view_answers);

        if (resultList == null) return;

        // 2. 점수 계산 및 표시
        int score = 0;
        for (Question q : resultList) {
            if (q.getUserAnswer() == q.getCorrectAnswerIndex()) {
                score++;
            }
        }
        tvScore.setText(resultList.size() + "문제 중 " + score + "개 정답!");

        // 3. [정답 확인] 버튼 클릭 -> QuizAnswerListFragment로 이동
        btnViewAnswers.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("resultList", (Serializable) resultList); // 리스트 그대로 전달

            QuizAnswerListFragment answerListFragment = new QuizAnswerListFragment();
            answerListFragment.setArguments(bundle);

            if (getActivity() instanceof QuizActivity) {
                ((QuizActivity) getActivity()).showAnswerListScreen(answerListFragment);
            }
        });

        // 4. [다시 풀기] 버튼 클릭 -> QuizQuestionFragment로 이동
        btnRetry.setOnClickListener(v -> {
            // 사용자의 답만 -1로 초기화
            for (Question q : resultList) {
                q.setUserAnswer(-1);
            }
            Bundle bundle = new Bundle();
            // "questionList" 키로 초기화된 리스트 전달
            bundle.putSerializable("questionList", (Serializable) resultList);

            QuizQuestionFragment questionFragment = new QuizQuestionFragment();
            questionFragment.setArguments(bundle);

            if (getActivity() instanceof QuizActivity) {
                ((QuizActivity) getActivity()).showQuestionScreen(questionFragment);
            }
        });
    }
}