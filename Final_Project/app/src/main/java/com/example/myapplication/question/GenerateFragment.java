package com.example.myapplication.question;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Home.Detail.DetailsFragment;
import com.example.myapplication.Home.HomeRecyclerAdapter;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.Recording;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GenerateFragment extends Fragment {

    private static final String TAG = "GenerateFragment";

    private RecyclerView recyclerView;
    private HomeRecyclerAdapter adapter;
    private List<Recording> recordingList = new ArrayList<>();
    private TextView tvEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_generate, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: GenerateFragment가 생성되었습니다.");

        // --- 올바른 실행 순서 ---

        // 1. UI 요소들을 먼저 찾습니다.
        recyclerView = view.findViewById(R.id.recentList);
        tvEmpty = view.findViewById(R.id.tv_empty);

        // 2. 데이터 목록(recordingList)을 사용해 어댑터를 '생성(초기화)'합니다.
        adapter = new HomeRecyclerAdapter(recordingList);

        // 3. 생성된 어댑터를 RecyclerView와 연결합니다.
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // 4. '생성된' 어댑터에 클릭 리스너를 설정합니다. (이제 adapter는 null이 아님)
        adapter.setOnItemClickListener(item -> {
            // QuizActivity로 이동하기 위한 Intent 생성
            Intent intent = new Intent(getActivity(), QuizActivity.class);

            // (선택) 클릭한 파일의 정보를 QuizActivity로 전달할 수 있습니다.
            // intent.putExtra("file_title", item.getTitle());
            // intent.putExtra("file_path", "경로 정보...");

            // QuizActivity 시작
            startActivity(intent);
        });

        // 5. 마지막으로 파일 목록을 불러와서 화면을 갱신합니다.
        // onResume에서도 호출되므로 여기서 꼭 필요하지 않을 수 있지만,
        // 초기 로딩을 위해 두는 것이 좋습니다.
        loadRecordingsFromStorage();
    }

    // 화면이 다시 보일 때마다 목록을 새로고침
    @Override
    public void onResume() {
        super.onResume();
        loadRecordingsFromStorage();
    }

    private void loadRecordingsFromStorage() {
        Log.d(TAG, "내부 저장소에서 파일 읽기를 시작합니다.");

        // 권한이 필요 없는 '내부 저장소' 경로를 지정합니다.
        File recordingsDir = getContext().getExternalFilesDir(null);
        Log.d(TAG, "검색할 폴더 경로: " + recordingsDir.getAbsolutePath());

        recordingList.clear(); // 목록을 새로 채우기 전에 항상 비웁니다.

        if (recordingsDir.exists()) {
            File[] files = recordingsDir.listFiles();
            if (files != null) {
                Log.d(TAG, "발견된 파일 개수: " + files.length);

                Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

                for (File file : files) {
                    if (file.getName().endsWith(".mp3")) {
                        String title = file.getName().replace(".mp3", "");
                        String date = new SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(new Date(file.lastModified()));
                        int problemCount = 0; // 임시 값
                        recordingList.add(new Recording(title, date, problemCount));
                    }
                }
            }
        } else {
            Log.d(TAG, "오류가 났습니다.");
        }

        // 목록이 비어있는지 확인하고, 그에 따라 UI를 업데이트합니다.
        if (recordingList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }

        adapter.notifyDataSetChanged();
        Log.d(TAG, "목록 새로고침 완료. 최종 크기: " + recordingList.size());
    }
}