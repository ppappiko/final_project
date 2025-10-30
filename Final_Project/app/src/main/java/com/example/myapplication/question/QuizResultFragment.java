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

public class QuizResultFragment extends Fragment {

    private int totalQuestions = 0;
    private int correctAnswers = 0;
    private List<Question> questionList; // 정답 보기를 위해 문제 목록 받기
    private int[] userAnswers; // 정답 보기를 위해 사용자 답안 받기

    // UI 요소 변수
    private TextView tvScore;
    private Button btnRetry, btnViewAnswers;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 프래그먼트 생성 시, Bundle에서 결과 데이터를 꺼냅니다.
        if (getArguments() != null) {
            totalQuestions = getArguments().getInt("totalQuestions", 0);
            correctAnswers = getArguments().getInt("correctAnswers", 0);
            questionList = (List<Question>) getArguments().getSerializable("questionList");
            userAnswers = getArguments().getIntArray("userAnswers");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz_result, container, false);

        // UI 요소 초기화
        tvScore = view.findViewById(R.id.tv_score);
        btnRetry = view.findViewById(R.id.btn_retry);
        btnViewAnswers = view.findViewById(R.id.btn_view_answers);

        // 점수 텍스트 설정
        String scoreText = totalQuestions + "문제 중 " + correctAnswers + "문제 맞추셨어요!\n한번 더 풀어보시겠어요?";
        tvScore.setText(scoreText);

        // '다시 풀어보기' 버튼 클릭 리스너
        btnRetry.setOnClickListener(v -> {
            // QuizQuestionFragment를 새로 생성하여 Bundle을 다시 전달하고 이동
            QuizQuestionFragment questionFragment = new QuizQuestionFragment();
            // 이전 화면(QuizQuestionFragment)에서 전달했던 Bundle을 그대로 다시 전달
            questionFragment.setArguments(getArguments());

            if (getActivity() instanceof QuizActivity) {
                ((QuizActivity) getActivity()).showQuestionScreen(questionFragment);
            }
        });

        // '정답 보기' 버튼 클릭 리스너 (임시 기능)
        btnViewAnswers.setOnClickListener(v -> {
            // TODO: 정답 보기 화면(Activity or Fragment)을 만들고,
            // questionList와 userAnswers 데이터를 전달하여 보여주는 로직 구현 필요
            Toast.makeText(getContext(), "정답 보기 기능 준비 중", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}