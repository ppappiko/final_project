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

import com.example.myapplication.Home.HomeRecyclerAdapter;
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

        recyclerView = view.findViewById(R.id.recentList);
        tvEmpty = view.findViewById(R.id.tv_empty);

        adapter = new HomeRecyclerAdapter(recordingList);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(getActivity(), QuizActivity.class);

            // 클릭한 파일의 제목과 경로를 QuizActivity로 전달
            intent.putExtra("file_title", item.getTitle());
            intent.putExtra("file_path", item.getFilePath());

            startActivity(intent);
        });

        loadRecordingsFromStorage();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRecordingsFromStorage();
    }

    private void loadRecordingsFromStorage() {
        Log.d(TAG, "내부 저장소에서 파일 읽기를 시작합니다.");

        File recordingsDir = getContext().getExternalFilesDir(null);
        if (recordingsDir == null) {
            Log.e(TAG, "External files directory not found.");
            return; // Exit if directory is not available
        }
        Log.d(TAG, "검색할 폴더 경로: " + recordingsDir.getAbsolutePath());

        recordingList.clear();

        if (recordingsDir.exists()) {
            File[] files = recordingsDir.listFiles();
            if (files != null) {
                Log.d(TAG, "발견된 파일 개수: " + files.length);

                Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

                for (File file : files) {
                    if (file.getName().endsWith(".m4a")) {
                        String title = file.getName().replace(".m4a", "");
                        String date = new SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(new Date(file.lastModified()));
                        int problemCount = 0; // 임시 값
                        // 파일 경로를 포함하여 Recording 객체 생성
                        recordingList.add(new Recording(title, date, problemCount, file.getAbsolutePath()));
                    }
                }
            }
        } else {
            Log.d(TAG, "오류가 났습니다.");
        }

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
