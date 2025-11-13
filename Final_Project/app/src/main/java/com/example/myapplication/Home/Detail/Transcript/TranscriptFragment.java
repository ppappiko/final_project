package com.example.myapplication.Home.Detail.Transcript;

import android.app.AlertDialog;
import android.os.Bundle;
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
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.Home.Detail.DetailsFragment;
import com.example.myapplication.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TranscriptFragment extends Fragment {

    private static final String TAG = "TranscriptFragment";
    private ScrollView scrollView;
    private TextView tvTranscript;
    private Button btnDictation;
    private ProgressBar progressBar;
    private String filePath;
    private boolean isShowingResult = false;

    private TranscriptViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transcript, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 뷰 초기화
        scrollView = view.findViewById(R.id.scroll_view);
        tvTranscript = view.findViewById(R.id.tv_transcript);
        btnDictation = view.findViewById(R.id.btn_dictation);
        progressBar = view.findViewById(R.id.progress_bar);

        // ViewModel 초기화
        viewModel = new ViewModelProvider(this).get(TranscriptViewModel.class);

        if (getArguments() != null) {
            filePath = getArguments().getString("filePath");
        }

        // 받아쓰기 버튼 리스너
        btnDictation.setOnClickListener(v -> {
            if (filePath != null && !filePath.isEmpty()) {
                viewModel.transcribeAudio(filePath);
            } else {
                Toast.makeText(getContext(), "녹음 파일을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        // ViewModel의 LiveData 관찰하여 UI 업데이트
        observeViewModel();

        // 기존 변환 결과 파일이 있으면 불러오기
        if (!loadExistingTranscript()) {
            btnDictation.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.GONE);
        }
    }

    private void observeViewModel() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), uiState -> {
            if (uiState instanceof TranscriptViewModel.Loading) {
                setLoadingState(true);
            } else if (uiState instanceof TranscriptViewModel.Success) {
                String transcript = ((TranscriptViewModel.Success) uiState).transcript;
                setLoadingState(false);
                displayTranscript(transcript);
                saveTranscriptToFile(transcript);
            } else if (uiState instanceof TranscriptViewModel.Error) {
                String errorMessage = ((TranscriptViewModel.Error) uiState).message;
                setLoadingState(false);
                displayTranscript("변환 실패: " + errorMessage);
            }
        });
    }

    public void handleRefreshRequest() {
        new AlertDialog.Builder(getContext())
                .setTitle("다시 받아쓰기")
                .setMessage("기존 내용을 지우고 다시 받아쓰기를 실행할까요?")
                .setPositiveButton("실행", (dialog, which) -> {
                    if (filePath != null) viewModel.transcribeAudio(filePath);
                })
                .setNegativeButton("취소", null)
                .show();
    }

    public boolean isShowingResult() {
        return isShowingResult;
    }

    private boolean loadExistingTranscript() {
        if (filePath == null) return false;
        String textFilePath = filePath.replaceAll("\\.m4a$", ".txt");
        File textFile = new File(textFilePath);

        if (textFile.exists()) {
            try (FileInputStream fis = new FileInputStream(textFile)) {
                byte[] data = new byte[(int) textFile.length()];
                fis.read(data);
                String transcript = new String(data, StandardCharsets.UTF_8);
                displayTranscript(transcript);
                return true;
            } catch (IOException e) {
                Log.e(TAG, "Failed to read transcript file", e);
                return false;
            }
        }
        return false;
    }

    private void displayTranscript(String text) {
        isShowingResult = true;
        tvTranscript.setText(text != null && !text.isEmpty() ? text : "변환된 텍스트가 없습니다.");
        scrollView.setVisibility(View.VISIBLE);
        btnDictation.setVisibility(View.GONE);
        updateRefreshButtonVisibilityInParent();
    }

    private void saveTranscriptToFile(String text) {
        if (filePath == null) return;
        String textFilePath = filePath.replaceAll("\\.m4a$", ".txt");
        try (FileOutputStream fos = new FileOutputStream(textFilePath)) {
            fos.write(text.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            Log.e(TAG, "Failed to save transcript file", e);
        }
    }

    private void setLoadingState(boolean isLoading) {
        isShowingResult = !isLoading;
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnDictation.setVisibility(isLoading ? View.GONE : btnDictation.getVisibility());
        scrollView.setVisibility(isLoading ? View.GONE : scrollView.getVisibility());
        updateRefreshButtonVisibilityInParent();
    }

    private void updateRefreshButtonVisibilityInParent() {
        if (getParentFragment() instanceof DetailsFragment) {
            ((DetailsFragment) getParentFragment()).updateRefreshButtonVisibility();
        }
    }
}
