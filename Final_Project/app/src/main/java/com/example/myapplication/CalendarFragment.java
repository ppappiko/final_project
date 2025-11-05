package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Home.Detail.DetailsFragment;
import com.example.myapplication.Home.HomeRecyclerAdapter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;
    private RecyclerView recyclerView;
    private TextView tvSelectedDate, tvEmpty;
    private HomeRecyclerAdapter adapter;
    private List<Recording> allRecordings = new ArrayList<>();
    private List<Recording> filteredRecordings = new ArrayList<>();
    private Calendar currentSelectedCalendar = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        calendarView = view.findViewById(R.id.calendarView);
        recyclerView = view.findViewById(R.id.calendar_recycler_view);
        tvSelectedDate = view.findViewById(R.id.tv_selected_date);
        tvEmpty = view.findViewById(R.id.tv_calendar_empty);

        setupRecyclerView();
        setupCalendarView();

        // 초기 화면 로딩
        loadAndFilter();
    }

    @Override
    public void onResume() {
        super.onResume();
        // 다른 화면에서 돌아왔을 때, 파일 목록 변경사항을 반영하기 위해 다시 로드
        loadAndFilter();
    }

    private void setupRecyclerView() {
        adapter = new HomeRecyclerAdapter(filteredRecordings);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(item -> {
            DetailsFragment detailsFragment = new DetailsFragment();
            Bundle bundle = new Bundle();
            bundle.putString("recordingTitle", item.getTitle());
            bundle.putString("recordingDate", item.getDate());
            bundle.putString("recordingFilePath", item.getFilePath());
            detailsFragment.setArguments(bundle);

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).replaceFragment(detailsFragment);
            }
        });
    }

    private void setupCalendarView() {
        calendarView.setOnDateChangeListener((v, year, month, dayOfMonth) -> {
            currentSelectedCalendar.set(year, month, dayOfMonth);
            filterRecordingsByDate(currentSelectedCalendar);
        });
    }

    private void loadAndFilter(){
        loadAllRecordingsFromStorage();
        filterRecordingsByDate(currentSelectedCalendar);
    }

    private void loadAllRecordingsFromStorage() {
        allRecordings.clear();
        if(getContext() == null) return;
        File recordingsDir = getContext().getExternalFilesDir(null);
        if (recordingsDir != null && recordingsDir.exists()) {
            File[] files = recordingsDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(".m4a")) {
                        String title = file.getName().replace(".m4a", "");
                        String date = new SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(new Date(file.lastModified()));
                        allRecordings.add(new Recording(title, date, 0, file.getAbsolutePath()));
                    }
                }
            }
        }
    }

    private void filterRecordingsByDate(Calendar selectedCalendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.KOREA);
        String selectedDate = sdf.format(selectedCalendar.getTime());

        tvSelectedDate.setText(selectedDate + " 녹음");

        filteredRecordings.clear();
        for (Recording rec : allRecordings) {
            if (rec.getDate().equals(selectedDate)) {
                filteredRecordings.add(rec);
            }
        }

        if(adapter != null) {
            adapter.notifyDataSetChanged();
        }
        updateEmptyView();
    }

    private void updateEmptyView() {
        if (filteredRecordings.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }
}
