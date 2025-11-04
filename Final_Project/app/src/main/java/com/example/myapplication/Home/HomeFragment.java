package com.example.myapplication.Home;

import android.app.AlertDialog;
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

import com.example.myapplication.Home.Detail.DetailsFragment;
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

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private RecyclerView recyclerView;
    private HomeRecyclerAdapter adapter;
    private List<Recording> recordingList = new ArrayList<>();
    private TextView tvEmpty;

    public static HomeFragment newInstance(String mode) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString("mode", mode);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recentList);
        tvEmpty = view.findViewById(R.id.tv_empty);

        adapter = new HomeRecyclerAdapter(recordingList);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // 아이템 클릭 리스너 설정
        adapter.setOnItemClickListener(item -> {
            DetailsFragment detailsFragment = new DetailsFragment();
            Bundle bundle = new Bundle();
            bundle.putString("recordingTitle", item.getTitle());
            bundle.putString("recordingDate", item.getDate());
            bundle.putString("recordingFilePath", item.getFilePath());
            detailsFragment.setArguments(bundle);

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).replaceFragment(detailsFragment);
            }
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
        // 1. 오디오 파일 삭제
        File audioFile = new File(recording.getFilePath());
        boolean audioDeleted = false;
        if (audioFile.exists()) {
            audioDeleted = audioFile.delete();
        }

        // 2. 텍스트 파일도 함께 삭제
        String textFilePath = recording.getFilePath().replaceAll("\\.m4a$", ".txt");
        File textFile = new File(textFilePath);
        boolean textDeleted = false;
        if (textFile.exists()) {
            textDeleted = textFile.delete();
        }

        if (audioDeleted) {
            Toast.makeText(getContext(), "녹음 파일이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
            // 3. 목록에서 아이템 제거 및 UI 갱신
            int position = recordingList.indexOf(recording);
            if (position != -1) {
                recordingList.remove(position);
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(position, recordingList.size());
                // 목록이 비었는지 다시 확인
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
