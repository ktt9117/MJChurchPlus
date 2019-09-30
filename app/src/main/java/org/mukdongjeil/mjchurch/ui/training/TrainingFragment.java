package org.mukdongjeil.mjchurch.ui.training;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.tabs.TabLayout;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.data.database.entity.ImageEntity;
import org.mukdongjeil.mjchurch.ui.BaseFragment;
import org.mukdongjeil.mjchurch.util.InjectorUtils;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class TrainingFragment extends BaseFragment {
    private static final String TAG = TrainingFragment.class.getSimpleName();

    private static TrainingFragment sInstance;
    private ViewPager mPager;
    private TabLayout mTabLayout;
    private TrainingPagerAdapter mAdapter;
    private TrainingViewModel mViewModel;

    public static TrainingFragment getInstance() {
        if (sInstance == null) {
            sInstance = new TrainingFragment();
        }

        return sInstance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setBarTitle(getString(R.string.title_training));
        View v = inflater.inflate(R.layout.fragment_training, container, false);
        setupPager(v);

        return v;
    }

    private void setupPager(View v) {
        mPager = v.findViewById(R.id.viewpager);
        mTabLayout = v.findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mPager);
        mAdapter = new TrainingPagerAdapter(getActivity());
        mPager.setAdapter(mAdapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        injectViewModel();

    }

    private void injectViewModel() {
        TrainingViewModelFactory factory = InjectorUtils.provideTrainingViewModelFactory(getActivity());
        mViewModel = ViewModelProviders.of(this, factory).get(TrainingViewModel.class);
//        mViewModel.getTrainingList().observe(this, trainingEntities -> {
//            mAdapter.swapList(trainingEntities);
//            if (trainingEntities != null && trainingEntities.size() != 0) {
//                closeLoadingDialog();
//            } else {
//                showLoadingDialog();
//                new Handler().postDelayed(()-> {
//                    if (getActivity() != null) {
//                        Toast.makeText(getActivity(), R.string.get_data_failed_message, Toast.LENGTH_LONG).show();
//                        closeLoadingDialog();
//                    }
//
//                }, 1000 * 5);
//            }
//        });
        mAdapter.swapList(mViewModel.getLocalTrainingList());
    }

    class TrainingPagerAdapter extends PagerAdapter {

        private List<ImageEntity> mList;
        private Context mContext;

        public TrainingPagerAdapter(Context context) {
            mContext = context;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            ViewGroup view = (ViewGroup) inflater.inflate(R.layout.training_pager_layout, container,false);
            ImageEntity entity = mList.get(position);
            PhotoView imgView = view.findViewById(R.id.photo_view);
            Glide.with(mContext)
                    .load(entity.getResourceId())
                    .into(imgView);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return mList != null ? mList.size() : 0;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            if (mList != null) {
                return mList.get(position).getTitle();
            }

            return super.getPageTitle(position);
        }

        public void swapList(final List<ImageEntity> newList) {
            mList = newList;
            notifyDataSetChanged();
        }
    }
}