package com.example.myapplication.question;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

public class QuizActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        String filePath = getIntent().getStringExtra("file_path");

        Log.d("DEBUG_PATH", "QuizActivity가 받은 경로: " + filePath);

        if (savedInstanceState == null) {
            // 2. QuizLoadingFragment를 생성합니다.
            QuizLoadingFragment loadingFragment = new QuizLoadingFragment();

            // 3. Bundle을 만들어 파일 경로를 담습니다.
            Bundle bundle = new Bundle();
            bundle.putString("file_path", filePath);

            // 4. Fragment에 Bundle을 인자로 설정합니다.
            loadingFragment.setArguments(bundle);

            // 5. Fragment를 화면에 표시합니다.
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.quiz_frame, loadingFragment)
                    .commit();
        }
    }

    /** 퀴즈 생성 완료/시작 화면 띄우기 */
    public void showSuccessScreen(Fragment successFragmentWithArgs) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.quiz_frame, successFragmentWithArgs)
                .commit();
    }

    /** 퀴즈 풀기 화면 띄우기 */
    public void showQuestionScreen(Fragment questionFragmentWithArgs) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.quiz_frame, questionFragmentWithArgs)
                .commit();
    }

    /** 퀴즈 결과(점수) 화면 띄우기 */
    public void showResultScreen(Fragment resultFragmentWithArgs) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.quiz_frame, resultFragmentWithArgs)
                .commit();
    }

    /** 최종 정답/오답 상세 목록 화면 띄우기 */
    public void showAnswerListScreen(Fragment answerListFragmentWithArgs) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.quiz_frame, answerListFragmentWithArgs)
                .addToBackStack(null) // 뒤로가기 시 점수 화면으로 돌아옴
                .commit();
    }
}
