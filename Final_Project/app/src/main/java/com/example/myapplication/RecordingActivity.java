package com.example.myapplication;

import android.Manifest; // 마이크 및 저장소 권한을 사용하기 위한 import
import android.app.AlertDialog; // 파일명 입력 다이얼로그를 위한 import
import android.content.DialogInterface; // 다이얼로그 인터페이스를 위한 import
import android.content.pm.PackageManager; // 권한 상태 확인을 위한 import
import android.media.MediaRecorder; // 실제 오디오 녹음 기능을 위한 클래스
import android.os.Bundle; // 액티비티 초기화 상태 저장을 위한 클래스
import android.os.Handler; // UI 업데이트를 위한 타이머(Runnable) 실행 클래스
import android.os.SystemClock; // 부팅 후 경과 시간 측정을 위한 클래스
import android.widget.Button; // 버튼 UI 요소
import android.widget.EditText; // 파일 이름 입력을 위한 텍스트 입력 창
import android.widget.ImageButton; // 이미지 버튼 UI 요소
import android.widget.TextView; // 텍스트 뷰 UI 요소
import android.widget.Toast; // 짧은 알림 메시지를 위한 클래스

import androidx.annotation.NonNull; // null 검사를 위한 어노테이션
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat; // 권한 요청 도우미 클래스
import androidx.core.content.ContextCompat; // 권한 상태 확인 도우미 클래스

import java.io.File; // 파일 관리를 위한 클래스
import java.io.IOException; // 입출력 예외 처리
import java.text.SimpleDateFormat; // 날짜 및 시간 포맷 설정을 위한 클래스
import java.util.Date; // 현재 날짜/시간 객체
import java.util.Locale; // 지역 설정 (포맷 등에 사용)

// 녹음 기능을 담당하는 액티비티 클래스
public class RecordingActivity extends AppCompatActivity {

    // UI 요소 변수들
    private TextView tvRecordTime; // 녹음 시간을 표시할 텍스트 뷰
    private ImageButton btnPauseResume; // 녹음 일시정지/재개를 위한 버튼
    private Button btnStopRecord; // 녹음 종료를 위한 버튼

    // 타이머 및 녹음 상태 관리 변수들
    private long startTime = 0L; // 녹음이 시작된 시점의 시스템 시간
    private long timeWhenPaused = 0L; // 녹음이 일시정지된 시점의 시스템 시간 (이어서 계산하기 위함)
    private Handler handler = new Handler(); // UI 스레드에서 타이머를 주기적으로 실행할 핸들러
    private boolean isRecording = false; // 현재 녹음이 활성화(진행) 중인지 여부

    // 실제 녹음 기능 관련 객체
    private MediaRecorder mediaRecorder = null; // 안드로이드 녹음 기능을 위한 객체
    private String audioFilePath = null; // 현재 녹음 중인 임시 파일 경로
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200; // 오디오 녹음 권한 요청 코드

    // 1초마다 실행되어 타이머를 업데이트하는 Runnable 객체
    private Runnable updateTimer = new Runnable() {
        public void run() {
            if (isRecording) {
                // 현재 경과 시간을 계산
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

        // 앱 시작 시 오디오 녹음 권한 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없으면 사용자에게 권한 요청
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        } else {
            // 권한이 있으면 바로 녹음 시작
            startRecording();
        }

        // '녹음 종료' 버튼 클릭 시 이벤트 처리
        btnStopRecord.setOnClickListener(v -> {
            stopRecording(); // 녹음 중지 로직 실행 (내부에서 파일명 입력 다이얼로그 호출)
        });

        // '일시정지/재개' 버튼 클릭 시 이벤트 처리
        btnPauseResume.setOnClickListener(v -> {
            if (isRecording) {
                pauseRecording(); // 녹음 중이면 일시정지
            } else {
                resumeRecording(); // 일시정지 상태면 재개
            }
        });
    }

    // 권한 요청 결과 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording(); // 사용자가 권한을 허용하면 녹음 시작
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
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC); // 오디오 소스를 마이크로 설정


            // 출력 형식을 MPEG_4로 변경 (m4a 파일 포맷)
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

            // 파일 저장 경로를 임시적으로 설정 (중복 방지를 위해 타임스탬프 사용)
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            audioFilePath = getExternalCacheDir().getAbsolutePath() + "/TEMP_" + timeStamp + ".m4a"; // 확장자는 .m4a



