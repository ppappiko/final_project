package com.example.myapplication.question;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

public class QuizSuccessFragment extends Fragment {

    // 로딩 프래그먼트에서 전달받은 Bundle을 저장할 변수
    private Bundle receivedBundle;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 프래그먼트가 생성될 때 인자(Bundle)를 받아서 저장합니다.
        if (getArguments() != null) {
            receivedBundle = getArguments();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz_success, container, false);

        Button btnStartQuiz = view.findViewById(R.id.btn_start_quiz);
        // (TextView 등 다른 UI 요소들도 여기서 findViewById로 찾습니다.)

        btnStartQuiz.setOnClickListener(v -> {
            // '퀴즈 시작' 버튼을 누르면
            QuizQuestionFragment questionFragment = new QuizQuestionFragment();
            // 저장해 두었던 Bundle을 Question 프래그먼트에 다시 설정합니다.
            questionFragment.setArguments(receivedBundle);

            // QuizActivity의 메소드를 호출하여 화면 전환
            if (getActivity() instanceof QuizActivity) {
                ((QuizActivity) getActivity()).showQuestionScreen(questionFragment);
            }
        });

        return view;
    }
}
