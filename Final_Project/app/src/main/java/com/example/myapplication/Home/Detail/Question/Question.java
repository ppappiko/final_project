package com.example.myapplication.Home.Detail.Question;

import java.io.Serializable;
import java.util.List;

/**
 * AI가 생성한 문제 데이터를 담는 클래스 (JSON 구조와 일치)
 * 프래그먼트 간 전달을 위해 Serializable을 구현합니다.
 */
public class Question implements Serializable {

    private String questionText;
    private List<String> options;
    private int correctAnswerIndex;

    // ▼ (퀴즈 진행 중 사용자가 선택한 답을 저장) ▼
    private int userAnswer = -1; // -1은 "아직 안 품"

    // --- Getter ---
    public String getQuestionText() {
        return questionText;
    }

    public List<String> getOptions() {
        return options;
    }

    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    public int getUserAnswer() {
        return userAnswer;
    }

    // --- Setter ---
    public void setUserAnswer(int userAnswer) {
        this.userAnswer = userAnswer;
    }

    // (Gson 파싱을 위한 Setter들 - 이미 있다고 가정)
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public void setOptions(List<String> options) { this.options = options; }
    public void setCorrectAnswerIndex(int correctAnswerIndex) { this.correctAnswerIndex = correctAnswerIndex; }
}