package com.example.myapplication.question;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Home.Detail.Question.Question;
import com.example.myapplication.R;

import java.util.List;

public class QuizAnswerListAdapter extends RecyclerView.Adapter<QuizAnswerListAdapter.AnswerViewHolder> {

    private List<Question> questionList;
    private Context context;

    public QuizAnswerListAdapter(List<Question> questionList) {
        this.questionList = questionList;
    }

    @NonNull
    @Override
    public AnswerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_quiz_result, parent, false);
        return new AnswerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnswerViewHolder holder, int position) {
        Question question = questionList.get(position);

        holder.tvQuestion.setText("Q" + (position + 1) + ". " + question.getQuestionText());

        // 사용자가 선택한 답 텍스트
        String userAnswerText = "선택 안 함";
        int userAnswerIndex = question.getUserAnswer();
        if (userAnswerIndex != -1) {
            userAnswerText = question.getOptions().get(userAnswerIndex);
        }

        // 실제 정답 텍스트
        int correctAnswerIndex = question.getCorrectAnswerIndex();
        String correctAnswerText = question.getOptions().get(correctAnswerIndex);

        holder.tvUserAnswer.setText("• 나의 답: " + userAnswerText);
        holder.tvCorrectAnswer.setText("• 정답: " + correctAnswerText);

        // [핵심] 정답/오답 비교하여 UI 변경
        if (userAnswerIndex == correctAnswerIndex) {
            // 정답
            holder.itemBackground.setBackground(ContextCompat.getDrawable(context, R.drawable.background_result_correct));
            holder.tvCorrectAnswer.setVisibility(View.GONE); // 맞았으면 숨김
        } else {
            // 오답
            holder.itemBackground.setBackground(ContextCompat.getDrawable(context, R.drawable.background_result_wrong));
            holder.tvCorrectAnswer.setVisibility(View.VISIBLE); // 틀렸으면 보여줌
        }
    }

    @Override
    public int getItemCount() {
        return questionList.size();
    }

    // --- ViewHolder ---
    public static class AnswerViewHolder extends RecyclerView.ViewHolder {
        LinearLayout itemBackground;
        TextView tvQuestion, tvUserAnswer, tvCorrectAnswer;

        public AnswerViewHolder(@NonNull View itemView) {
            super(itemView);
            itemBackground = itemView.findViewById(R.id.ll_result_item_background);
            tvQuestion = itemView.findViewById(R.id.tv_result_question);
            tvUserAnswer = itemView.findViewById(R.id.tv_result_user_answer);
            tvCorrectAnswer = itemView.findViewById(R.id.tv_result_correct_answer);
        }
    }
}