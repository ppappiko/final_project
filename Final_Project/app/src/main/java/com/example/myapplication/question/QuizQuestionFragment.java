package com.example.myapplication.question;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class QuizQuestionFragment extends Fragment {

    // 더미 데이터를 위한 Question 클래스 (Fragment 파일 밖에 별도로 만들거나 내부에 만들어도 됨)
    private static class Question {
        String questionText;
        String[] options;
        int correctAnswerIndex;
        Question(String questionText, String[] options, int correctAnswerIndex) {
            this.questionText = questionText;
            this.options = options;
            this.correctAnswerIndex = correctAnswerIndex;
        }
    }

    private List<Question> questionList = new ArrayList<>();
    private int currentQuestionIndex = 0;

    private TextView tvQuestion;
    private RadioGroup radioGroup;
    private RadioButton rbOption1, rbOption2, rbOption3, rbOption4;
    private Button btnPrev, btnNext;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz_question, container, false);

        // 더미 데이터 생성
        createDummyQuestions();

        // UI 요소 초기화
        tvQuestion = view.findViewById(R.id.tv_question);
        radioGroup = view.findViewById(R.id.radio_group_options);
        rbOption1 = view.findViewById(R.id.rb_option1);
        rbOption2 = view.findViewById(R.id.rb_option2);
        rbOption3 = view.findViewById(R.id.rb_option3);
        rbOption4 = view.findViewById(R.id.rb_option4);
        btnPrev = view.findViewById(R.id.btn_prev);
        btnNext = view.findViewById(R.id.btn_next);

        // 첫 번째 문제 표시
        showQuestion(currentQuestionIndex);

        // '다음' 버튼 리스너
        btnNext.setOnClickListener(v -> {
            if (currentQuestionIndex < questionList.size() - 1) {
                currentQuestionIndex++;
                showQuestion(currentQuestionIndex);
            } else {
                // 마지막 문제이면 결과 화면으로 이동
                if (getActivity() instanceof QuizActivity) {
                    ((QuizActivity) getActivity()).showResultScreen();
                }
            }
        });

        // '이전' 버튼 리스너
        btnPrev.setOnClickListener(v -> {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--;
                showQuestion(currentQuestionIndex);
            }
        });

        return view;
    }

    private void createDummyQuestions() {
        questionList.add(new Question("1. 어린이보호구역에서의 속도제한은 몇 Km입니까?", new String[]{"30km", "35km", "45km", "40km"}, 0));
        questionList.add(new Question("2. 대한민국의 수도는 어디입니까?", new String[]{"부산", "서울", "인천", "대전"}, 1));
        questionList.add(new Question("3. 1 + 1 = ?", new String[]{"1", "2", "3", "4"}, 1));
    }

    private void showQuestion(int index) {
        Question currentQuestion = questionList.get(index);
        tvQuestion.setText(currentQuestion.questionText);
        rbOption1.setText(currentQuestion.options[0]);
        rbOption2.setText(currentQuestion.options[1]);
        rbOption3.setText(currentQuestion.options[2]);
        rbOption4.setText(currentQuestion.options[3]);
        radioGroup.clearCheck();
    }
}
