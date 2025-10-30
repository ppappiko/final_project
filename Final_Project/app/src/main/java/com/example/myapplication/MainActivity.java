package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.myapplication.Home.Detail.DetailsFragment;
import com.example.myapplication.Home.HomeFragment;
import com.example.myapplication.question.GenerateFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private Button btnNewRecord;
    private ImageView btnSearch, btnBell;

    private final HomeFragment homeFragment = new HomeFragment();
    private final CalendarFragment calendarFragment = new CalendarFragment();
    private final ProfileFragment profileFragment = new ProfileFragment();
    private final GenerateFragment generateFragment = new GenerateFragment();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav    = findViewById(R.id.bottomNav);
        btnNewRecord = findViewById(R.id.btnNewRecord);
        btnSearch    = findViewById(R.id.btnSearch);
        btnBell      = findViewById(R.id.btnBell);

        btnSearch.setOnClickListener(v -> Toast.makeText(this, "검색 클릭", Toast.LENGTH_SHORT).show());
        btnBell.setOnClickListener(v -> Toast.makeText(this, "알림 클릭", Toast.LENGTH_SHORT).show());

        btnNewRecord.setOnClickListener(v -> {
            Intent intent = new Intent(this, RecordingActivity.class);
            startActivity(intent);
        });

        // 앱 시작 시 HomeFragment를 기본으로 보여줌
        replaceFragment(homeFragment);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                replaceFragment(HomeFragment.newInstance("home"));
                return true;
            }
            if (id == R.id.nav_calendar) {
                replaceFragment(calendarFragment);
                return true;
            }
            if (id == R.id.nav_profile) {
                replaceFragment(profileFragment);
                return true;
            }
            if (id == R.id.nav_generate) {
                replaceFragment(generateFragment);
                return true;
            }
            return false;
        });
    }

    public void replaceFragment(Fragment fragment) {
        // DetailsFragment가 보일 때는 '새로운 녹음' 버튼을 숨김
        if (fragment instanceof DetailsFragment) {
            btnNewRecord.setVisibility(View.GONE);
        } else {
            // 다른 프래그먼트에서는 버튼을 다시 보이게 함
            btnNewRecord.setVisibility(View.VISIBLE);
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_frame, fragment)
                // 사용자가 뒤로가기 버튼을 눌렀을 때 이전 프래그먼트로 돌아갈 수 있도록 스택에 추가
                .addToBackStack(null)
                .commit();
    }
}