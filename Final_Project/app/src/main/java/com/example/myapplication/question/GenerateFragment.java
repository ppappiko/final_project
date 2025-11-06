package com.example.myapplication.question;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
            // 1. 오디오 파일 경로를 가져옵니다.
            String audioPath = item.getFilePath(); // 예: ".../file.m4a"

            // 2. 오디오 경로를 텍스트 파일 경로로 변환합니다.
            String textPath = audioPath.replaceAll("\\.m4a$", ".txt"); // 예: ".../file.txt"

            // 3. (중요!) QuizActivity로 변환된 텍스트 파일 경로(textPath)를 넘깁니다.
            Intent intent = new Intent(getActivity(), QuizActivity.class);
            intent.putExtra("file_path", textPath); // ⬅️ audioPath 대신 textPath!

            // (아마 title도 넘겨주고 있을 것입니다)
            // intent.putExtra("file_title", item.getTitle());

            startActivity(intent);
        });

        adapter.setOnItemLongClickListener(item -> {
            final CharSequence[] options = {"이름 변경", "파일 삭제"};

            new AlertDialog.Builder(getContext())
                    .setTitle(item.getTitle())
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            showRenameDialog(item);
                        } else if (which == 1) {
                            showDeleteConfirmationDialog(item);
                        }
                    })
                    .show();
        });

        loadRecordingsFromStorage();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRecordingsFromStorage();
    }

    private void showRenameDialog(final Recording recording) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("이름 변경");

        final EditText input = new EditText(getContext());
        input.setText(recording.getTitle());
        builder.setView(input);

        builder.setPositiveButton("변경", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty() && !newName.equals(recording.getTitle())) {
                renameRecording(recording, newName);
            } else if (newName.isEmpty()) {
                Toast.makeText(getContext(), "이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void renameRecording(Recording recording, String newName) {
        File oldAudioFile = new File(recording.getFilePath());
        File parentDir = oldAudioFile.getParentFile();

        File newAudioFile = new File(parentDir, newName + ".txt");

        if (newAudioFile.exists()) {
            Toast.makeText(getContext(), "이미 존재하는 이름입니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean audioRenamed = oldAudioFile.renameTo(newAudioFile);

        String oldTextFilePath = recording.getFilePath().replaceAll("\\.m4a$", ".txt");
        File oldTextFile = new File(oldTextFilePath);
        if (oldTextFile.exists()) {
            File newTextFile = new File(parentDir, newName + ".txt");
            oldTextFile.renameTo(newTextFile);
        }

        if (audioRenamed) {
            Toast.makeText(getContext(), "이름이 변경되었습니다.", Toast.LENGTH_SHORT).show();
            loadRecordingsFromStorage();
        } else {
            Toast.makeText(getContext(), "이름 변경에 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmationDialog(final Recording recording) {
        new AlertDialog.Builder(getContext())
                .setTitle(recording.getTitle() + " 녹음 삭제")
                .setMessage("이 녹음과 관련된 모든 파일(오디오, 텍스트)을 삭제하시겠습니까?")
                .setPositiveButton("삭제", (dialog, which) -> deleteRecording(recording))
                .setNegativeButton("취소", null)
                .show();
    }

    private void deleteRecording(Recording recording) {
        File audioFile = new File(recording.getFilePath());
        if (audioFile.exists()) {
            audioFile.delete();
        }

        String textFilePath = recording.getFilePath().replaceAll("\\.m4a$", ".txt");
        File textFile = new File(textFilePath);
        if (textFile.exists()) {
            textFile.delete();
        }

        Toast.makeText(getContext(), "녹음 파일이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
        loadRecordingsFromStorage();
    }

    private void loadRecordingsFromStorage() {
        recordingList.clear();
        if (getContext() == null) return;
        File recordingsDir = getContext().getExternalFilesDir(null);
        if (recordingsDir != null && recordingsDir.exists()) {
            File[] files = recordingsDir.listFiles();
            if (files != null) {
                Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
                for (File file : files) {
                    if (file.getName().endsWith(".m4a")) {
                        String title = file.getName().replace(".m4a", "");
                        String date = new SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(new Date(file.lastModified()));
                        String filePath = file.getAbsolutePath();
                        recordingList.add(new Recording(title, date, 0, filePath));
                    }
                }
            }
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        updateEmptyView();
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