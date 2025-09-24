package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecentExamAdapter extends RecyclerView.Adapter<RecentExamAdapter.ViewHolder> {

    // 아이템 데이터 모델 (제목 = 파일명, date = 수정시각, countLabel = 설명)
    public static class ExamItem {
        private final String title;
        private final String date;
        private final String countLabel;

        public ExamItem(String title, String date, String countLabel) {
            this.title = title;
            this.date = date;
            this.countLabel = countLabel;
        }

        public String getTitle() { return title; }
        public String getDate() { return date; }
        public String getCountLabel() { return countLabel; }
    }

    public interface OnItemClickListener {
        void onItemClick(ExamItem item, int position);
    }

    private final List<ExamItem> items;
    private final OnItemClickListener listener;

    public RecentExamAdapter(@NonNull List<ExamItem> items,
                             @NonNull OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public RecentExamAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.file_list, parent, false); // ← item 레이아웃: file_list.xml
        return new ViewHolder(v, listener, items);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentExamAdapter.ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    @Override
    public long getItemId(int position) {
        ExamItem item = items.get(position);
        return (item.getTitle() + "|" + item.getDate()).hashCode();
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView tvTitle;
        private final TextView tvSub;
        private final OnItemClickListener listener;
        private final List<ExamItem> items;

        ViewHolder(@NonNull View itemView,
                   @NonNull OnItemClickListener listener,
                   @NonNull List<ExamItem> items) {
            super(itemView);
            this.listener = listener;
            this.items = items;
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSub   = itemView.findViewById(R.id.tvSub);
            itemView.setOnClickListener(this);
        }

        void bind(@NonNull ExamItem item) {
            tvTitle.setText(item.getTitle());
            // 예: "2025.09.24 22:10   녹음 파일"
            tvSub.setText(item.getDate() + "   " + item.getCountLabel());
        }

        @Override
        public void onClick(View v) {
            // 구버전 호환 위해 getAdapterPosition() 사용
            int pos = getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onItemClick(items.get(pos), pos);
            }
        }
    }
}