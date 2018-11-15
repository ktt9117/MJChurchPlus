package org.mukdongjeil.mjchurch.ui.sermon_detail;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.pierfrancescosoffritti.androidyoutubeplayer.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.YouTubePlayerView;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.YouTubePlayerInitListener;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.data.database.entity.SermonEntity;
import org.mukdongjeil.mjchurch.util.InjectorUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SermonDetailActivity extends AppCompatActivity {

    private static final String TAG = SermonDetailActivity.class.getSimpleName();

    public static final String INTENT_KEY_BBS_NO = "bbsNo";

    private SermonDetailActivityViewModel mViewModel;

    private RecyclerView mRecyclerView;
    private SermonDetailAdapter mSermonDetailAdapter;
    private int mPosition = RecyclerView.NO_POSITION;

    private TextView mTitleView, mViewCountView;
    private YouTubePlayerView mPlayerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sermon_detail);
        setupView();
        setupToolbar();

        Intent intent = getIntent();
        int bbsNo = intent.getIntExtra(INTENT_KEY_BBS_NO, -1);
        Log.i(TAG, "received bbsNo : " + bbsNo);
        if (bbsNo != -1) {
            injectViewModel(bbsNo);

        } else {
            Log.e(TAG, "SermonDetailActivity force finished caused by invalid parameter(bbsNo)");
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPlayerView.enableBackgroundPlayback(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlayerView.release();
    }

    private void setupView() {
        mTitleView = findViewById(R.id.detail_title);
        mViewCountView = findViewById(R.id.detail_view_count);
        mPlayerView = findViewById(R.id.detail_video_view);

        mRecyclerView = findViewById(R.id.recyclerview_sermon_replies);
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mSermonDetailAdapter = new SermonDetailAdapter(this);
        mRecyclerView.setAdapter(mSermonDetailAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void injectViewModel(int bbsNo) {
        SermonDetailActivityViewModelFactory factory =
                InjectorUtils.provideSermonDetailActivityViewModelFactory(this.getApplicationContext(), bbsNo);
        mViewModel = ViewModelProviders.of(this, factory).get(SermonDetailActivityViewModel.class);

        mViewModel.getSermonEntity().observe(this, sermonEntity -> {
            if (sermonEntity != null) showSermonDetailDataView(sermonEntity);
        });

        mViewModel.getSermonReplyList().observe(this, replyEntities -> {
            mSermonDetailAdapter.swapList(replyEntities);
            if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
            mRecyclerView.smoothScrollToPosition(mPosition);
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
            ab.setTitle(R.string.sermon_detail_title);
        }
    }

    private void showSermonDetailDataView(final SermonEntity sermonEntity) {
        mRecyclerView.setVisibility(View.VISIBLE);

        mTitleView.setText(sermonEntity.getTitle());
        mViewCountView.setText(String.format("조회수 %d회", sermonEntity.getViewCount()));

        mPlayerView.initialize(new YouTubePlayerInitListener() {
            @Override
            public void onInitSuccess(@NonNull YouTubePlayer youTubePlayer) {
                youTubePlayer.addListener(new AbstractYouTubePlayerListener() {
                    @Override
                    public void onReady() {
                        super.onReady();
                        String contentId = getYoutubeContentId(sermonEntity.getVideoUrl());
                        if (TextUtils.isEmpty((contentId)) == false) {
                            youTubePlayer.loadVideo(contentId, 0);
                        }
                    }

                    @Override
                    public void onStateChange(@NonNull PlayerConstants.PlayerState state) {
                        super.onStateChange(state);
                        Log.i(TAG, "onStateChange : " + state.name());
                    }
                });
            }
        }, true);
    }

    private String getYoutubeContentId(String videoUrl) {
        if (TextUtils.isEmpty(videoUrl) == false && videoUrl.contains("/")) {
            return videoUrl.substring(videoUrl.lastIndexOf("/") + 1, videoUrl.length());
        }

        return null;
    }
}
