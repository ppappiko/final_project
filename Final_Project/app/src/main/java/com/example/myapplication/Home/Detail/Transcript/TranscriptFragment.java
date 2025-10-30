package com.example.myapplication.Home.Detail.Transcript;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TranscriptFragment extends Fragment {

    private static final String TAG = "TranscriptFragment";
    private ScrollView scrollView;
    private TextView tvTranscript;
    private Button btnDictation;
    private ProgressBar progressBar;
    private String filePath;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(180, TimeUnit.SECONDS)
            .writeTimeout(180, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .build();

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transcript, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        scrollView = view.findViewById(R.id.scroll_view);
        tvTranscript = view.findViewById(R.id.tv_transcript);
        btnDictation = view.findViewById(R.id.btn_dictation);
        progressBar = view.findViewById(R.id.progress_bar);

        if (getArguments() != null) {
            filePath = getArguments().getString("filePath");
        }

        // 프래그먼트가 생성될 때, 저장된 텍스트 파일이 있는지 확인
        if (!loadExistingTranscript()) {
            // 파일이 없으면 받아쓰기 버튼 표시
            btnDictation.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.GONE);
        }

        btnDictation.setOnClickListener(v -> {
            if (filePath != null && !filePath.isEmpty()) {
                transcribeAudio(filePath);
            } else {
                Toast.makeText(getContext(), "녹음 파일을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean loadExistingTranscript() {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }

        String textFilePath = filePath.replaceAll("\\.m4a$", ".txt");
        File textFile = new File(textFilePath);

        if (textFile.exists()) {
            try (FileInputStream fis = new FileInputStream(textFile)) {
                byte[] data = new byte[(int) textFile.length()];
                fis.read(data);
                String transcript = new String(data, StandardCharsets.UTF_8);
                displayTranscript(transcript);
                return true; // 불러오기 성공
            } catch (IOException e) {
                Log.e(TAG, "Failed to read existing transcript file", e);
                return false; // 불러오기 실패
            }
        }
        return false; // 파일이 존재하지 않음
    }

    private void transcribeAudio(String audioFilePath) {
        setLoadingState(true);

        File audioFile = new File(audioFilePath);
        RequestBody fileBody = RequestBody.create(audioFile, MediaType.parse("audio/mp4"));

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("audio_file", audioFile.getName(), fileBody)
                .build();

        Request request = new Request.Builder()
                .url("http://34.50.41.99:8000/transcribe/")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mainHandler.post(() -> {
                    setLoadingState(false);
                    displayTranscript("변환 실패: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseBody = response.body().string();
                Log.d(TAG, "Server Response: " + responseBody);

                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String transcript = jsonObject.getString("transcribed_text");
                        mainHandler.post(() -> {
                            setLoadingState(false);
                            displayTranscript(transcript);
                            saveTranscriptToFile(transcript);
                        });
                    } catch (JSONException e) {
                        mainHandler.post(() -> {
                            setLoadingState(false);
                            displayTranscript("--- 응답 파싱 실패 ---\n서버 원본 응답:\n\n" + responseBody);
                        });
                    }
                } else {
                    mainHandler.post(() -> {
                        setLoadingState(false);
                        displayTranscript("--- 서버 오류 " + response.code() + " ---\n" + responseBody);
                    });
                }
            }
        });
    }

    private void displayTranscript(String text) {
        if (text != null && !text.isEmpty()) {
            tvTranscript.setText(text);
        } else {
            tvTranscript.setText("변환된 텍스트가 없습니다.");
        }
        scrollView.setVisibility(View.VISIBLE);
        btnDictation.setVisibility(View.GONE);
    }

    private void saveTranscriptToFile(String text) {
        if (filePath == null || filePath.isEmpty()) {
            Toast.makeText(getContext(), "파일 경로가 없어 저장을 스킵합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String textFilePath = filePath.replaceAll("\\.m4a$", ".txt");
        File textFile = new File(textFilePath);

        try (FileOutputStream fos = new FileOutputStream(textFile)) {
            fos.write(text.getBytes(StandardCharsets.UTF_8));
            Toast.makeText(getContext(), "텍스트가 파일로 저장되었습니다.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "Failed to save transcript file", e);
            Toast.makeText(getContext(), "텍스트 파일 저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setLoadingState(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnDictation.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }
}
