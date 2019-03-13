package org.mukdongjeil.mjchurch.ui.board_add;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.mukdongjeil.mjchurch.AppExecutors;
import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.data.database.entity.BoardEntity;
import org.mukdongjeil.mjchurch.data.database.entity.User;
import org.mukdongjeil.mjchurch.data.network.BoardNetworkDataSource;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class BoardAddActivity extends AppCompatActivity {
    private static final String TAG = BoardAddActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 1001;

    private MaterialButton mBtnAdd;
    private TextInputEditText mEditContent;

    private FirebaseUser mUser;
    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            if (TextUtils.isEmpty(mEditContent.getText().toString().trim())) {
                mBtnAdd.setEnabled(false);
            } else {
                mBtnAdd.setEnabled(true);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_add);

        setupViewAndEvents();
        setupUser();
    }

    private void setupViewAndEvents() {
        findViewById(R.id.btn_cancel).setOnClickListener(view -> {
            finish();
        });

        mBtnAdd = findViewById(R.id.btn_add);
        mBtnAdd.setEnabled(false);
        mBtnAdd.setOnClickListener(view -> {
            if (mUser == null) {
                showToast("게시판에 글을 올리려면 로그인 해야 합니다.");
                callLoginUI();
                return;
            }

            mBtnAdd.setEnabled(false);
            mEditContent.clearFocus();
            hideSoftKeyboard();
            BoardEntity entity = new BoardEntity();
            entity.setContent(mEditContent.getText().toString());
            entity.setWriter(new User(mUser.getUid(), mUser.getDisplayName(), mUser.getPhotoUrl().toString()));
            entity.setCreateAt(System.currentTimeMillis());

            BoardNetworkDataSource dataSource = BoardNetworkDataSource.getInstance(this, AppExecutors.getInstance());
            dataSource.addBoard(entity, ((isSucceed, message) -> {
                mBtnAdd.setEnabled(true);
                if (isSucceed) {
                    showToast("게시글이 작성되었습니다.");
                    finish();
                } else {
                    showToast("게시글 작성이 실패했습니다. " + message == null ? "" : message);
                }
            }));
        });

        mEditContent = findViewById(R.id.edit_content);
        mEditContent.addTextChangedListener(mTextWatcher);
        mEditContent.requestFocus();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                mUser = FirebaseAuth.getInstance().getCurrentUser();

            } else {
                if (response != null) {
                    Crashlytics.log(Log.ERROR, TAG, "Login failed : " + response.getError().getMessage());
                } else {
                    Log.i(TAG, "Login failed : User canceled");
                }
            }
        }
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditContent.getWindowToken(), 0);
    }

    private void showToast(String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 250);
        toast.show();
    }

    private void setupUser() {
        if (FirebaseAuth.getInstance() != null) {
            mUser = FirebaseAuth.getInstance().getCurrentUser();
        }

        if (mUser == null) {
            callLoginUI();
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
}
