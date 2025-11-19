package com.example.myapplication.question;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuizLoadingFragment extends Fragment {

    private UserService userService;


    private String textFromFile = ""; 

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String filePath = getArguments().getString("filePath");
            Log.d("DEBUG_PATH", "Fragment가 받은 경로: " + filePath);
            if (filePath != null) {
                textFromFile = readTextFromFile(filePath);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz_loading, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ApiClient의 공개 메소드를 사용하여 UserService 초기화
        userService = ApiClient.getUserService();

        if (textFromFile.isEmpty()) {
            Toast.makeText(getContext(), "파일을 읽는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) getActivity().finish();
            return;
        }

        SharedPreferences prefs = getContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);

        if (token == null || token.isEmpty()) {
            Toast.makeText(getContext(), "로그인 정보가 없습니다. 다시 로그인해주세요.", Toast.LENGTH_LONG).show();

            Log.e("AuthError", "가져온 토큰이 유효하지 않음: " + token);

            if (getActivity() != null) getActivity().finish();
            return;
        }

        Log.d("AuthDebug", "서버로 보낼 토큰 (앞 10자리): " + token.substring(0, Math.min(token.length(), 10)));

        String authToken = "Bearer " + token;

        HashMap<String, String> requestBody = new HashMap<>();
        requestBody.put("text", textFromFile);

        Call<Map<String, List<Question>>> call = userService.generateQuestions(authToken,requestBody);
        call.enqueue(new Callback<Map<String, List<Question>>>() {
            @Override
            public void onResponse(Call<Map<String, List<Question>>> call, Response<Map<String, List<Question>>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    List<Question> questionList = response.body().get("questions");

                    if (questionList != null && !questionList.isEmpty()) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("questionList", (Serializable) questionList);

                        QuizSuccessFragment successFragment = new QuizSuccessFragment();
                        successFragment.setArguments(bundle);

                        if (getActivity() instanceof QuizActivity) {
                            ((QuizActivity) getActivity()).showSuccessScreen(successFragment);
                        }
                    } else {
                        Toast.makeText(getContext(), "AI가 문제를 생성하지 못했습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "문제 생성에 실패했습니다. (오류 코드: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                    Log.d("DEBUG_PATH", "문제 생성에 실패했습니다 (오류코드:" + response.code());
                }
            }

            @Override
            public void onFailure(Call<Map<String, List<Question>>> call, Throwable t) {
                Toast.makeText(getContext(), "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("DEBUG_PATH", "네트워크 오류: " + t.getMessage());
            }
        });

    }


    private String readTextFromFile(String filePath) {
        File file = new File(filePath);
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("FileReadError", "파일을 읽을 수 없습니다: " + filePath);
        }
        return text.toString();
    }

}
