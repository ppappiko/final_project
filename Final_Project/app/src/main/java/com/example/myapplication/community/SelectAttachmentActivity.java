package com.example.myapplication.community;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.Recording;
import com.example.myapplication.Home.HomeRecyclerAdapter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SelectAttachmentActivity extends AppCompatActivity {

    private static final String TAG = "SelectAttachment";
    private RecyclerView recyclerView;
    private HomeRecyclerAdapter adapter;
    private List<Recording> fileList = new ArrayList<>();
    private TextView tvEmpty;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_attachment);

        recyclerView = findViewById(R.id.select_attachment_recycler_view);
        tvEmpty = findViewById(R.id.tv_select_attachment_empty);

        setupRecyclerView();
        loadAttachableFilesFromStorage();
    }

    private void setupRecyclerView() {
        adapter = new HomeRecyclerAdapter(fileList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(item -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selected_file_path", item.getFilePath());
            resultIntent.putExtra("selected_file_name", new File(item.getFilePath()).getName());
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    private void loadAttachableFilesFromStorage() {
        fileList.clear();
        File recordingsDir = getExternalFilesDir(null);
        if (recordingsDir != null && recordingsDir.exists()) {
            File[] files = recordingsDir.listFiles();
            if (files != null) {
                Log.d(TAG, "찾은 파일 개수: " + files.length);
                Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
                for (File file : files) {
                    String fileName = file.getName();
                    if (fileName.endsWith(".m4a") || fileName.endsWith(".txt")) {
                        String title = fileName.substring(0, fileName.lastIndexOf('.'));
                        String date = new SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(new Date(file.lastModified()));
                        fileList.add(new Recording(title, date, 0, file.getAbsolutePath()));
                        Log.d(TAG, "추가된 파일: " + fileName);
                    }
                }
            }
        }
        adapter.notifyDataSetChanged();
        updateEmptyView();
    }

    private void updateEmptyView() {
        if (fileList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            Log.d(TAG, "표시할 파일이 없습니다.");
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }
}