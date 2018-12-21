package org.mukdongjeil.mjchurch.ui.sermons;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.ui.BaseFragment;
import org.mukdongjeil.mjchurch.ui.sermon_detail.SermonDetailActivity;
import org.mukdongjeil.mjchurch.util.InjectorUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import jp.wasabeef.recyclerview.adapters.SlideInRightAnimationAdapter;

public class SermonFragment extends BaseFragment implements SermonAdapter.SermonAdapterOnItemClickHandler {
    private static final String TAG = SermonFragment.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private SlideInRightAnimationAdapter mSermonAdapter;
    private int mPosition = RecyclerView.NO_POSITION;

    private SermonListViewModel mViewModel;

    private static SermonFragment sInstance;
    public static SermonFragment getInstance() {
        if (sInstance == null) {
            sInstance = new SermonFragment();
        }

        return sInstance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sermons, container, false);
        setBarTitle(getString(R.string.title_sermon_list));
        setupRecyclerView(v);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        injectViewModel();
    }

    private void setupRecyclerView(View v) {
        mRecyclerView = v.findViewById(R.id.recyclerview_sermon_list);
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mSermonAdapter = new SlideInRightAnimationAdapter(new SermonAdapter(getActivity(), this));
        mRecyclerView.setAdapter(mSermonAdapter);
    }

    private void injectViewModel() {
        SermonViewModelFactory factory = InjectorUtils.provideSermonViewModelFactory(getActivity());
        mViewModel = ViewModelProviders.of(this, factory).get(SermonListViewModel.class);
        mViewModel.getSermonList().observe(this, sermonEntities -> {
            ((SermonAdapter) mSermonAdapter.getWrappedAdapter()).swapList(sermonEntities);
            if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
            mRecyclerView.smoothScrollToPosition(mPosition);

            if (sermonEntities != null && sermonEntities.size() != 0) {
                closeLoadingDialog();
            } else {
                showLoadingDialog();
                new Handler().postDelayed(()-> {
                    Toast.makeText(getActivity(), R.string.get_data_failed_message, Toast.LENGTH_LONG).show();
                    Crashlytics.log(Log.WARN, TAG, "cannot get sermon entities");
                    closeLoadingDialog();
                }, 1000 * 10);
            }
        });
    }

    @Override
    public void onItemClick(View v, int bbsNo) {
        Log.d(TAG, "onItemClick : " + bbsNo);
        Intent intent = new Intent(getActivity(), SermonDetailActivity.class);
        intent.putExtra(SermonDetailActivity.INTENT_KEY_BBS_NO, bbsNo);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivity(intent, createTransitionOption(v).toBundle());
        } else {
            startActivity(intent);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private ActivityOptionsCompat createTransitionOption(View v) {
        View titleView = v.findViewById(R.id.title);
        View thumbnailView = v.findViewById(R.id.thumbnail);
        Pair<View, String> p1 = Pair.create(titleView, titleView.getTransitionName());
        Pair<View, String> p2 = Pair.create(thumbnailView, thumbnailView.getTransitionName());
        return ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), p1, p2);
    }
}
