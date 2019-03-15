package org.mukdongjeil.mjchurch.ui.boards;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.ui.BaseFragment;
import org.mukdongjeil.mjchurch.ui.board_add.BoardAddActivity;
import org.mukdongjeil.mjchurch.ui.board_detail.BoardDetailActivity;
import org.mukdongjeil.mjchurch.util.InjectorUtils;
import org.mukdongjeil.mjchurch.util.OnItemClickListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;

public class BoardFragment extends BaseFragment implements OnItemClickListener {
    private static final String TAG = BoardFragment.class.getSimpleName();

    private TextInputEditText mBtnWrite;
    private RecyclerView mRecyclerView;
    private SlideInBottomAnimationAdapter mAdapter;

    private BoardListViewModel mViewModel;
    private int mPosition = 0;

    private static BoardFragment sInstance;
    public static BoardFragment getInstance() {
        if (sInstance == null) {
            sInstance = new BoardFragment();
        }

        return sInstance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_boards, container, false);
        setBarTitle(getString(R.string.title_board_long));
        setupView(v);

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        injectViewModel();
    }

    private void setupView(View v) {
        mRecyclerView = v.findViewById(R.id.recyclerview_board_list);
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(VERTICAL_ITEM_SPACE));
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new SlideInBottomAnimationAdapter(new BoardAdapter(getActivity(), this));
        mRecyclerView.setAdapter(mAdapter);

        mBtnWrite = v.findViewById(R.id.btn_write);
        mBtnWrite.setOnClickListener(view -> {
            mPosition = 0;
            startActivity(new Intent(getActivity(), BoardAddActivity.class));
        });
    }

    private void injectViewModel() {
        showLoadingDialog();

        BoardViewModelFactory factory = InjectorUtils.provideBoardViewModelFactory(getActivity());
        mViewModel = ViewModelProviders.of(this, factory).get(BoardListViewModel.class);
        mViewModel.getBoardList().observe(this, boardEntities -> {
            closeLoadingDialog();
            if (boardEntities != null && boardEntities.size() != 0) {
                Log.i(TAG, "boardEntities received : " + boardEntities.size());
                ((BoardAdapter) mAdapter.getWrappedAdapter()).swapList(boardEntities);
                mRecyclerView.setVisibility(View.VISIBLE);
                mRecyclerView.smoothScrollToPosition(mPosition);

            } else {
                Toast.makeText(getActivity(), R.string.get_data_failed_message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private ActivityOptionsCompat createTransitionOption(View v) {
        View avatarView = v.findViewById(R.id.avatar);
        View writerView = v.findViewById(R.id.writer);
        View timestampView = v.findViewById(R.id.timestamp);
        Pair<View, String> p1 = Pair.create(avatarView, avatarView.getTransitionName());
        Pair<View, String> p2 = Pair.create(writerView, writerView.getTransitionName());
        Pair<View, String> p3 = Pair.create(timestampView, timestampView.getTransitionName());
        return ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), p1, p2, p3);
    }

    private static final int VERTICAL_ITEM_SPACE = 24;
    public class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration {

        private final int verticalSpaceHeight;

        public VerticalSpaceItemDecoration(int verticalSpaceHeight) {
            this.verticalSpaceHeight = verticalSpaceHeight;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            outRect.bottom = verticalSpaceHeight;
        }
    }

    @Override
    public void onItemClick(View v) {
        mPosition = (int) v.findViewById(R.id.writer).getTag();
        Intent intent = new Intent(getActivity(), BoardDetailActivity.class);
        intent.putExtra(BoardDetailActivity.INTENT_KEY_BOARD_ID, v.findViewById(R.id.title).getTag().toString());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivity(intent, createTransitionOption(v).toBundle());
        } else {
            startActivity(intent);
        }
    }
}