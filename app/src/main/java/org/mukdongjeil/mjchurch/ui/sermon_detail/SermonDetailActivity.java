package org.mukdongjeil.mjchurch.ui.sermon_detail;

import android.app.Service;
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
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

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
import org.mukdongjeil.mjchurch.data.database.entity.ReplyEntity;
import org.mukdongjeil.mjchurch.data.database.entity.User;
import org.mukdongjeil.mjchurch.ui.extension.SoftKeyboard;
import org.mukdongjeil.mjchurch.ui.extension.WrapContentLinearLayoutManager;
import org.mukdongjeil.mjchurch.util.InjectorUtils;
import org.mukdongjeil.mjchurch.util.OnItemClickListener;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;

public class SermonDetailActivity extends AppCompatActivity
        implements SoftKeyboard.SoftKeyboardChanged, YouTubePlayerFullScreenListener, OnItemClickListener {

    private static final String TAG = SermonDetailActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 1001;

    public static final String INTENT_KEY_BBS_NO = "bbsNo";

    private SermonDetailActivityViewModel mViewModel;

    private RecyclerView mRecyclerView;
    private SlideInBottomAnimationAdapter mAdapter;
    private int mPosition = RecyclerView.NO_POSITION;

    private EditText mEditView;
    private String mBbsNo;
    private FirebaseUser mUser;
    private SoftKeyboard mKeyboard;
    private YouTubePlayerView mPlayerView;
    private View mReplyContainer;

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
        mBbsNo = intent.getStringExtra(INTENT_KEY_BBS_NO);
        if (!TextUtils.isEmpty(mBbsNo)) {
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
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mKeyboard != null) mKeyboard.unRegisterSoftKeyboardCallback();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
                setupUser();
                invalidateOptionsMenu();

            } else {
                if (response != null) {
                    Log.e(TAG, "Login failed : " + response.getError().getMessage());
                } else {
                    Log.i(TAG, "Login failed : User canceled");
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
        mEditView = findViewById(R.id.detail_edit_view);
        findViewById(R.id.detail_reply_send_button).setOnClickListener(view -> sendReply());
        mReplyContainer = findViewById(R.id.detail_reply_container);
        mRecyclerView = findViewById(R.id.recyclerview_sermon_replies);

        WrapContentLinearLayoutManager layoutManager =
                new WrapContentLinearLayoutManager(this, RecyclerView.VERTICAL, false);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mAdapter = new SlideInBottomAnimationAdapter(new SermonDetailAdapter(this, this));
        mRecyclerView.setAdapter(mAdapter);

        LinearLayout containerView = findViewById(R.id.container_view);
        mKeyboard = new SoftKeyboard(containerView, (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE));
        mKeyboard.setSoftKeyboardCallback(this);

        mPlayerView = findViewById(R.id.detail_video_view);
        getLifecycle().addObserver(mPlayerView);
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

        ReplyEntity entity = new ReplyEntity(
                replyContent,
                new User(mUser.getUid(), mUser.getDisplayName(), mUser.getPhotoUrl().toString()),
                System.currentTimeMillis());
        mViewModel.addReply(mBbsNo, entity);
        mPosition = mAdapter.getItemCount();

        hideSoftKeyboard();
        mEditView.setText(null);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void injectViewModel(String bbsNo) {
        SermonDetailActivityViewModelFactory factory =
                InjectorUtils.provideSermonDetailActivityViewModelFactory(this.getApplicationContext(), bbsNo);
        mViewModel = ViewModelProviders.of(this, factory).get(SermonDetailActivityViewModel.class);
        mViewModel.getSermonEntity().observe(this, sermonEntity -> {
            if (sermonEntity != null) {
                ((SermonDetailAdapter) mAdapter.getWrappedAdapter()).setHeaderContent(sermonEntity);
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
                            }
                        });
                    }
                }, true);
                mPlayerView.removeFullScreenListener(this);
                mPlayerView.addFullScreenListener(this);
            }
        });

        mViewModel.getSermonReplyList().observe(this, replyEntities -> {
            ((SermonDetailAdapter) mAdapter.getWrappedAdapter()).swapList(replyEntities);
            if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
            try {
                mRecyclerView.smoothScrollToPosition(mPosition);
            } catch (Exception e) { e. printStackTrace(); }
        });
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

    private String getYoutubeContentId(String videoUrl) {
        if (TextUtils.isEmpty(videoUrl) == false && videoUrl.contains("/")) {
            return videoUrl.substring(videoUrl.lastIndexOf("/") + 1, videoUrl.length());
        }

        return null;
    }

    @Override
    public void onSoftKeyboardHide() {}

    @Override
    public void onSoftKeyboardShow() {

    }

    @Override
    public void onYouTubePlayerEnterFullScreen() {
        mReplyContainer.setVisibility(View.GONE);
    }

    @Override
    public void onYouTubePlayerExitFullScreen() {
        mReplyContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onItemClick(View v) {
        hideSoftKeyboard();
    }
}