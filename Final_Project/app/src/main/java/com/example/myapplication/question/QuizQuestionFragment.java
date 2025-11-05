package com.example.myapplication.question;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.Home.Detail.Question.Question;
import com.example.myapplication.R;

import java.io.Serializable;
import java.util.List;

public class QuizQuestionFragment extends Fragment {

    private List<Question> questionList;
    private int currentQuestionIndex = 0;

    private TextView tvQuestionNumber, tvQuestionText;
    private RadioGroup radioGroupOptions;
    private RadioButton radioOption1, radioOption2, radioOption3, radioOption4;
    private Button btnNextQuestion;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 1. 퀴즈 시작 화면(QuizSuccessFragment)으로부터 "questionList"를 받습니다.
        if (getArguments() != null) {
            questionList = (List<Question>) getArguments().getSerializable("questionList");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz_question, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvQuestionNumber = view.findViewById(R.id.tv_question_number);
        tvQuestionText = view.findViewById(R.id.tv_question_text);
        radioGroupOptions = view.findViewById(R.id.radio_group_options);
        radioOption1 = view.findViewById(R.id.radio_option1);
        radioOption2 = view.findViewById(R.id.radio_option2);
        radioOption3 = view.findViewById(R.id.radio_option3);
        radioOption4 = view.findViewById(R.id.radio_option4);
        btnNextQuestion = view.findViewById(R.id.btn_next_question);

        if (questionList == null || questionList.isEmpty()) {
            Toast.makeText(getContext(), "문제를 표시할 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. 첫 번째 문제 표시
        showQuestion(currentQuestionIndex);

        // 3. '다음' 버튼 리스너
        btnNextQuestion.setOnClickListener(v -> {
            saveUserAnswer(); // 3-1. 현재 선택한 답을 Question 객체에 저장

            currentQuestionIndex++;
            if (currentQuestionIndex < questionList.size()) {
                // 3-2. 다음 문제 표시
                showQuestion(currentQuestionIndex);
                if (currentQuestionIndex == questionList.size() - 1) {
                    btnNextQuestion.setText("결과 보기");
                }
            } else {
                // 3-3. 퀴즈 끝! 결과 화면으로 이동
                showQuizResults();
            }
        });
    }

    private void showQuestion(int index) {
        Question currentQuestion = questionList.get(index);

        tvQuestionNumber.setText("Q" + (index + 1));
        tvQuestionText.setText(currentQuestion.getQuestionText());

        List<String> options = currentQuestion.getOptions();
        // (안전장치) 보기 개수에 따라 RadioButton을 보여주거나 숨깁니다.
        radioOption1.setText(options.size() > 0 ? options.get(0) : "");
        radioOption2.setText(options.size() > 1 ? options.get(1) : "");
        radioOption3.setText(options.size() > 2 ? options.get(2) : "");
        radioOption4.setText(options.size() > 3 ? options.get(3) : "");

        radioOption1.setVisibility(options.size() > 0 ? View.VISIBLE : View.GONE);
        radioOption2.setVisibility(options.size() > 1 ? View.VISIBLE : View.GONE);
        radioOption3.setVisibility(options.size() > 2 ? View.VISIBLE : View.GONE);
        radioOption4.setVisibility(options.size() > 3 ? View.VISIBLE : View.GONE);

        // 이전에 선택한 답이 있으면 복원, 없으면 초기화
        int userAnswer = currentQuestion.getUserAnswer();
        if (userAnswer == 0) radioGroupOptions.check(R.id.radio_option1);
        else if (userAnswer == 1) radioGroupOptions.check(R.id.radio_option2);
        else if (userAnswer == 2) radioGroupOptions.check(R.id.radio_option3);
        else if (userAnswer == 3) radioGroupOptions.check(R.id.radio_option4);
        else radioGroupOptions.clearCheck();
    }

    private void saveUserAnswer() {
        int selectedRadioButtonId = radioGroupOptions.getCheckedRadioButtonId();
        int userAnswerIndex = -1;

        if (selectedRadioButtonId == R.id.radio_option1) userAnswerIndex = 0;
        else if (selectedRadioButtonId == R.id.radio_option2) userAnswerIndex = 1;
        else if (selectedRadioButtonId == R.id.radio_option3) userAnswerIndex = 2;
        else if (selectedRadioButtonId == R.id.radio_option4) userAnswerIndex = 3;

        // Question 객체에 사용자의 답을 저장합니다.
        questionList.get(currentQuestionIndex).setUserAnswer(userAnswerIndex);
    }

    private void showQuizResults() {
        Bundle bundle = new Bundle();
        // 사용자의 답이 모두 저장된 리스트를 "resultList" 키로 넘깁니다.
        bundle.putSerializable("resultList", (Serializable) questionList);

        QuizResultFragment resultFragment = new QuizResultFragment();
        resultFragment.setArguments(bundle);

        if (getActivity() instanceof QuizActivity) {
            ((QuizActivity) getActivity()).showResultScreen(resultFragment);
        }
    }
}