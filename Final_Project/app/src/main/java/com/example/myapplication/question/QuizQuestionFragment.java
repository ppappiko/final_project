package com.example.myapplication.question;

import android.os.Bundle;
import android.util.Log;
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

import java.io.Serializable; // Serializable import
import java.util.ArrayList;
import java.util.List;

public class QuizQuestionFragment extends Fragment {

    private static final String TAG = "QuizQuestionFragment";

    private List<Question> questionList = new ArrayList<>(); // 문제 목록 저장
    private int currentQuestionIndex = 0; // 현재 보여주는 문제의 인덱스
    private int[] userAnswers; // 사용자의 답안을 저장할 배열

    private int correctAnswers = 0;

    // UI 요소 변수
    private TextView tvQuestion;
    private RadioGroup radioGroupOptions;
    private RadioButton rbOption1, rbOption2, rbOption3, rbOption4;
    private Button btnPrev, btnNext;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 프래그먼트 생성 시, Bundle에서 문제 목록을 꺼냅니다.
        if (getArguments() != null) {
            questionList = (List<Question>) getArguments().getSerializable("questionList");
            if (questionList == null) {
                questionList = new ArrayList<>(); // null 방지
                Log.e(TAG, "Bundle에서 questionList를 가져오지 못했습니다.");
            } else {
                // 문제 목록 크기만큼 사용자 답안 저장 배열 초기화 (-1은 아직 선택 안 함을 의미)
                userAnswers = new int[questionList.size()];
                for (int i = 0; i < userAnswers.length; i++) {
                    userAnswers[i] = -1;
                }
            }
        } else {
            Log.e(TAG, "Bundle이 null입니다.");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz_question, container, false);

        // UI 요소 초기화
        tvQuestion = view.findViewById(R.id.tv_question);
        radioGroupOptions = view.findViewById(R.id.radio_group_options);
        rbOption1 = view.findViewById(R.id.rb_option1);
        rbOption2 = view.findViewById(R.id.rb_option2);
        rbOption3 = view.findViewById(R.id.rb_option3);
        rbOption4 = view.findViewById(R.id.rb_option4);
        btnPrev = view.findViewById(R.id.btn_prev);
        btnNext = view.findViewById(R.id.btn_next);

        // 첫 번째 문제 표시 (문제 목록이 비어있지 않다면)
        if (!questionList.isEmpty()) {
            showQuestion(currentQuestionIndex);
        } else {
            // 문제 목록이 비어있을 경우 사용자에게 알림
            Toast.makeText(getContext(), "표시할 문제가 없습니다.", Toast.LENGTH_LONG).show();
        }

        // '다음 문제' 버튼 클릭 리스너
        btnNext.setOnClickListener(v -> {
            saveUserAnswer(); // 현재 선택한 답 저장

            if (currentQuestionIndex < questionList.size() - 1) {
                // 다음 문제가 있으면 다음 문제 표시
                currentQuestionIndex++;
                showQuestion(currentQuestionIndex);
            } else {
                // 마지막 문제이면 결과 화면으로 이동
                if (getActivity() instanceof QuizActivity) {
                    calculateScoreAndShowResult();
                }
            }
        });

        // '이전 문제' 버튼 클릭 리스너
        btnPrev.setOnClickListener(v -> {
            saveUserAnswer(); // 현재 선택한 답 저장

            if (currentQuestionIndex > 0) {
                // 이전 문제가 있으면 이전 문제 표시
                currentQuestionIndex--;
                showQuestion(currentQuestionIndex);
            }
        });

        return view;
    }

    /** 지정된 인덱스의 문제를 화면에 표시하는 메소드 */
    private void showQuestion(int index) {
        if (index < 0 || index >= questionList.size()) return; // 유효하지 않은 인덱스 방지

        Question currentQuestion = questionList.get(index);
        tvQuestion.setText(currentQuestion.getQuestionText());

        // 선택지 설정 (옵션 개수가 4개 미만일 경우 대비)
        List<String> options = currentQuestion.getOptions();
        RadioButton[] radioButtons = {rbOption1, rbOption2, rbOption3, rbOption4};
        for(int i = 0; i < radioButtons.length; i++) {
            if (i < options.size()) {
                radioButtons[i].setText(options.get(i));
                radioButtons[i].setVisibility(View.VISIBLE);
            } else {
                radioButtons[i].setVisibility(View.GONE); // 옵션이 없으면 숨김
            }
        }

        // 이전에 선택했던 답이 있으면 표시, 없으면 선택 해제
        if (userAnswers[index] != -1) {
            RadioButton savedButton = (RadioButton) radioGroupOptions.getChildAt(userAnswers[index]);
            if (savedButton != null) savedButton.setChecked(true);
        } else {
            radioGroupOptions.clearCheck();
        }

        // 버튼 상태 업데이트 (첫 문제면 '이전' 비활성화)
        btnPrev.setEnabled(index > 0);
        // 마지막 문제면 '다음' 대신 '결과 보기' 등으로 텍스트 변경 가능
        // btnNext.setText(index == questionList.size() - 1 ? "결과 보기" : "다음 문제 >");
    }

    /** 현재 RadioGroup에서 선택된 답을 userAnswers 배열에 저장하는 메소드 */
    private void saveUserAnswer() {
        int selectedRadioButtonId = radioGroupOptions.getCheckedRadioButtonId();
        if (selectedRadioButtonId != -1) {
            View selectedRadioButton = radioGroupOptions.findViewById(selectedRadioButtonId);
            int selectedIndex = radioGroupOptions.indexOfChild(selectedRadioButton);
            userAnswers[currentQuestionIndex] = selectedIndex;
        } else {
            userAnswers[currentQuestionIndex] = -1; // 선택 안 함
        }
    }

    /** 점수를 계산하고 결과 화면으로 이동하는 메소드 */
    private void calculateScoreAndShowResult() {
        for (int i = 0; i < questionList.size(); i++) {
            if (userAnswers[i] == questionList.get(i).getCorrectAnswerIndex()) {
                correctAnswers++;
            }
        }

        // 결과 데이터를 Bundle에 담아 ResultFragment로 전달
        Bundle resultBundle = new Bundle();
        resultBundle.putInt("totalQuestions", questionList.size());
        resultBundle.putInt("correctAnswers", correctAnswers);
        resultBundle.putSerializable("questionList", (Serializable) questionList); // 문제 목록도 전달
        resultBundle.putIntArray("userAnswers", userAnswers); // 사용자 답안도 전달

        QuizResultFragment resultFragment = new QuizResultFragment();
        resultFragment.setArguments(resultBundle);

        if (getActivity() instanceof QuizActivity) {
            ((QuizActivity) getActivity()).showResultScreen(resultFragment);
        }
    }
}