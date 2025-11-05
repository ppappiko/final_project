package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.myapplication.Home.Detail.DetailsFragment;
import com.example.myapplication.Home.HomeFragment;
import com.example.myapplication.question.GenerateFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    public interface OnRefreshButtonClickListener {
        void onRefreshClick();
    }

    private BottomNavigationView bottomNav;
    private Button btnNewRecord;
    private ImageView btnSearch, btnBell, btnRefresh;

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
        btnRefresh   = findViewById(R.id.btnRefresh);

        btnSearch.setOnClickListener(v -> showSearchDialog());
        btnBell.setOnClickListener(v -> Toast.makeText(this, "알림 클릭", Toast.LENGTH_SHORT).show());

        btnRefresh.setOnClickListener(v -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.main_frame);
            if (currentFragment instanceof DetailsFragment) {
                ((DetailsFragment) currentFragment).requestRefreshToChild();
            }
        });

        btnNewRecord.setOnClickListener(v -> {
            Intent intent = new Intent(this, RecordingActivity.class);
            startActivity(intent);
        });

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

    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("녹음파일 검색");

        final EditText input = new EditText(this);
        input.setHint("검색어를 입력하세요...");
        builder.setView(input);

        builder.setPositiveButton("검색", (dialog, which) -> {
            String query = input.getText().toString().trim();
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.main_frame);
            if (currentFragment instanceof HomeFragment) {
                ((HomeFragment) currentFragment).filterRecordingsByContent(query);
            }
        });
        builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    public void replaceFragment(Fragment fragment) {
        if (fragment instanceof DetailsFragment) {
            btnNewRecord.setVisibility(View.GONE);
        } else {
            btnNewRecord.setVisibility(View.VISIBLE);
            btnRefresh.setVisibility(View.GONE);
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_frame, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void showRefreshButton(boolean show) {
        btnRefresh.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}