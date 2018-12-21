package org.mukdongjeil.mjchurch.ui.sermon_detail;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.transition.Fade;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.YouTubePlayerView;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.YouTubePlayerFullScreenListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.YouTubePlayerInitListener;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.data.database.entity.SermonEntity;
import org.mukdongjeil.mjchurch.data.database.entity.SermonReplyEntity;
import org.mukdongjeil.mjchurch.data.database.entity.User;
import org.mukdongjeil.mjchurch.util.InjectorUtils;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;

public class SermonDetailActivity extends AppCompatActivity implements
        YouTubePlayerFullScreenListener {

    private static final String TAG = SermonDetailActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 1001;

    public static final String INTENT_KEY_BBS_NO = "bbsNo";

    private SermonDetailActivityViewModel mViewModel;

    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private SlideInBottomAnimationAdapter mSermonDetailAdapter;
    private int mPosition = RecyclerView.NO_POSITION;

    private TextView mTitleView, mViewCountView, mEmptyMessageView;
    private YouTubePlayerView mPlayerView;

    private ViewGroup mBaseContainerView;
    private ViewGroup mReplyContainerView;
    private EditText mEditView;
    private int mBbsNo;
    private FirebaseUser mUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);

        setContentView(R.layout.activity_sermon_detail);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setupWindowAnimations();
        }

        setupView();
        setupToolbar();
        setupUser();

        Intent intent = getIntent();
        mBbsNo = intent.getIntExtra(INTENT_KEY_BBS_NO, -1);
        Log.i(TAG, "received bbsNo : " + mBbsNo);
        if (mBbsNo != -1) {
            injectViewModel(mBbsNo);

        } else {
            Log.e(TAG, "SermonDetailActivity force finished caused by invalid parameter(bbsNo)");
            finish();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setupWindowAnimations() {
        Fade fade = new Fade();
        fade.setDuration(1500);
        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayEmptyView(mSermonDetailAdapter.getItemCount() > 0 ? false : true);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu");
        if (mUser == null) {
            getMenuInflater().inflate(R.menu.login, menu);
            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.google_sign_in:
                callLoginUI();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                Log.d(TAG, "Login succeed");
                setupUser();
                invalidateOptionsMenu();

            } else {
                if (response == null) {
                    Log.e(TAG, "Login failed : User canceled");
                } else {
                    Log.e(TAG, "Login failed : " + response.getError().getMessage());
                    Crashlytics.log(Log.ERROR, TAG, "Login failed : " + response.getError().getMessage());
                }
            }
        }
    }

    private void callLoginUI() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.GoogleBuilder().build());

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                        RC_SIGN_IN);
    }

    private void setupView() {
        mBaseContainerView = findViewById(R.id.detail_base_container);
        mTitleView = findViewById(R.id.detail_title);
        mViewCountView = findViewById(R.id.detail_view_count);
        mPlayerView = findViewById(R.id.detail_video_view);
        mReplyContainerView = findViewById(R.id.detail_reply_container);
        mEditView = findViewById(R.id.detail_edit_view);
        mEmptyMessageView = findViewById(R.id.empty_message_view);
        mRecyclerView = findViewById(R.id.recyclerview_sermon_replies);

        mLayoutManager =
                new LinearLayoutManager(this, RecyclerView.VERTICAL, false);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        mSermonDetailAdapter = new SlideInBottomAnimationAdapter(new SermonDetailAdapter(this));
        mRecyclerView.setAdapter(mSermonDetailAdapter);

        findViewById(R.id.detail_info).setOnClickListener((view) -> {
            hideSoftKeyboard();
        });

        findViewById(R.id.detail_reply_send_button).setOnClickListener((view)-> {
            sendReply();
        });
    }

    private void hideVideoView() {
        if (mBaseContainerView.getVisibility() == View.VISIBLE) {
            mBaseContainerView.animate().translationYBy(0).translationY(-150)
                    .withEndAction(() -> {
                        mBaseContainerView.setVisibility(View.GONE);
                        ActionBar ab = getSupportActionBar();
                        if (ab != null) {
                            ab.setTitle(mTitleView.getText().toString());
                        }
                    });
        }
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditView.getWindowToken(), 0);
    }

    private void sendReply() {
        String replyContent = mEditView.getText().toString();
        if (TextUtils.isEmpty(replyContent)) {
            return;
        }

        if (mUser == null) {
            callLoginUI();
            return;
        }

        SermonReplyEntity entity = new SermonReplyEntity(
                replyContent,
                new User(mUser.getUid(), mUser.getDisplayName(), mUser.getPhotoUrl().toString()),
                System.currentTimeMillis());
        mViewModel.addReply(mBbsNo, entity);
        mPosition = mSermonDetailAdapter.getItemCount();

        hideSoftKeyboard();
        mEditView.setText(null);
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
            ((SermonDetailAdapter) mSermonDetailAdapter.getWrappedAdapter()).swapList(replyEntities);
            displayEmptyView(mSermonDetailAdapter.getItemCount() > 0 ? false : true);

            if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
            try {
                mRecyclerView.smoothScrollToPosition(mPosition);
            } catch (Exception e) {
                Log.i(TAG, "just ignore it : " + e.getMessage());
            }
        });
    }

    private void displayEmptyView(boolean b) {
        mRecyclerView.setVisibility(b ? View.GONE : View.VISIBLE);
        mEmptyMessageView.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
            ab.setTitle(R.string.title_sermon_detail);
        }
    }

    private void setupUser() {
        if (FirebaseAuth.getInstance() != null) {
            mUser = FirebaseAuth.getInstance().getCurrentUser();
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
        mPlayerView.addFullScreenListener(this);
    }

    private String getYoutubeContentId(String videoUrl) {
        if (TextUtils.isEmpty(videoUrl) == false && videoUrl.contains("/")) {
            return videoUrl.substring(videoUrl.lastIndexOf("/") + 1, videoUrl.length());
        }

        return null;
    }

    @Override
    public void onYouTubePlayerEnterFullScreen() {
        mReplyContainerView.setVisibility(View.GONE);
    }

    @Override
    public void onYouTubePlayerExitFullScreen() {
        mReplyContainerView.setVisibility(View.VISIBLE);

    }
}
