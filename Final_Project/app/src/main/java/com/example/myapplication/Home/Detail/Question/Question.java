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

    // Gson이 JSON을 파싱할 때 기본 생성자가 필요할 수 있습니다.
    public Question() {}

    // Getter와 Setter (Gson과 코드에서 사용)
    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    public void setCorrectAnswerIndex(int correctAnswerIndex) {
        this.correctAnswerIndex = correctAnswerIndex;
    }
}
