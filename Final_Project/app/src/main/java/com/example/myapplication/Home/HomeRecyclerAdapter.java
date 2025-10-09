package com.example.myapplication.Home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.Recording;

import java.util.List;

public class HomeRecyclerAdapter extends RecyclerView.Adapter<HomeRecyclerAdapter.ViewHolder> {

    private final List<Recording> recordingList; // 1. 데이터 목록을 저장할 변수

    // --- 클릭 리스너를 위한 인터페이스 ---
    public interface OnItemClickListener {
        void onItemClick(Recording item);
    }
    private OnItemClickListener listener;
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    // ------------------------------------

    // 2. 생성자: 외부에서 데이터 목록을 받아와서 어댑터의 변수와 연결
    public HomeRecyclerAdapter(List<Recording> recordingList) {
        this.recordingList = recordingList;
    }

    // 3. ViewHolder 생성: item_home_recording.xml을 inflate(객체화)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_recording, parent, false);
        return new ViewHolder(view);
    }

    // 4. ViewHolder에 데이터 바인딩
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recording item = recordingList.get(position);
        holder.bind(item, listener);
    }

    // 5. 전체 아이템 개수 반환
    @Override
    public int getItemCount() {
        return recordingList.size();
    }

    // ViewHolder 내부 클래스
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvDate;
        private final TextView tvProblems;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_item_title);
            tvDate = itemView.findViewById(R.id.tv_item_date);
            tvProblems = itemView.findViewById(R.id.tv_item_problems);
        }

        public void bind(final Recording item, final OnItemClickListener listener) {
            tvTitle.setText(item.getTitle());
            tvDate.setText(item.getDate());
            tvProblems.setText(item.getProblemCount() + "문제");

            // 아이템 뷰에 클릭 리스너 설정
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });
        }
    }
}
