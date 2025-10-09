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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz_success, container, false);

        Button btnStartQuiz = view.findViewById(R.id.btn_start_quiz);
        Button btnSolveLater = view.findViewById(R.id.btn_solve_later);

        btnStartQuiz.setOnClickListener(v -> {
            // '시험보러 가기' 클릭 시 문제 풀이 화면으로 이동
            if (getActivity() instanceof QuizActivity) {
                ((QuizActivity) getActivity()).showQuestionScreen();
            }
        });

        btnSolveLater.setOnClickListener(v -> {
            // '나중에 풀기' 클릭 시 액티비티 종료
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        return view;
    }
}
