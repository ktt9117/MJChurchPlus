package org.mukdongjeil.mjchurch.ui.board_detail;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.data.database.entity.ReplyEntity;
import org.mukdongjeil.mjchurch.data.database.entity.User;
import org.mukdongjeil.mjchurch.ui.extension.SoftKeyboard;
import org.mukdongjeil.mjchurch.ui.extension.WrapContentLinearLayoutManager;
import org.mukdongjeil.mjchurch.util.InjectorUtils;
import org.mukdongjeil.mjchurch.util.OnItemClickListener;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;

public class BoardDetailActivity extends AppCompatActivity implements OnItemClickListener, SoftKeyboard.SoftKeyboardChanged {
    private static final String TAG = BoardDetailActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 1001;

    public static final String INTENT_KEY_BOARD_ID = "boardId";

    private ActionBar mActionBar;
    private BoardDetailViewModel mViewModel;

    private String mBoardId;

    private FirebaseUser mUser;
    private AppCompatEditText mEditView;
    private RecyclerView mRecyclerView;
    private SlideInBottomAnimationAdapter mReplyAdapter;
    private SoftKeyboard mKeyboard;
    private int mPosition = RecyclerView.NO_POSITION;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_detail);

        setupView();
        setupToolbar();
        setupUser();

        mBoardId = getIntent().getStringExtra(INTENT_KEY_BOARD_ID);
        if (!TextUtils.isEmpty(mBoardId)) {
            injectViewModel(mBoardId);

        } else {
            Log.e(TAG, "BoardDetailActivity force finished caused by invalid parameter(boardId)");
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mKeyboard != null) mKeyboard.unRegisterSoftKeyboardCallback();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void setupView() {
        findViewById(R.id.detail_reply_send_button).setOnClickListener(view -> sendReply());
        mEditView = findViewById(R.id.detail_edit_view);
        mRecyclerView = findViewById(R.id.recyclerview_replies);
        mRecyclerView.setOnClickListener(view -> hideSoftKeyboard());
        WrapContentLinearLayoutManager layoutManager =
                new WrapContentLinearLayoutManager(this, RecyclerView.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mReplyAdapter = new SlideInBottomAnimationAdapter(new BoardDetailAdapter(this, this));
        mRecyclerView.setAdapter(mReplyAdapter);

        LinearLayout containerView = findViewById(R.id.container_view);
        mKeyboard = new SoftKeyboard(containerView, (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE));
        mKeyboard.setSoftKeyboardCallback(this);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setDisplayShowHomeEnabled(true);
            mActionBar.setTitle(R.string.title_board_long);
        }
    }

    private void setupUser() {
        if (FirebaseAuth.getInstance() != null) {
            mUser = FirebaseAuth.getInstance().getCurrentUser();
        }
    }

    private void sendReply() {
        String replyContent = mEditView.getText().toString();
        if (TextUtils.isEmpty(replyContent.trim())) {
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
        mViewModel.addReply(entity);
        mPosition = mReplyAdapter.getItemCount();

        hideSoftKeyboard();
        mEditView.setText(null);
        Toast.makeText(this, R.string.reply_sent_message, Toast.LENGTH_LONG).show();
    }

    private void injectViewModel(String boardId) {
        BoardDetailViewModelFactory factory = InjectorUtils.provideBoardDetailViewModelFactory(this, boardId);
        mViewModel = ViewModelProviders.of(this, factory).get(BoardDetailViewModel.class);
        mViewModel.getBoard().observe(this, boardEntity -> {
            BoardDetailAdapter adapter = ((BoardDetailAdapter) mReplyAdapter.getWrappedAdapter());
            adapter.setHeaderContent(boardEntity);
        });

        mViewModel.getReplyList().observe(this, replyEntities -> {
            BoardDetailAdapter adapter = ((BoardDetailAdapter) mReplyAdapter.getWrappedAdapter());
            adapter.swapList(replyEntities);
            if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
            if (mPosition != 0) {
                try {
                    mRecyclerView.smoothScrollToPosition(mPosition);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
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

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditView.getWindowToken(), 0);
    }

    @Override
    public void onItemClick(View v) {
        if (v.getId() == R.id.btn_more) {
            //TODO: display edit or remove function menu
            Log.e(TAG, "btn more clicked");

        } else {
            hideSoftKeyboard();
        }
    }

    @Override
    public void onSoftKeyboardHide() {}

    @Override
    public void onSoftKeyboardShow() {
        if (mRecyclerView != null) mRecyclerView.smoothScrollToPosition(mReplyAdapter.getItemCount());
    }
}