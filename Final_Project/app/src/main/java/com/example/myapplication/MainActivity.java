package com.example.myapplication;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recentList;
    private BottomNavigationView bottomNav;
    private Button btnNewRecord;
    private ImageView btnSearch, btnBell;

    private RecentExamAdapter adapter;
    private final List<RecentExamAdapter.ExamItem> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // 이미 갖고 계신 메인 레이아웃

        // 뷰 바인딩
        recentList   = findViewById(R.id.recentList);
        bottomNav    = findViewById(R.id.bottomNav);
        btnNewRecord = findViewById(R.id.btnNewRecord);
        btnSearch    = findViewById(R.id.btnSearch);
        btnBell      = findViewById(R.id.btnBell);

        // RecyclerView 세팅
        recentList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecentExamAdapter(items, new RecentExamAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(RecentExamAdapter.ExamItem item, int position) {
                Toast.makeText(MainActivity.this, "클릭: " + item.getTitle(), Toast.LENGTH_SHORT).show();
                // TODO: 재생 화면으로 이동하거나 MediaPlayer로 곧바로 재생하고 싶다면 여기서 구현
            }
        });
        recentList.setAdapter(adapter);

        // 상단바 버튼
        btnSearch.setOnClickListener(v ->
                Toast.makeText(this, "검색 클릭", Toast.LENGTH_SHORT).show());
        btnBell.setOnClickListener(v ->
                Toast.makeText(this, "알림 클릭", Toast.LENGTH_SHORT).show());

        // "+ 새로운 녹음" 버튼
        btnNewRecord.setOnClickListener(v -> {
            // TODO: 녹음 화면으로 이동 or 녹음 시작 (녹음 기능은 이미 구현되어 있다고 가정)
            Toast.makeText(this, "+ 새로운 녹음 클릭", Toast.LENGTH_SHORT).show();
        });

        // 하단 네비
        bottomNav.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home)      { Toast.makeText(MainActivity.this, "홈", Toast.LENGTH_SHORT).show(); return true; }
                if (id == R.id.nav_calendar)  { Toast.makeText(MainActivity.this, "캘린더", Toast.LENGTH_SHORT).show(); return true; }
                if (id == R.id.nav_generate)  { Toast.makeText(MainActivity.this, "문제 생성", Toast.LENGTH_SHORT).show(); return true; }
                if (id == R.id.nav_profile)   { Toast.makeText(MainActivity.this, "내 정보", Toast.LENGTH_SHORT).show(); return true; }
                return false;
            }
        });

        // 최초 로드
        refreshList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 돌아올 때마다 최신 파일 반영
        refreshList();
    }

    /** 내부저장소(또는 내부의 records 폴더)에서 최근 녹음 파일을 읽어 items 갱신 */
    private void refreshList() {
        items.clear();

        List<RecentExamAdapter.ExamItem> loaded = loadRecentRecords();
        if (loaded.isEmpty()) {
            // 녹음 파일이 아직 없다면: 더미 데이터로 화면 확인
            for (int i = 0; i < 10; i++) {
                items.add(new RecentExamAdapter.ExamItem("item" + i, "2025.09.24 22:10", (i + 1) + "문제"));
            }
        } else {
            items.addAll(loaded);
        }

        adapter.notifyDataSetChanged();
    }

    /** 실제 녹음 파일 읽기 (최대 10개, 최신순) */
    private List<RecentExamAdapter.ExamItem> loadRecentRecords() {
        File dir = getRecordDirectory();
        Log.d("RecordCheck", "검색할 폴더 경로: " + dir.getAbsolutePath());

        File[] files = dir.listFiles((d, name) -> name.endsWith(".mp3"));
        List<RecentExamAdapter.ExamItem> items = new ArrayList<>();

        if (files != null) {
            Log.d("RecordCheck", "발견된 파일 개수: " + files.length);

            Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

            for (File f : files) {
                Log.d("RecordCheck", "파일 발견: " + f.getName() + " (경로: " + f.getAbsolutePath() + ")");
                String date = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
                        .format(new Date(f.lastModified()));
                items.add(new RecentExamAdapter.ExamItem(f.getName(), date, "녹음 파일"));
            }
        } else {
            Log.d("RecordCheck", "파일이 없음 (listFiles() == null)");
        }

        return items;
    }

    /** 녹음 파일 저장 위치 추정
     *  - 1순위: app 내부 files/records 디렉토리 (녹음기가 여기에 저장하도록 구현되었을 수 있음)
     *  - 2순위: app 내부 files (기본)
     *  필요 시, 녹음기 구현의 실제 경로로 바꾸세요.
     */
    private File getRecordDirectory() {
        // /data/data/<pkg>/files/records
        File records = new File(getFilesDir(), "records");
        if (records.exists() && records.isDirectory()) {
            return records;
        }
        // /data/data/<pkg>/files
        return getFilesDir();
    }
}