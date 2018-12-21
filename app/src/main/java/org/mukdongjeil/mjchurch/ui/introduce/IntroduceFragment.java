package org.mukdongjeil.mjchurch.ui.introduce;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.tabs.TabLayout;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.data.database.entity.IntroduceEntity;
import org.mukdongjeil.mjchurch.ui.BaseFragment;
import org.mukdongjeil.mjchurch.util.InjectorUtils;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class IntroduceFragment extends BaseFragment {
    private static final String TAG = IntroduceFragment.class.getSimpleName();

    private static IntroduceFragment sInstance;
    private ViewPager mPager;
    private TabLayout mTabLayout;
    private IntroducePagerAdapter mAdapter;
    private IntroduceViewModel mViewModel;

    public static IntroduceFragment getInstance() {
        if (sInstance == null) {
            sInstance = new IntroduceFragment();
        }

        return sInstance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setBarTitle(getString(R.string.title_welcome));

        View v = inflater.inflate(R.layout.fragment_introduce, container, false);
        setupPager(v);

        return v;
    }

    private void setupPager(View v) {
        mPager = v.findViewById(R.id.viewpager);
        mTabLayout = v.findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mPager);
        mAdapter = new IntroducePagerAdapter(getActivity());
        mPager.setAdapter(mAdapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        injectViewModel();

    }

    private void injectViewModel() {
        IntroduceViewModelFactory factory = InjectorUtils.provideIntroduceViewModelFactory(getActivity());
        mViewModel = ViewModelProviders.of(this, factory).get(IntroduceViewModel.class);
        mViewModel.getIntroduceList().observe(this, introduceEntities -> {
            mAdapter.swapList(introduceEntities);
            if (introduceEntities != null && introduceEntities.size() != 0) {
                closeLoadingDialog();
            } else {
                showLoadingDialog();
                new Handler().postDelayed(()-> {
                    Toast.makeText(getActivity(), R.string.get_data_failed_message, Toast.LENGTH_LONG).show();
                    closeLoadingDialog();
                }, 1000 * 10);
            }
        });
    }

    class IntroducePagerAdapter extends PagerAdapter {

        private List<IntroduceEntity> mIntroduceList;
        private Context mContext;

        public IntroducePagerAdapter(Context context) {
            mContext = context;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            ViewGroup view = (ViewGroup) inflater.inflate(R.layout.introduce_pager_layout, container,false);
            IntroduceEntity entity = mIntroduceList.get(position);
            PhotoView imgView = view.findViewById(R.id.photo_view);
            Glide.with(mContext)
                    .load(entity.getContentUri())
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
            return mIntroduceList != null ? mIntroduceList.size() : 0;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            if (mIntroduceList != null) {
                return mIntroduceList.get(position).getTitle();
            }

            return super.getPageTitle(position);
        }

        public void swapList(final List<IntroduceEntity> newList) {
            mIntroduceList = newList;
            notifyDataSetChanged();
        }
    }
}