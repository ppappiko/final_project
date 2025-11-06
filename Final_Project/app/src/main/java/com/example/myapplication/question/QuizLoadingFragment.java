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
// 💡 참고: Question 클래스의 위치가 question 패키지 안쪽이 아니라 Home/Detail... 이네요!
// 나중에 com.example.myapplication.question 패키지 안으로 옮기면 더 깔끔할 수 있습니다.
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


    private String textFromFile = ""; // 파일에서 읽은 텍스트를 저장할 변수

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 프래그먼트 생성 시, Bundle에서 파일 경로를 꺼냅니다.
        if (getArguments() != null) {
            String filePath = getArguments().getString("filePath");
            Log.d("DEBUG_PATH", "Fragment가 받은 경로: " + filePath);
            if (filePath != null) {
                // 파일 경로를 이용해 텍스트를 읽어옵니다.
                // (이제 readTextFromFile 메소드를 정상적으로 찾을 수 있습니다.)
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

        userService = ApiClient.getClient().create(UserService.class);

        //  파일 읽기 실패 시 여기서 먼저 처리
        if (textFromFile.isEmpty()) {
            Toast.makeText(getContext(), "파일을 읽는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) getActivity().finish();
            return;
        }

        // 1. SharedPreferences에서 저장된 로그인 토큰 가져오기
        // (주의: "my_prefs_name"과 "auth_token"은 로그인 시 저장했던 키와 동일해야 함)
        SharedPreferences prefs = getContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);

        // 2. 토큰이 없으면 로그인 화면으로 보내거나 오류 처리
        if (token == null || token.isEmpty()) {
            Toast.makeText(getContext(), "로그인 정보가 없습니다. 다시 로그인해주세요.", Toast.LENGTH_LONG).show();

            Log.e("AuthError", "가져온 토큰이 유효하지 않음: " + token);

            if (getActivity() != null) getActivity().finish();
            return;
        }

        // 3. [추가] 실제 서버로 보낼 토큰 값을 로그로 확인 (보안상 앞 10자리만)
        Log.d("AuthDebug", "서버로 보낼 토큰 (앞 10자리): " + token.substring(0, Math.min(token.length(), 10)));

        // 3. 서버가 "Bearer [토큰]" 형식을 요구할 수 있음 (서버 설정 확인)
        String authToken = "Bearer " + token;

        HashMap<String, String> requestBody = new HashMap<>();
        requestBody.put("text", textFromFile);

        // 서버에 문제 생성 요청
        Call<Map<String, List<Question>>> call = userService.generateQuestions(authToken,requestBody);
        call.enqueue(new Callback<Map<String, List<Question>>>() {
            @Override
            public void onResponse(Call<Map<String, List<Question>>> call, Response<Map<String, List<Question>>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    List<Question> questionList = response.body().get("questions");

                    if (questionList != null && !questionList.isEmpty()) {
                        // Bundle 생성
                        Bundle bundle = new Bundle();
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


    // ▼▼▼ 여기에 메소드가 있어야 합니다! ▼▼▼
    /**파일 경로를 받아 텍스트 내용을 읽어오는 메소드*/
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