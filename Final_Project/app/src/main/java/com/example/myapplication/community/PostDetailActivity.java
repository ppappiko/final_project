package com.example.myapplication.community;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.ApiClient;
import com.example.myapplication.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostDetailActivity extends AppCompatActivity {

    // 1. 게시글 정보 보여줄 뷰들
    private TextView tvBack, tvTitle, tvAuthor, tvDate, tvContent;

    // 2. 댓글 관련 뷰들
    private RecyclerView rvComments;
    private EditText etComment;
    private Button btnSend;

    // 3. 데이터
    private Post currentPost;
    private CommentAdapter commentAdapter;
    private LinearLayout layoutAttachment; // 추가
    private TextView tvAttachmentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // --- [1] 뷰 초기화 (findViewById) ---

        // 게시글 영역
        tvBack = findViewById(R.id.btn_back);
        tvTitle = findViewById(R.id.detail_title);
        tvAuthor = findViewById(R.id.detail_author);
        tvDate = findViewById(R.id.detail_date);
        tvContent = findViewById(R.id.detail_content);

        // 댓글 영역
        rvComments = findViewById(R.id.rv_comments);
        etComment = findViewById(R.id.et_comment);
        btnSend = findViewById(R.id.btn_send_comment);

        layoutAttachment = findViewById(R.id.layout_attachment);
        tvAttachmentName = findViewById(R.id.tv_attachment_name);


        // --- [2] 데이터 받아오기 & 화면 표시 ---

        // 목록 화면에서 넘겨준 Post 객체 받기
        currentPost = (Post) getIntent().getSerializableExtra("post_data");

        if (currentPost != null) {

            tvTitle.setText(currentPost.getTitle());
            tvAuthor.setText(currentPost.getAuthor()); // (Post.java의 변수명에 따라 getAuthorName()일 수 있음)
            tvContent.setText(currentPost.getContentPreview()); // (전체 내용을 가져오는 Getter 사용 권장)

            // 날짜 포맷팅 메서드가 있다면 사용
            if (currentPost.getTimestamp() != null) {
                tvDate.setText(currentPost.getTimestamp().replace("T", " ").substring(0, 16));
            }

            String fileName = currentPost.getAttachmentFileName();

            if (fileName != null && !fileName.isEmpty()) {
                // 파일이 있으면 보이게 설정
                layoutAttachment.setVisibility(View.VISIBLE);

                // "uuid_진짜이름.pdf" 형식일 수 있으므로 앞의 uuid 제거하고 보여주기 (선택사항)
                // (서버가 그냥 보냈다면 그대로 표시)
                String displayName = fileName;
                if (fileName.contains("_")) {
                    displayName = fileName.substring(fileName.indexOf("_") + 1);
                }
                tvAttachmentName.setText(displayName);

                // (선택) 클릭 시 다운로드 기능 등을 연결할 수 있음
                layoutAttachment.setOnClickListener(v -> {
                    Toast.makeText(this, "파일 다운로드는 추후 구현 예정입니다.", Toast.LENGTH_SHORT).show();
                });

            } else {
                // 파일 없으면 숨김
                layoutAttachment.setVisibility(View.GONE);
            }
        } else {
            Toast.makeText(this, "게시글 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish(); // 정보가 없으면 화면 종료
            return;
        }



        // --- [3] 기능 설정 ---

        // 뒤로가기 버튼 클릭 시
        tvBack.setOnClickListener(v -> finish());

        // 댓글 리스트 설정
        rvComments.setLayoutManager(new LinearLayoutManager(this));

        // 댓글 목록 서버에서 가져오기
        loadComments();

        // 댓글 등록 버튼 클릭 시
        btnSend.setOnClickListener(v -> {
            String content = etComment.getText().toString();
            if (!content.isEmpty()) {
                postComment(content);
            } else {
                Toast.makeText(this, "내용을 입력하세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- [4] 서버 통신 메서드들 ---

    private void postComment(String content) {
        // 1. 토큰 가져오기
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String token = "Bearer " + prefs.getString("jwt_token", "");

        Map<String, String> body = new HashMap<>();
        body.put("content", content);

        // ▼▼▼ [로그 확인] 로그캣에서 "CHECK_ID"로 검색해보세요 ▼▼▼
        android.util.Log.d("CHECK_ID", "보내려는 게시글 ID: " + currentPost.getId());

        if (currentPost.getId() == null) {
            Toast.makeText(this, "게시글 오류: ID가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. 서버에 댓글 저장 요청
        ApiClient.getClient().create(ApiService.class)
                .createComment(token, currentPost.getId(), body)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            // ▼▼▼ [핵심 수정] ▼▼▼

                            // 1. 입력창 비우기
                            etComment.setText("");

                            // 2. 키보드 내리기 (선택 사항, 사용자 경험 향상)
                            hideKeyboard();

                            // 3. ★ 댓글 목록 다시 불러오기 ★
                            // 이 함수가 실행되면서 리스트가 최신 상태(방금 쓴 댓글 포함)로 갱신됩니다.
                            loadComments();

                            Toast.makeText(PostDetailActivity.this, "댓글이 등록되었습니다.", Toast.LENGTH_SHORT).show();

                            // ▲▲▲ [수정 완료] ▲▲▲
                        } else {
                            Toast.makeText(PostDetailActivity.this, "등록 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(PostDetailActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadComments() {

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String token = "Bearer " + prefs.getString("jwt_token", "");

        // (주의) Post 클래스에 getId() 메서드가 있어야 합니다. (서버 DB의 PK)
        ApiClient.getClient().create(ApiService.class)
                .getComments(token, currentPost.getId())
                .enqueue(new Callback<List<Comment>>() {
                    @Override
                    public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                        if (response.isSuccessful() && response.body() != null) {

                            List<Comment> comments = response.body();

                            // ▼▼▼ [로그 추가] Logcat에서 "CHECK_COMMENT"로 검색해보세요 ▼▼▼
                            android.util.Log.d("CHECK_COMMENT", "받은 댓글 개수: " + comments.size());
                            if (!comments.isEmpty()) {
                                android.util.Log.d("CHECK_COMMENT", "첫번째 댓글 내용: " + comments.get(0).getContent());
                            }

                            commentAdapter = new CommentAdapter(response.body());
                            rvComments.setAdapter(commentAdapter);



                        }
                    }

                    @Override
                    public void onFailure(Call<List<Comment>> call, Throwable t) {
                        Toast.makeText(PostDetailActivity.this, "댓글 로드 실패", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // (키보드 숨기기 헬퍼 메서드 - PostDetailActivity 안에 추가하세요)
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}