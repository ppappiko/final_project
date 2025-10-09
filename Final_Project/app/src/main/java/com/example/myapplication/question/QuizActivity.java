package com.example.myapplication.question;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

public class QuizActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // 액티비티가 처음 생성될 때만 첫 프래그먼트를 추가
        // (화면 회전 등에도 프래그먼트가 중복 생성되지 않도록 함)
        if (savedInstanceState == null) {
            // 첫 화면으로 로딩 프래그먼트를 보여줌
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.quiz_frame, new QuizLoadingFragment())
                    .commit();
        }
    }

    /** QuizLoadingFragment가 호출할 메소드 */
    public void showSuccessScreen() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.quiz_frame, new QuizSuccessFragment())
                .commit();
    }

    /** QuizSuccessFragment 또는 QuizResultFragment가 호출할 메소드 */
    public void showQuestionScreen() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.quiz_frame, new QuizQuestionFragment())
                .commit();
    }

    /** QuizQuestionFragment가 호출할 메소드 */
    public void showResultScreen() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.quiz_frame, new QuizResultFragment())
                .commit();
    }
}
