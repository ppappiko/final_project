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
    private OnItemClickListener clickListener;
    private OnItemLongClickListener longClickListener;

    public interface OnItemClickListener {
        void onItemClick(Recording item);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Recording item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

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
        private final TextView tvTitle, tvDate, tvItemType;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_item_title);
            tvDate = itemView.findViewById(R.id.tv_item_date);
            tvItemType = itemView.findViewById(R.id.tv_item_type); // ID 변경
        }

        public void bind(final Recording item, final OnItemClickListener clickListener, final OnItemLongClickListener longClickListener) {
            tvTitle.setText(item.getTitle());
            tvDate.setText(item.getDate());

            // 파일 경로를 기반으로 파일 타입 설정
            if (item.getFilePath() != null) {
                if (item.getFilePath().toLowerCase().endsWith(".m4a")) {
                    tvItemType.setText("오디오");
                    tvItemType.setVisibility(View.VISIBLE);
                } else if (item.getFilePath().toLowerCase().endsWith(".txt")) {
                    tvItemType.setText("텍스트");
                    tvItemType.setVisibility(View.VISIBLE);
                } else {
                    tvItemType.setVisibility(View.GONE);
                }
            } else {
                tvItemType.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onItemClick(item);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onItemLongClick(item);
                    return true;
                }
                return false;
            });
        }
    }
}
