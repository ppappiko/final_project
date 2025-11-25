package com.example.myapplication.question;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.Home.Detail.Question.Question; // Question 모델
import com.example.myapplication.R;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList; // ⬅️ 1. (추가) ArrayList 임포트
import java.util.List;

public class QuizSuccessFragment extends Fragment {

    // 2. (추가) 문제 리스트를 저장할 변수
    private List<Question> questionList;

    private String filePath; // ⬅️ 1. (추가) .txt 파일 경로
    private Button btnStartQuiz, btnRegenerate;;
    private TextView tvQuestionCount, tvSuccessMessage; // ⬅️ 3. (수정) TextView 변수

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 4. [핵심] 로딩 화면(QuizLoadingFragment)으로부터 "questionList"를 받습니다.
        if (getArguments() != null) {
            questionList = (List<Question>) getArguments().getSerializable("questionList");
            filePath = getArguments().getString("filePath");
        }

        // (NPE 방지) 만약 리스트가 null로 오면, 빈 리스트로 초기화
        if (questionList == null) {
            questionList = new ArrayList<>();
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
        btnRegenerate = view.findViewById(R.id.btn_regenerate);
        tvQuestionCount = view.findViewById(R.id.tv_question_count);
        tvSuccessMessage = view.findViewById(R.id.tv_success_message); // (xml에 ID가 이렇다고 가정)

        // 5. [핵심] 전달받은 questionList의 크기(.size())로 TextView의 텍스트를 설정합니다.
        if (!questionList.isEmpty()) {
            tvSuccessMessage.setText("문제 생성이 완료되었습니다!");
            tvQuestionCount.setText("총 " + questionList.size() + " 문제가 생성되었습니다.");
        } else {
            // (혹시 모를 예외 처리)
            tvSuccessMessage.setText("문제 생성 실패");
            tvQuestionCount.setText("AI가 문제를 반환하지 않았습니다.");
        }

        File file = null;
        if (filePath != null) {
            file = new File(filePath);
        }

        // 내 폰에 원본 파일(.txt)이 존재할 때만 '다시 만들기' 버튼을 보여줍니다.
        // (공유받은 문제는 파일이 없으므로 버튼이 숨겨짐)
        if (file != null && file.exists()) {
            btnRegenerate.setVisibility(View.VISIBLE);
        } else {
            btnRegenerate.setVisibility(View.GONE);
        }

        btnStartQuiz.setOnClickListener(v -> {
            if (questionList.isEmpty()) {
                Toast.makeText(getContext(), "시작할 문제가 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }


            // 6. [확인] 퀴즈 풀기 화면으로 "questionList"를 전달
            Bundle bundle = new Bundle();
            bundle.putSerializable("questionList", (Serializable) questionList);

            QuizQuestionFragment questionFragment = new QuizQuestionFragment();
            questionFragment.setArguments(bundle);

            if (getActivity() instanceof QuizActivity) {
                ((QuizActivity) getActivity()).showQuestionScreen(questionFragment);
            }
        });



        btnRegenerate.setOnClickListener(v -> {
            // "몇 문제 만드시겠습니까?" 안내창 띄우기
            showRegenerateDialog();
        });
    }

    /**
     * [신규] '다시 만들기' 시 문제 개수를 묻는 안내창
     */
    private void showRegenerateDialog() {
        if (getContext() == null || filePath == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("문제 다시 만들기");
        builder.setMessage("새로 생성할 문제 개수를 입력하세요.");
        builder.setCancelable(false);

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("예: 5 (기존 " + questionList.size() + "개)");
        builder.setView(input);

        builder.setPositiveButton("생성", (dialog, which) -> {
            int questionCount;
            try { questionCount = Integer.parseInt(input.getText().toString()); }
            catch (NumberFormatException e) { questionCount = 5; }
            if (questionCount <= 0) questionCount = 5;

            // [핵심] 'QuizLoadingFragment'를 "다시 만들기" 모드로 실행
            restartLoadingProcess(questionCount);
        });
        builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /**
     * [신규] 'QuizLoadingFragment'를 '다시 만들기' 모드로 실행
     */
    private void restartLoadingProcess(int questionCount) {
        if (getActivity() == null) return;

        QuizLoadingFragment loadingFragment = new QuizLoadingFragment();

        Bundle bundle = new Bundle();
        bundle.putString("filePath", filePath); // ⬅️ .txt 경로
        bundle.putInt("questionCount", questionCount); // ⬅️ 새로 입력받은 개수

        // ▼▼▼ (7. [핵심] "무조건 생성" 플래그 전달) ▼▼▼
        bundle.putBoolean("forceRegenerate", true);

        loadingFragment.setArguments(bundle);

        // (QuizActivity의 showSuccessScreen을 재활용하여 로딩 화면으로 교체)
        ((QuizActivity) getActivity()).showSuccessScreen(loadingFragment);
    }
}
