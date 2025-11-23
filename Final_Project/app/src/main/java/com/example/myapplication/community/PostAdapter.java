package com.example.myapplication.community;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.List;

// [1] 클래스 선언: <PostAdapter.PostViewHolder> 확인
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> postList;

    public PostAdapter(List<Post> postList) {
        this.postList = postList;
    }

    @NonNull
    @Override
    // [2] ★★★ 여기가 수정 포인트입니다! ★★★
    // 반환 타입이 RecyclerView.ViewHolder가 아니라 'PostViewHolder'여야 합니다.
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    // [3] 파라미터 타입: PostViewHolder 확인
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);

        holder.tvTitle.setText(post.getTitle());
        holder.tvContent.setText(post.getContentPreview());
        holder.tvAuthor.setText(post.getAuthor());
        holder.tvDate.setText(post.getFormattedTime());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), PostDetailActivity.class);
            intent.putExtra("post_data", post);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return postList != null ? postList.size() : 0;
    }

    // 뷰홀더 클래스
    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvAuthor, tvDate;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            // XML ID 연결 (R.id.xxx는 본인의 xml 파일에 맞게 확인 필요)
            tvTitle = itemView.findViewById(R.id.post_title);
            tvContent = itemView.findViewById(R.id.post_content_preview);
            tvAuthor = itemView.findViewById(R.id.post_author);
            tvDate = itemView.findViewById(R.id.post_timestamp);
        }
    }
}