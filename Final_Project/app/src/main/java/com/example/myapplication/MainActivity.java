package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.myapplication.Home.Detail.DetailsFragment;
import com.example.myapplication.Home.HomeFragment;
import com.example.myapplication.question.GenerateFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private Button btnNewRecord;
    private ImageView btnSearch, btnBell, btnRefresh;

    private ActivityResultLauncher<String> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav    = findViewById(R.id.bottomNav);
        btnNewRecord = findViewById(R.id.btnNewRecord);
        btnSearch    = findViewById(R.id.btnSearch);
        btnBell      = findViewById(R.id.btnBell);
        btnRefresh   = findViewById(R.id.btnRefresh);

        filePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        copyFileToAppStorageFromUri(uri);
                    }
                });

        btnSearch.setOnClickListener(v -> showSearchDialog());
        btnBell.setOnClickListener(v -> Toast.makeText(this, "알림 클릭", Toast.LENGTH_SHORT).show());

        btnRefresh.setOnClickListener(v -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.main_frame);
            if (currentFragment instanceof DetailsFragment) {
                ((DetailsFragment) currentFragment).requestRefreshToChild();
            }
        });

        btnNewRecord.setOnClickListener(v -> showUploadOrRecordDialog());

        replaceFragment(new HomeFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                selectedFragment = HomeFragment.newInstance("home");
            } else if (id == R.id.nav_community) {
                selectedFragment = new CommunityFragment();
            } else if (id == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            } else if (id == R.id.nav_generate) {
                selectedFragment = new GenerateFragment();
            }

            if (selectedFragment != null) {
                replaceFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    private void showUploadOrRecordDialog() {
        final CharSequence[] options = {"파일에서 가져오기", "새로 녹음하기"};
        new AlertDialog.Builder(this)
                .setTitle("새로운 녹음 추가")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        filePickerLauncher.launch("audio/*");
                    } else {
                        startActivity(new Intent(this, RecordingActivity.class));
                    }
                })
                .show();
    }

    private void copyFileToAppStorageFromUri(Uri sourceUri) {
        String sourceFileName = getFileNameFromUri(sourceUri);
        if (sourceFileName == null) {
            Toast.makeText(this, "파일 이름을 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!sourceFileName.toLowerCase().endsWith(".m4a")) {
            Toast.makeText(this, "m4a 형식의 오디오 파일만 가져올 수 있습니다.", Toast.LENGTH_LONG).show();
            return;
        }

        File destDir = getExternalFilesDir(null);
        File destFile = new File(destDir, sourceFileName);

        if (destFile.exists()) {
            Toast.makeText(this, "이미 동일한 이름의 파일이 존재합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        try (InputStream in = getContentResolver().openInputStream(sourceUri); OutputStream out = new FileOutputStream(destFile)) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            Toast.makeText(this, "파일을 성공적으로 가져왔습니다.", Toast.LENGTH_SHORT).show();

            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.main_frame);
            if (currentFragment instanceof HomeFragment) {
                ((HomeFragment) currentFragment).filterRecordingsByContent(null);
            } else if (currentFragment instanceof GenerateFragment) {
                ((GenerateFragment) currentFragment).loadRecordingsFromStorage();
            }

        } catch (IOException e) {
            Toast.makeText(this, "파일 가져오기 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        } else {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("제목으로 녹음 검색");
        final EditText input = new EditText(this);
        input.setHint("찾고 싶으신 녹음파일의 제목을 입력해주세요");
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
        // HomeFragment일 때만 '새로운 녹음' 버튼을 표시
        if (fragment instanceof HomeFragment) {
            btnNewRecord.setVisibility(View.VISIBLE);
        } else {
            btnNewRecord.setVisibility(View.GONE);
        }
        
        // DetailsFragment가 아닐 때는 항상 새로고침 버튼 숨김
        if (!(fragment instanceof DetailsFragment)) {
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