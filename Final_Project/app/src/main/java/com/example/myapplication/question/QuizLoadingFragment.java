package com.example.myapplication.question;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.ApiClient;
import com.example.myapplication.Home.Detail.Question.Question;
import com.example.myapplication.R;
import com.example.myapplication.User.UserService;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuizLoadingFragment extends Fragment {

    private UserService userService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz_loading, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userService = ApiClient.getClient().create(UserService.class);

        // 서버로 보낼 더미 텍스트
        String dummyText = "동해물과 백두산이 마르고 닳도록 하느님이 보우하사 우리나라 만세";

        HashMap<String, String> requestBody = new HashMap<>();
        requestBody.put("text", dummyText);

        // 서버에 문제 생성 요청
        Call<Map<String, List<Question>>> call = userService.generateQuestions(requestBody);
        call.enqueue(new Callback<Map<String, List<Question>>>() {
            @Override
            public void onResponse(Call<Map<String, List<Question>>> call, Response<Map<String, List<Question>>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    List<Question> questionList = response.body().get("questions");

                    if (questionList != null && !questionList.isEmpty()) {
                        // Bundle 생성
                        Bundle bundle = new Bundle();
                        // List<Question>은 Serializable이므로 putSerializable로 Bundle에 추가
                        bundle.putSerializable("questionList", (Serializable) questionList);

                        // QuizSuccessFragment를 생성하고 Bundle을 인자로 설정
                        QuizSuccessFragment successFragment = new QuizSuccessFragment();
                        successFragment.setArguments(bundle);

                        // QuizActivity의 메소드를 호출하여 화면 전환
                        if (getActivity() instanceof QuizActivity) {
                            ((QuizActivity) getActivity()).showSuccessScreen(successFragment);
                        }
                    } else {
                        Toast.makeText(getContext(), "AI가 문제를 생성하지 못했습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "문제 생성에 실패했습니다. (오류 코드: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, List<Question>>> call, Throwable t) {
                Toast.makeText(getContext(), "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
