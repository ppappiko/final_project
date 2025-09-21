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

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recentList;
    private BottomNavigationView bottomNav;
    private Button btnNewRecord;
    private ImageView btnSearch, btnBell;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recentList   = findViewById(R.id.recentList);
        bottomNav    = findViewById(R.id.bottomNav);
        btnNewRecord = findViewById(R.id.btnNewRecord);
        btnSearch    = findViewById(R.id.btnSearch);
        btnBell      = findViewById(R.id.btnBell);

        // ✅ RecentExamAdapter.ExamItem 로 참조
        List<RecentExamAdapter.ExamItem> data = Arrays.asList(
                new RecentExamAdapter.ExamItem("프로젝트실무2", "2025.07.10", "10문제"),
                new RecentExamAdapter.ExamItem("프로젝트실무1", "2025.07.10", "30문제")
        );

        recentList.setLayoutManager(new LinearLayoutManager(this));
        RecentExamAdapter adapter = new RecentExamAdapter(
                data,
                item -> Toast.makeText(this, "선택: " + item.getTitle(), Toast.LENGTH_SHORT).show()
        );
        recentList.setAdapter(adapter);

        btnSearch.setOnClickListener(v ->
                Toast.makeText(this, "검색 클릭", Toast.LENGTH_SHORT).show());
        btnBell.setOnClickListener(v ->
                Toast.makeText(this, "알림 클릭", Toast.LENGTH_SHORT).show());
        btnNewRecord.setOnClickListener(v ->
                Toast.makeText(this, "+ 새로운 녹음 클릭", Toast.LENGTH_SHORT).show());

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
    }
}