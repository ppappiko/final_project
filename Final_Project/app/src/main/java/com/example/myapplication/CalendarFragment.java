package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    // 뷰 요소들
    private CalendarView calendarView;
    private CardView lectureCard;
    private TextView lectureTitle, lectureDate, lectureDuration;

    // 데이터 저장용 Map
    private HashMap<String, List<Recording>> recordingsByDate = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // fragment_calendar.xml 파일을 화면으로 만듭니다.
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 액티비티의 onCreate에서 하던 작업을 여기서 합니다.
        // 프래그먼트에서는 view.findViewById()를 사용해야 합니다.
        calendarView = view.findViewById(R.id.calendarView);
        lectureCard = view.findViewById(R.id.lectureCard);
        lectureTitle = view.findViewById(R.id.lectureTitle);
        lectureDate = view.findViewById(R.id.lectureDate);
        lectureDuration = view.findViewById(R.id.lectureDuration);

        // 샘플 데이터 로드 및 캘린더 설정
        loadSampleData();
        setupCalendar();

        // 처음 화면이 보일 때 오늘 날짜의 정보 표시
        Calendar today = Calendar.getInstance();
        String todayKey = String.format(Locale.KOREA, "%d-%02d-%02d",
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH) + 1,
                today.get(Calendar.DAY_OF_MONTH));
        updateLectureCard(todayKey);
    }

    private void setupCalendar() {
        calendarView.setOnDateChangeListener((v, year, month, dayOfMonth) -> {
            String selectedDateKey = String.format(Locale.KOREA, "%d-%02d-%02d", year, month + 1, dayOfMonth);
            updateLectureCard(selectedDateKey);
        });
    }

    private void updateLectureCard(String dateKey) {
        List<Recording> selectedRecordings = recordingsByDate.get(dateKey);

        if (selectedRecordings != null && !selectedRecordings.isEmpty()) {
            Recording recording = selectedRecordings.get(0);
            lectureTitle.setText(recording.getTitle());
            lectureDuration.setText(recording.getDuration());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.KOREA);
            lectureDate.setText(sdf.format(recording.getDate()));

            lectureCard.setVisibility(View.VISIBLE);
        } else {
            lectureCard.setVisibility(View.GONE);
        }
    }

    // --- 데이터 관련 메소드들 (이전과 동일) ---
    private void loadSampleData() {
        addRecording("프로젝트실무1", 2025, 7, 15, "30분"); // 월은 0부터 시작 (7 -> 8월)
        addRecording("자료구조", 2025, 7, 17, "55분");

        Calendar today = Calendar.getInstance();
        addRecording("알고리즘", today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH), "10분");
    }

    private void addRecording(String title, int year, int month, int day, String duration) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        Date date = calendar.getTime();
        Recording rec = new Recording(title, date, duration);

        String key = String.format(Locale.KOREA, "%d-%02d-%02d", year, month + 1, day);

        if (!recordingsByDate.containsKey(key)) {
            recordingsByDate.put(key, new ArrayList<>());
        }
        recordingsByDate.get(key).add(rec);
    }

    // 데이터 모델 클래스 (프래그먼트 안에 두거나 별도 파일로 분리 가능)
    class Recording {
        private String title;
        private Date date;
        private String duration;

        public Recording(String title, Date date, String duration) {
            this.title = title; this.date = date; this.duration = duration;
        }
        public String getTitle() { return title; }
        public Date getDate() { return date; }
        public String getDuration() { return duration; }
    }
}