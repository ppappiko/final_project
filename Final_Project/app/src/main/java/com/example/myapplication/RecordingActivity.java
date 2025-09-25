package com.example.myapplication;

import android.Manifest; // 앱의 권한 설정을 위한 클래스
import android.content.pm.PackageManager; // 권한 관리
import android.media.MediaRecorder; // 오디오 녹음 기능 제공
import android.os.Bundle; // 액티비티 상태 저장
import android.os.Handler; // UI 스레드에서 Runnable을 실행하기 위한 클래스
import android.os.SystemClock; // 부팅 후 경과 시간 측정을 위한 클래스
import android.widget.Button; // 버튼 UI
import android.widget.ImageButton; // 이미지 버튼 UI
import android.widget.TextView; // 텍스트 뷰 UI
import android.widget.Toast; // 짧은 알림 메시지 표시

import androidx.annotation.NonNull; // null 검사를 위한 어노테이션
import androidx.appcompat.app.AppCompatActivity; // AppCompatActivity를 상속받아 호환성 제공
import androidx.core.app.ActivityCompat; // 권한 요청 도우미 클래스
import androidx.core.content.ContextCompat; // 권한 확인 도우미 클래스

import java.io.IOException; // 입출력 예외 처리
import java.util.Locale; // 시간 형식을 현지화하기 위함

// 녹음 기능을 담당하는 액티비티 클래스
public class RecordingActivity extends AppCompatActivity {

    // UI 요소 변수들
    private TextView tvRecordTime; // 녹음 시간을 표시할 텍스트 뷰
    private ImageButton btnPauseResume; // 녹음 일시정지/재개를 위한 버튼
    private Button btnStopRecord; // 녹음 종료를 위한 버튼

    // 타이머 및 녹음 상태 관리 변수들
    private long startTime = 0L; // 녹음 시작 시점의 시간을 저장
    private long timeWhenPaused = 0L; // 녹음 일시정지 시점의 시간을 저장
    private Handler handler = new Handler(); // UI 스레드에서 타이머를 업데이트할 핸들러
    private boolean isRecording = false; // 현재 녹음 중인지 상태를 나타내는 플래그

    // 실제 녹음 기능 관련 객체
    private MediaRecorder mediaRecorder = null; // 안드로이드 녹음 기능을 사용하기 위한 객체
    private String audioFilePath = null; // 녹음 파일이 저장될 경로
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200; // 오디오 녹음 권한 요청 코드

    // 1초마다 실행되어 타이머를 업데이트하는 Runnable 객체
    private Runnable updateTimer = new Runnable() {
        public void run() {
            if (isRecording) {
                // 일시 정지된 시간을 포함하여 전체 경과 시간을 계산
                long timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
                long seconds = timeInMilliseconds / 1000;
                long minutes = seconds / 60;
                long hours = minutes / 60;

                // 시간을 "HH:MM:SS" 형식으로 포맷팅하여 TextView에 설정
                String timeText = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
                tvRecordTime.setText(timeText);

                // 1초(1000ms) 뒤에 이 Runnable을 다시 실행하도록 예약
                handler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);

        // 레이아웃의 UI 요소들을 찾아 변수에 할당
        tvRecordTime = findViewById(R.id.tv_record_time);
        btnPauseResume = findViewById(R.id.btn_pause_resume);
        btnStopRecord = findViewById(R.id.btn_stop_record);

        // 앱 시작 시 오디오 녹음 권한이 있는지 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없으면 사용자에게 권한을 요청
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        } else {
            // 권한이 있으면 바로 녹음 시작
            startRecording();
        }

        // '녹음 종료' 버튼 클릭 시 이벤트 처리
        btnStopRecord.setOnClickListener(v -> {
            stopRecording();
            Toast.makeText(this, "녹음이 종료되었습니다.", Toast.LENGTH_SHORT).show();
            finish(); // 현재 액티비티를 닫고 이전 화면으로 돌아감
        });

        // '일시정지/재개' 버튼 클릭 시 이벤트 처리
        btnPauseResume.setOnClickListener(v -> {
            if (isRecording) {
                // 녹음 중이면 일시정지
                pauseRecording();
            } else {
                // 일시정지 상태면 재개
                resumeRecording();
            }
        });
    }

