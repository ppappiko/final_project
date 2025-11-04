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

    private final List<Recording> recordingList;

    // --- 클릭 리스너 인터페이스 ---
    public interface OnItemClickListener {
        void onItemClick(Recording item);
    }
    private OnItemClickListener clickListener;
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    // --- 길게 누르기 리스너 인터페이스 ---
    public interface OnItemLongClickListener {
        void onItemLongClick(Recording item);
    }
    private OnItemLongClickListener longClickListener;
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public HomeRecyclerAdapter(List<Recording> recordingList) {
        this.recordingList = recordingList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_recording, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recording item = recordingList.get(position);
        holder.bind(item, clickListener, longClickListener);
    }

    @Override
    public int getItemCount() {
        return recordingList.size();
    }

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

        public void bind(final Recording item, final OnItemClickListener clickListener, final OnItemLongClickListener longClickListener) {
            tvTitle.setText(item.getTitle());
            tvDate.setText(item.getDate());
            tvProblems.setText(item.getProblemCount() + "문제");

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onItemClick(item);
                }
            });

            // 길게 누르기 리스너 설정
            itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onItemLongClick(item);
                    return true; // 이벤트를 소비했음을 알림
                }
                return false;
            });
        }
    }
}
