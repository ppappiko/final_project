package com.example.myapplication.Home.Detail.Transcript;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

public class TranscriptFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // fragment_transcript.xml 레이아웃을 화면으로 만듭니다.
        return inflater.inflate(R.layout.fragment_transcript, container, false);
    }
}