    // 권한 요청 결과 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 사용자가 권한을 허용하면 녹음 시작
                startRecording();
            } else {
                // 사용자가 권한을 거부하면 기능 사용 불가 안내 및 액티비티 종료
                Toast.makeText(this, "녹음 권한이 없어 기능을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    // 녹음 시작
    private void startRecording() {
        if (!isRecording) {
            // MediaRecorder 객체 초기화
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC); // 오디오 소스를 마이크로 설정
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); // 출력 파일 형식 설정

            // 오디오 파일 저장 경로 설정 (앱의 캐시 디렉토리 사용)
            audioFilePath = getExternalCacheDir().getAbsolutePath() + "/audiorecordtest.3gp";
            mediaRecorder.setOutputFile(audioFilePath);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB); // 오디오 인코더 설정

            try {
                // 녹음 준비 및 시작
                mediaRecorder.prepare();
                mediaRecorder.start();
                Toast.makeText(this, "녹음 시작", Toast.LENGTH_SHORT).show();

                // 타이머 시작
                startTime = SystemClock.uptimeMillis();
                handler.post(updateTimer); // Runnable을 Handler에 등록하여 타이머 시작
                isRecording = true;
                btnPauseResume.setImageResource(R.drawable.ic_pause); // 버튼 이미지를 일시정지 아이콘으로 변경
            } catch (IOException e) {
                // 녹음 준비 또는 시작 중 오류 발생 시 처리
                e.printStackTrace();
                Toast.makeText(this, "녹음 시작 실패", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 녹음 일시정지
    private void pauseRecording() {
        if (isRecording && mediaRecorder != null) {
            // 안드로이드 N(API 24) 이상에서만 지원
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                mediaRecorder.pause();
                handler.removeCallbacks(updateTimer); // 타이머 중지
                timeWhenPaused = SystemClock.uptimeMillis(); // 일시 정지된 시점의 시간을 저장
                isRecording = false;
                Toast.makeText(this, "녹음 일시정지", Toast.LENGTH_SHORT).show();
                btnPauseResume.setImageResource(R.drawable.ic_play); // 버튼 이미지를 재생 아이콘으로 변경
            } else {
                Toast.makeText(this, "이 기기에서는 일시정지 기능을 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 녹음 재개
    private void resumeRecording() {
        if (!isRecording && mediaRecorder != null) {
            // 안드로이드 N(API 24) 이상에서만 지원
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                mediaRecorder.resume();
                // 재개 시 일시 정지된 시간만큼 startTime을 보정하여 이어서 계산되도록 함
                startTime += (SystemClock.uptimeMillis() - timeWhenPaused);
                handler.post(updateTimer); // 타이머 재시작
                isRecording = true;
                Toast.makeText(this, "녹음 재개", Toast.LENGTH_SHORT).show();
                btnPauseResume.setImageResource(R.drawable.ic_pause); // 버튼 이미지를 일시정지 아이콘으로 변경
            } else {
                Toast.makeText(this, "이 기기에서는 재개 기능을 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 녹음 중지 및 파일 저장
    private void stopRecording() {
        if (mediaRecorder != null) {
            handler.removeCallbacks(updateTimer); // 타이머 중지
            mediaRecorder.stop(); // 녹음 중지
            mediaRecorder.release(); // MediaRecorder 자원 해제
            mediaRecorder = null;
            isRecording = false;
            Toast.makeText(this, "녹음 파일 저장 완료: " + audioFilePath, Toast.LENGTH_LONG).show();
        }
    }

    // 액티비티가 소멸될 때 MediaRecorder 자원 해제
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }
}