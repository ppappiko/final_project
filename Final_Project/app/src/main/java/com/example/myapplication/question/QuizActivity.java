package com.example.myapplication.question;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

public class QuizActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // 첫 화면으로 로딩 프래그먼트를 보여줌
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.quiz_frame, new QuizLoadingFragment())
                    .commit();
        }
    }

    // 각 프래그먼트에서 호출할 화면 전환 메소드들
    public void showSuccessScreen() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.quiz_frame, new QuizSuccessFragment())
                .commit();
    }

    public void showQuestionScreen() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.quiz_frame, new QuizQuestionFragment())
                .commit();
    }

    public void showResultScreen() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.quiz_frame, new QuizResultFragment())
                .commit();
    }
}