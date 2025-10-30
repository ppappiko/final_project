package com.example.myapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// 녹음 기능을 담당하는 액티비티 클래스
public class RecordingActivity extends AppCompatActivity {

    private static final String TAG = "RecordingActivity";

    // UI 요소 변수들
    private TextView tvRecordTime;
    private ImageButton btnPauseResume;
    private Button btnStopRecord;

    // 타이머 및 녹음 상태 관리 변수들
    private long startTime = 0L;
    private long timeWhenPaused = 0L;
    private Handler handler = new Handler();
    private boolean isRecording = false;

    // 실제 녹음 기능 관련 객체
    private MediaRecorder mediaRecorder = null;
    private String audioFilePath = null;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private Runnable updateTimer = new Runnable() {
        public void run() {
            if (isRecording) {
                long timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
                long seconds = timeInMilliseconds / 1000;
                long minutes = seconds / 60;
                long hours = minutes / 60;

                String timeText = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
                tvRecordTime.setText(timeText);

                handler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);

        tvRecordTime = findViewById(R.id.tv_record_time);
        btnPauseResume = findViewById(R.id.btn_pause_resume);
        btnStopRecord = findViewById(R.id.btn_stop_record);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        } else {
            startRecording();
        }

        btnStopRecord.setOnClickListener(v -> stopRecording());

        btnPauseResume.setOnClickListener(v -> {
            if (isRecording) {
                pauseRecording();
            } else {
                resumeRecording();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording();
            } else {
                Toast.makeText(this, "녹음 권한이 없어 기능을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void startRecording() {
        if (!isRecording) {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            audioFilePath = getExternalCacheDir().getAbsolutePath() + "/TEMP_" + timeStamp + ".m4a";

            mediaRecorder.setOutputFile(audioFilePath);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
                Toast.makeText(this, "녹음 시작", Toast.LENGTH_SHORT).show();

                startTime = SystemClock.uptimeMillis();
                handler.post(updateTimer);
                isRecording = true;
                btnPauseResume.setImageResource(R.drawable.ic_pause);
            } catch (IOException e) {
                Log.e(TAG, "startRecording: prepare() failed", e);
                Toast.makeText(this, "녹음 시작 실패", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void pauseRecording() {
        if (isRecording && mediaRecorder != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                mediaRecorder.pause();
                handler.removeCallbacks(updateTimer);
                timeWhenPaused = SystemClock.uptimeMillis();
                isRecording = false;
                Toast.makeText(this, "녹음 일시정지", Toast.LENGTH_SHORT).show();
                btnPauseResume.setImageResource(R.drawable.ic_play);
            } else {
                Toast.makeText(this, "이 기기에서는 일시정지 기능을 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void resumeRecording() {
        if (!isRecording && mediaRecorder != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                mediaRecorder.resume();
                startTime += (SystemClock.uptimeMillis() - timeWhenPaused);
                handler.post(updateTimer);
                isRecording = true;
                Toast.makeText(this, "녹음 재개", Toast.LENGTH_SHORT).show();
                btnPauseResume.setImageResource(R.drawable.ic_pause);
            } else {
                Toast.makeText(this, "이 기기에서는 재개 기능을 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void stopRecording() {
        if (mediaRecorder != null) {
            handler.removeCallbacks(updateTimer);
            try {
                mediaRecorder.stop();
            } catch (RuntimeException e) {
                Log.e(TAG, "stopRecording: stop() failed", e);
            }
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;
            showFileNameInputDialog(audioFilePath);
        }
    }

    private void showFileNameInputDialog(String tempFilePath) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("녹음 파일 이름 입력");

        final EditText input = new EditText(this);
        String defaultName = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(new Date());
        input.setText(defaultName);
        builder.setView(input);

        builder.setPositiveButton("저장", (dialog, which) -> {
            String fileName = input.getText().toString().trim();
            if (fileName.isEmpty()) {
                Toast.makeText(RecordingActivity.this, "파일 이름이 비어있어 저장을 취소합니다.", Toast.LENGTH_SHORT).show();
                new File(tempFilePath).delete();
                finish();
                return;
            }

            String newFilePath = getExternalFilesDir(null).getAbsolutePath() + "/" + fileName + ".m4a";
            File tempFile = new File(tempFilePath);
            File newFile = new File(newFilePath);

            if (newFile.exists()) {
                newFilePath = getExternalFilesDir(null).getAbsolutePath() + "/" + fileName + "_"
                        + new SimpleDateFormat("HHmmss", Locale.getDefault()).format(new Date()) + ".m4a";
                newFile = new File(newFilePath);
                Toast.makeText(RecordingActivity.this, "파일 이름이 중복되어 " + newFile.getName() + "으로 저장됩니다.", Toast.LENGTH_LONG).show();
            }
            
            // renameTo 대신 copyFile 사용
            if (copyFile(tempFile, newFile)) {
                Toast.makeText(RecordingActivity.this, "녹음 파일 저장 완료: " + newFile.getName(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(RecordingActivity.this, "파일 저장 실패", Toast.LENGTH_LONG).show();
            }
            tempFile.delete(); // 성공하든 실패하든 임시 파일은 삭제
            finish();
        });

        builder.setNegativeButton("취소", (dialog, which) -> {
            new File(tempFilePath).delete();
            Toast.makeText(RecordingActivity.this, "녹음 저장을 취소했습니다.", Toast.LENGTH_SHORT).show();
            finish();
        });
        builder.show();
    }
    
    private boolean copyFile(File source, File dest) {
        try (InputStream is = new FileInputStream(source); OutputStream os = new FileOutputStream(dest)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            return true;
        } catch (IOException e) {
            Log.e(TAG, "copyFile failed", e);
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }
}
