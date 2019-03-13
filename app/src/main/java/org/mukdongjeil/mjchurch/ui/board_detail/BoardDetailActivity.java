package org.mukdongjeil.mjchurch.ui.board_detail;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.util.DateUtil;
import org.mukdongjeil.mjchurch.util.InjectorUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

public class BoardDetailActivity extends AppCompatActivity {
    private static final String TAG = BoardDetailActivity.class.getSimpleName();

    public static final String INTENT_KEY_BOARD_ID = "boardId";

    private BoardDetailViewModel mViewModel;
    private TextView contentView, timestampView, writerView;
    private ImageView avatarView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_detail);

        setupView();
        String boardId = getIntent().getStringExtra(INTENT_KEY_BOARD_ID);
        injectViewModel(boardId);
    }

    private void injectViewModel(String boardId) {
        BoardDetailViewModelFactory factory = InjectorUtils.provideBoardDetailViewModelFactory(this);
        mViewModel = ViewModelProviders.of(this, factory).get(BoardDetailViewModel.class);
        mViewModel.getBoard(boardId).observe(this, boardEntity -> {
            contentView.setText(boardEntity.getContent());
            timestampView.setText(DateUtil.convertReadableDateTime(boardEntity.getCreatedAt()));
            writerView.setText(boardEntity.getWriter().getDisplayName());
        });
    }


    private void setupView() {
        contentView = findViewById(R.id.content);
        timestampView = findViewById(R.id.timestamp);
        writerView = findViewById(R.id.writer);
        avatarView = findViewById(R.id.avatar);
    }
}