            mediaRecorder.setOutputFile(audioFilePath);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC); // 오디오 인코더를 AAC로 설정 (고품질)

            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
                Toast.makeText(this, "녹음 시작", Toast.LENGTH_SHORT).show();

                // 타이머 시작
                startTime = SystemClock.uptimeMillis();
                handler.post(updateTimer);
                isRecording = true;
                btnPauseResume.setImageResource(R.drawable.ic_pause);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "녹음 시작 실패", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 녹음 일시정지
    private void pauseRecording() {
        if (isRecording && mediaRecorder != null) {
            // 안드로이드 N(API 24) 이상에서만 지원하는 기능
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                mediaRecorder.pause();
                handler.removeCallbacks(updateTimer); // 타이머 중지
                timeWhenPaused = SystemClock.uptimeMillis(); // 일시 정지 시점 시간 저장
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
            // 안드로이드 N(API 24) 이상에서만 지원하는 기능
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                mediaRecorder.resume();
                // 재개 시 일시 정지된 시간만큼 startTime을 보정하여 이어서 계산
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

    // 녹음 중지
    private void stopRecording() {
        if (mediaRecorder != null) {
            handler.removeCallbacks(updateTimer); // 타이머 중지

            // MediaRecorder 중지
            try {
                mediaRecorder.stop();
            } catch (RuntimeException e) {
                // 이미 중지되었거나, 녹음 시간이 매우 짧을 때 발생하는 예외 처리
                e.printStackTrace();
            }
            mediaRecorder.release(); // MediaRecorder 자원 해제
            mediaRecorder = null;
            isRecording = false;

            // 파일 이름 입력 다이얼로그 호출
            showFileNameInputDialog(audioFilePath);
        }
    }

    // 녹음 파일 이름 입력 다이얼로그를 표시하고 파일을 최종 저장하는 함수
    private void showFileNameInputDialog(String tempFilePath) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("녹음 파일 이름 입력");

        final EditText input = new EditText(this);
        // 기본 파일명을 현재 날짜와 시간으로 설정
        String defaultName = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(new Date());
        input.setText(defaultName);
        builder.setView(input);

        // '저장' 버튼 클릭 시
        builder.setPositiveButton("저장", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String fileName = input.getText().toString().trim();

                // 파일 이름이 없으면 임시 파일 삭제 후 종료
                if (fileName.isEmpty()) {
                    Toast.makeText(RecordingActivity.this, "파일 이름이 비어있어 저장을 취소합니다.", Toast.LENGTH_SHORT).show();
                    new File(tempFilePath).delete();
                    finish();
                    return;
                }

                // 최종 파일 경로 설정 (앱의 외부 전용 저장소에 .m4a 확장자로 저장)
                String newFilePath = getExternalFilesDir(null).getAbsolutePath() + "/" + fileName + ".m4a";

                File tempFile = new File(tempFilePath);
                File newFile = new File(newFilePath);

                // 파일 이름 중복 검사
                if (newFile.exists()) {
                    // 이미 존재하는 파일명이면 현재 시간을 붙여서 파일명 자동 변경 (덮어쓰기 방지)
                    newFilePath = getExternalFilesDir(null).getAbsolutePath() + "/" + fileName + "_"
                            + new SimpleDateFormat("HHmmss", Locale.getDefault()).format(new Date()) + ".m4a";
                    newFile = new File(newFilePath);
                    Toast.makeText(RecordingActivity.this, "파일 이름이 중복되어 " + newFile.getName() + "으로 저장됩니다.", Toast.LENGTH_LONG).show();
                }

                // 임시 파일을 최종 파일명으로 이름 변경
                if (tempFile.renameTo(newFile)) {
                    Toast.makeText(RecordingActivity.this, "녹음 파일 저장 완료: " + newFile.getName(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(RecordingActivity.this, "파일 저장 실패", Toast.LENGTH_LONG).show();
                    tempFile.delete(); // 실패 시 임시 파일 삭제
                }
                finish(); // 액티비티 종료
            }
        });

        // '취소' 버튼 클릭 시
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 임시 파일 삭제 후 액티비티 종료
                new File(tempFilePath).delete();
                Toast.makeText(RecordingActivity.this, "녹음 저장을 취소했습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        builder.show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 액티비티가 소멸될 때 MediaRecorder 자원 해제
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }
}