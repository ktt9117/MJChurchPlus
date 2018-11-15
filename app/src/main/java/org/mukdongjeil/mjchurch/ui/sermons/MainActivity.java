package org.mukdongjeil.mjchurch.ui.sermons;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.material.navigation.NavigationView;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.ui.sermon_detail.SermonDetailActivity;
import org.mukdongjeil.mjchurch.util.InjectorUtils;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity implements SermonAdapter.SermonAdapterOnItemClickHandler {

    private static final String TAG = MainActivity.class.getSimpleName();

    private DrawerLayout mDrawerLayout;
    private ProgressBar mLoadingIndicator;

    private RecyclerView mRecyclerView;
    private SermonAdapter mSermonAdapter;
    private int mPosition = RecyclerView.NO_POSITION;

    private MainActivityViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        mLoadingIndicator = findViewById(R.id.pb_loading_indicator);
        setupToolbar();
        setupNavigationDrawer();
        setupRecyclerView();
        injectViewModel();
    }

    @Override
    public void onItemClick(int bbsNo) {
        Log.d(TAG, "onItemClick : " + bbsNo);
        Intent intent = new Intent(this, SermonDetailActivity.class);
        intent.putExtra(SermonDetailActivity.INTENT_KEY_BBS_NO, bbsNo);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Open the navigation drawer when the home icon is selected from the toolbar.
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return;
        }

        super.onBackPressed();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("주일설교");
    }

    private void setupNavigationDrawer() {
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(R.color.colorPrimaryDark);
        NavigationView navigationView = findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }
    }

    private void setupRecyclerView() {
        mRecyclerView = findViewById(R.id.recyclerview_sermon_list);
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mSermonAdapter = new SermonAdapter(this, this);
        mRecyclerView.setAdapter(mSermonAdapter);
    }

    private void injectViewModel() {
        MainViewModelFactory factory = InjectorUtils.provideMainActivityViewModelFactory(this.getApplicationContext());
        mViewModel = ViewModelProviders.of(this, factory).get(MainActivityViewModel.class);
        mViewModel.getSermonList().observe(this, sermonEntities -> {
            mSermonAdapter.swapList(sermonEntities);
            if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
            //mRecyclerView.smoothScrollToPosition(mPosition);

            if (sermonEntities != null && sermonEntities.size() != 0) showSermonDataView();
            else showLoading();
        });
    }

    private void showSermonDataView() {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showLoading() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener((menuItem)-> {
            switch (menuItem.getItemId()) {
//                        case R.id.list_worship_menu_item:
//                            // Do nothing, we're already on that screen
//                            break;
//                        case R.id.statistics_navigation_menu_item:
//                            Intent intent =
//                                    new Intent(TasksActivity.this, StatisticsActivity.class);
//                            startActivity(intent);
//                            break;
                default:
                    break;
            }
            // Close the navigation drawer when an item is selected.
            menuItem.setChecked(true);
            mDrawerLayout.closeDrawers();
            return true;
        });
    }
}
