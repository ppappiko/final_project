package com.example.myapplication.community;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Post implements Serializable{
    // 서버 DB의 ID (수정/삭제 시 필요)
    @SerializedName("id") // 서버에서 보내주는 JSON 키 이름이 "id"일 경우
    private Long id;

    private String title;

    // 서버에서는 content로 오지만, 앱에서는 contentPreview로 쓰고 싶다면 매핑
    @SerializedName("content")
    private String contentPreview;

    @SerializedName("authorName")
    private String author;

    @SerializedName("createdAt")
    private String timestamp;

    @SerializedName("attachmentFileName")
    private String attachmentFileName;

    @SerializedName("attachedQuizKey")
    private String attachedQuizKey;

    // 기본 생성자 (Retrofit용)
    public Post() {}

    public Long getId() {
        return id;
    }

    // Getter
    public String getTitle() { return title; }
    public String getContentPreview() { return contentPreview; }
    public String getAuthor() { return author; }
    public String getTimestamp() { return timestamp; }

    public String getAttachmentFileName() { return attachmentFileName; }

    public String getAttachedQuizKey() { return attachedQuizKey; }
    public String getFormattedTime() {
        if (timestamp == null || timestamp.isEmpty()) return "";

        // 서버 시간 형식: "2025-11-23T02:34:50.052093"
        // 구형 방식은 소수점 이하 초(Nanoseconds) 처리가 까다로워서, 앞부분만 잘라서 씁니다.
        String timeToParse = timestamp;
        if (timestamp.length() > 19) {
            timeToParse = timestamp.substring(0, 19); // "2025-11-23T02:34:50" 까지만 자름
        }

        // T를 공백으로 바꿔서 파싱하기 쉽게 만듦
        timeToParse = timeToParse.replace("T", " ");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        try {
            Date date = sdf.parse(timeToParse);
            long now = System.currentTimeMillis();
            long diff = now - date.getTime(); // 차이 (밀리초)

            long diffSec = TimeUnit.MILLISECONDS.toSeconds(diff);
            long diffMin = TimeUnit.MILLISECONDS.toMinutes(diff);
            long diffHour = TimeUnit.MILLISECONDS.toHours(diff);
            long diffDays = TimeUnit.MILLISECONDS.toDays(diff);

            if (diffSec < 60) {
                return "방금 전";
            } else if (diffMin < 60) {
                return diffMin + "분 전";
            } else if (diffHour < 24) {
                return diffHour + "시간 전";
            } else if (diffDays < 7) {
                return diffDays + "일 전";
            } else {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
                return dateFormat.format(date);
            }

        } catch (ParseException e) {
            e.printStackTrace();
            return timeToParse; // 에러나면 원본 반환
        }
    }
}