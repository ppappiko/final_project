package com.example.myapplication.Home.Detail.Summary;

import android.os.Bundle;
// Handler, Looper import는 삭제해도 됩니다.
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.ApiClient;
import com.example.myapplication.R;
import com.example.myapplication.User.UserService;

import java.util.HashMap; // HashMap import 추가
import java.util.Map; // Map import 추가

import retrofit2.Call; // Retrofit import 추가
import retrofit2.Callback; // Retrofit import 추가
import retrofit2.Response; // Retrofit import 추가

public class SummaryFragment extends Fragment {

    private Button btnSummarize;
    private ProgressBar progressBar;
    private TextView tvSummaryResult;

    // UserService 인스턴스를 가져옵니다.
    private UserService userService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_summary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnSummarize = view.findViewById(R.id.btn_summarize);
        progressBar = view.findViewById(R.id.progress_bar);
        tvSummaryResult = view.findViewById(R.id.tv_summary_result);

        // ApiClient를 통해 UserService 인스턴스 생성
        userService = ApiClient.getClient().create(UserService.class);

        btnSummarize.setOnClickListener(v -> {
            // 버튼 숨기고 로딩 시작
            btnSummarize.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            tvSummaryResult.setText(""); // 이전 결과 지우기

            // 서버로 보낼 텍스트 (★★★ 중요: 실제로는 녹음 파일의 전체 텍스트를 사용해야 함 ★★★)
            String dummyText = "안녕하세요 반갑습니다 저는 이민호라고 하고요 저는 사람입니다. 저는 나사렛대학교를 다니고 있으며, 취직을 하기위해 작품을 만들고 있습니다.";

            // 요청 본문(Body) 생성
            HashMap<String, String> requestBody = new HashMap<>();
            requestBody.put("text", dummyText);

            // 서버에 요약 요청 보내기
            Call<Map<String, String>> call = userService.summarizeText(requestBody);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    progressBar.setVisibility(View.GONE); // 로딩 끝내기
                    if (response.isSuccessful() && response.body() != null) {
                        // 성공 시, 응답에서 "summary" 키로 요약 텍스트 추출
                        String summary = response.body().get("summary");
                        tvSummaryResult.setText(summary); // TextView에 결과 표시
                    } else {
                        // 서버 응답 오류 시
                        tvSummaryResult.setText("요약에 실패했습니다. (오류 코드: " + response.code() + ")");
                        btnSummarize.setVisibility(View.VISIBLE); // 버튼 다시 보이게
                    }
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    progressBar.setVisibility(View.GONE); // 로딩 끝내기
                    // 네트워크 통신 실패 시
                    tvSummaryResult.setText("네트워크 오류가 발생했습니다.\n" + t.getMessage());
                    btnSummarize.setVisibility(View.VISIBLE); // 버튼 다시 보이게
                }
            });
        });
    }
}