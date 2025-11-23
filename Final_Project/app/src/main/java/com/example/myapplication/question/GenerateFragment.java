package com.example.myapplication.question;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.example.myapplication.ApiClient;
import com.example.myapplication.Home.HomeRecyclerAdapter;
import com.example.myapplication.R;
import com.example.myapplication.Recording;
import com.example.myapplication.User.UserService;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GenerateFragment extends Fragment {

    private RecyclerView recyclerView;
    private HomeRecyclerAdapter adapter;
    private List<Recording> recordingList = new ArrayList<>();
    private TextView tvEmpty;
    private UserService userService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_generate, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userService = ApiClient.getClient().create(UserService.class);

        recyclerView = view.findViewById(R.id.recentList);
        tvEmpty = view.findViewById(R.id.tv_empty);

        adapter = new HomeRecyclerAdapter(recordingList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // 클릭 시: 이미 문제가 생성된 파일이므로 바로 퀴즈 풀기(QuizActivity)로 이동
        adapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(getActivity(), QuizActivity.class);

            // .m4a -> .txt 경로 변환 (ANR 방지 필수)
            String audioPath = item.getFilePath();
            String textPath = audioPath.replaceAll("\\.m4a$", ".txt");

            intent.putExtra("file_title", item.getTitle());
            intent.putExtra("filePath", textPath); // "filePath" 키로 전달

            startActivity(intent);
        });

        // 롱클릭: 파일 삭제 등 (기존 유지)
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
    }

    @Override
    public void onResume() {
        super.onResume();
        // 화면에 들어올 때마다 서버 목록과 비교하여 갱신
        filterAndLoadMyQuizzes();
    }

    /**
     * [신규] 1단계: 서버에서 '내가 문제를 생성한 파일 목록(Keys)'을 받아옵니다.
     */
    public void filterAndLoadMyQuizzes() {
        // 1. 토큰 로드
        if (getActivity() == null) return;
        SharedPreferences prefs = getActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);

        if (token == null) {
            // 로그인 안 했으면 목록을 비움
            recordingList.clear();
            adapter.notifyDataSetChanged();
            updateEmptyView();
            return;
        }

        // 2. 서버에 목록 요청 (GET /ai/questions/list)
        userService.getMyQuizList("Bearer " + token).enqueue(new Callback<Map<String, List<String>>>() {
            @Override
            public void onResponse(Call<Map<String, List<String>>> call, Response<Map<String, List<String>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 서버가 준 키 목록 (예: ["녹음1", "녹음2"])
                    List<String> serverKeys = response.body().get("keys");

                    // 3. 로컬 파일 스캔 및 필터링 시작
                    loadFilteredFiles(serverKeys);
                } else {
                    // 서버 오류 시 빈 화면
                    recordingList.clear();
                    adapter.notifyDataSetChanged();
                    updateEmptyView();
                }
            }

            @Override
            public void onFailure(Call<Map<String, List<String>>> call, Throwable t) {
                // 네트워크 오류 시 처리 (토스트 등)
            }
        });
    }

    /**
     * [신규] 2단계: 로컬 파일을 스캔하고, 서버 목록에 있는 파일만 골라냅니다. (백그라운드 실행)
     */
    private void loadFilteredFiles(List<String> validKeys) {
        if (validKeys == null || validKeys.isEmpty()) {
            recordingList.clear();
            adapter.notifyDataSetChanged();
            updateEmptyView();
            return;
        }

        // [ANR 방지] 파일 스캔은 백그라운드 스레드에서!
        new Thread(() -> {
            List<Recording> filteredList = new ArrayList<>();
            if (getContext() == null) return;

            File recordingsDir = getContext().getExternalFilesDir(null);

            if (recordingsDir != null && recordingsDir.exists()) {
                File[] files = recordingsDir.listFiles();
                if (files != null) {
                    // 최신순 정렬
                    Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

                    for (File file : files) {
                        // .m4a 파일만 확인
                        if (file.getName().endsWith(".m4a")) {
                            String title = file.getName().replace(".m4a", "");

                            // ★ 핵심: 서버 리스트(validKeys)에 있는 파일만 추가 ★
                            if (validKeys.contains(title)) {
                                String date = new SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(new Date(file.lastModified()));
                                filteredList.add(new Recording(title, date, 0, file.getAbsolutePath()));
                            }
                        }
                    }
                }
            }

            // UI 갱신은 메인 스레드에서
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    recordingList.clear();
                    recordingList.addAll(filteredList);
                    adapter.notifyDataSetChanged();
                    updateEmptyView();
                });
            }
        }).start();
    }

    // --- (이하 기존 파일 관리 메서드들 유지) ---

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
        // (주의: 이름을 바꾸면 서버의 Key와 달라져서 목록에서 사라질 수 있음.
        //  완벽하게 하려면 서버에도 이름 변경 API를 요청해야 하지만, 일단 로컬만 수정)
        File oldAudioFile = new File(recording.getFilePath());
        File parentDir = oldAudioFile.getParentFile();
        File newAudioFile = new File(parentDir, newName + ".m4a");

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
            filterAndLoadMyQuizzes(); // 목록 갱신
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
        // (주의: 로컬 파일만 삭제되고 서버 DB 데이터는 남습니다. 추후 서버 삭제 API 필요)
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
        filterAndLoadMyQuizzes(); // 목록 갱신
    }

    private void updateEmptyView() {
        if (recordingList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setText("생성된 문제가 없습니다."); // 문구 변경
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }
}