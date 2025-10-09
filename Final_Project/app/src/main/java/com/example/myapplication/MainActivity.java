package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.myapplication.Home.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private Button btnNewRecord;
    private ImageView btnSearch, btnBell;

    // 프래그먼트 인스턴스 생성
    private final HomeFragment homeFragment = new HomeFragment();
    private final CalendarFragment calendarFragment = new CalendarFragment();
    private final ProfileFragment fragmentFragment = new ProfileFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 뷰 바인딩
        bottomNav    = findViewById(R.id.bottomNav);
        btnNewRecord = findViewById(R.id.btnNewRecord);
        btnSearch    = findViewById(R.id.btnSearch);
        btnBell      = findViewById(R.id.btnBell);

        // 상단바 버튼 리스너
        btnSearch.setOnClickListener(v -> Toast.makeText(this, "검색 클릭", Toast.LENGTH_SHORT).show());
        btnBell.setOnClickListener(v -> Toast.makeText(this, "알림 클릭", Toast.LENGTH_SHORT).show());

        // "+ 새로운 녹음" 버튼 리스너
        btnNewRecord.setOnClickListener(v -> {
            Intent intent = new Intent(this, RecordingActivity.class);
            startActivity(intent);
        });

        // 앱 시작 시 HomeFragment를 기본으로 보여줌
        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, homeFragment).commit();

        // 하단 네비게이션 리스너
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // '홈' 탭: 일반 모드로 HomeFragment 실행
                replaceFragment(HomeFragment.newInstance("home"));
                return true;
            }
            if (id == R.id.nav_calendar) {
                replaceFragment(calendarFragment);
                return true;
            }
            if (id == R.id.nav_profile) {
                replaceFragment(fragmentFragment);
                return true;
            }
            if (id == R.id.nav_generate) {
                // '문제 생성' 탭: 선택 모드로 HomeFragment 실행
                replaceFragment(HomeFragment.newInstance("generate"));
                return true;
            }

            // ... 다른 메뉴 아이템들 ...
            return false;
        });
    }

    // 프래그먼트를 교체하는 공개 메소드
    public void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_frame, fragment)
                .commit();
    }
}