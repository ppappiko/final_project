package com.example.myapplication.question;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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

        recyclerView = view.findViewById(R.id.recentList);
        tvEmpty = view.findViewById(R.id.tv_empty);

        adapter = new HomeRecyclerAdapter(recordingList);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(getActivity(), QuizActivity.class);
            intent.putExtra("file_title", item.getTitle());
            intent.putExtra("file_path", item.getFilePath());
            startActivity(intent);
        });

        // 아이템 길게 누르기 리스너 설정 (삭제 기능)
        adapter.setOnItemLongClickListener(item -> {
            new AlertDialog.Builder(getContext())
                    .setTitle(item.getTitle() + " 녹음 삭제")
                    .setMessage("이 녹음과 관련된 모든 파일(오디오, 텍스트)을 삭제하시겠습니까?")
                    .setPositiveButton("삭제", (dialog, which) -> {
                        deleteRecording(item);
                    })
                    .setNegativeButton("취소", null)
                    .show();
        });

        loadRecordingsFromStorage();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRecordingsFromStorage();
    }

    private void deleteRecording(Recording recording) {
        File audioFile = new File(recording.getFilePath());
        boolean audioDeleted = false;
        if (audioFile.exists()) {
            audioDeleted = audioFile.delete();
        }

        String textFilePath = recording.getFilePath().replaceAll("\\.m4a$", ".txt");
        File textFile = new File(textFilePath);
        if (textFile.exists()) {
            textFile.delete();
        }

        if (audioDeleted) {
            Toast.makeText(getContext(), "녹음 파일이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
            int position = recordingList.indexOf(recording);
            if (position != -1) {
                recordingList.remove(position);
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(position, recordingList.size());
                updateEmptyView();
            }
        } else {
            Toast.makeText(getContext(), "파일 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadRecordingsFromStorage() {
        recordingList.clear();
        File recordingsDir = getContext().getExternalFilesDir(null);
        if (recordingsDir != null && recordingsDir.exists()) {
            File[] files = recordingsDir.listFiles();
            if (files != null) {
                Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
                for (File file : files) {
                    if (file.getName().endsWith(".m4a")) {
                        String title = file.getName().replace(".m4a", "");
                        String date = new SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(new Date(file.lastModified()));
                        int problemCount = 0; // 임시 값
                        recordingList.add(new Recording(title, date, problemCount, file.getAbsolutePath()));
                    }
                }
            }
        }
        updateEmptyView();
        adapter.notifyDataSetChanged();
    }

    private void updateEmptyView() {
        if (recordingList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }
}
