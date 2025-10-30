package com.example.myapplication.question;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

public class QuizActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        if (savedInstanceState == null) {
            // 시작 시 QuizLoadingFragment를 띄웁니다.
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.quiz_frame, new QuizLoadingFragment())
                    .commit();
        }
    }

    // 로딩 완료 후 성공 화면을 띄우는 메소드 (Bundle과 함께)
    public void showSuccessScreen(Fragment successFragmentWithArgs) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.quiz_frame, successFragmentWithArgs)
                .commit();
    }

    // 퀴즈 시작 시 문제 화면을 띄우는 메소드 (Bundle과 함께)
    public void showQuestionScreen(Fragment questionFragmentWithArgs) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.quiz_frame, questionFragmentWithArgs)
                .commit();
    }

    // 퀴즈 완료 시 결과 화면을 띄우는 메소드 (Bundle과 함께)
    public void showResultScreen(Fragment resultFragmentWithArgs) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.quiz_frame, resultFragmentWithArgs)
                .commit();
    }
}
