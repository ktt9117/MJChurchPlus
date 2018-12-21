package org.mukdongjeil.mjchurch.ui;

import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.ui.introduce.IntroduceFragment;
import org.mukdongjeil.mjchurch.ui.sermons.SermonFragment;
import org.mukdongjeil.mjchurch.ui.training.TrainingFragment;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private DrawerLayout mDrawerLayout;
    private ProgressBar mLoadingIndicator;

    private boolean mExitConfirmFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLoadingIndicator = findViewById(R.id.pb_loading_indicator);
        setupToolbar();
        setupNavigationDrawer();
        setDefaultFragment();
    }

    private void setDefaultFragment() {
        Fragment fragment = SermonFragment.getInstance();
        switchContent(fragment);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
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

        if (mExitConfirmFlag) {
            super.onBackPressed();

        } else {
            mExitConfirmFlag = true;
            Toast.makeText(getApplicationContext(),
                    R.string.application_quit_message, Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(()-> mExitConfirmFlag = false, 2000);
        }
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

    private int mLastSelectedMenuItemId = -1;

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener((menuItem)-> {
            if (mLastSelectedMenuItemId != -1) {
                MenuItem lastSelectedMenuItem = navigationView.getMenu().findItem(mLastSelectedMenuItemId);
                if (lastSelectedMenuItem != null) {
                    lastSelectedMenuItem.setChecked(false);
                }
            } else if (mLastSelectedMenuItemId == menuItem.getItemId()) {
                mDrawerLayout.closeDrawers();
                return true;
            }

            switch (menuItem.getItemId()) {
                case R.id.sermon_menu_item:
                    switchContent(SermonFragment.getInstance());
                    break;

                case R.id.welcome_menu_item:
                    switchContent(IntroduceFragment.getInstance());
                    break;

                case R.id.training_menu_item:
                    switchContent(TrainingFragment.getInstance());
                    break;

                default:
                    break;
            }

            menuItem.setChecked(true);
            mLastSelectedMenuItemId = menuItem.getItemId();
            mDrawerLayout.closeDrawers();
            return true;
        });
    }

    private void switchContent(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment)
                .commit();
    }

    public void setTitleText(String text) {
        ActionBar ab = getSupportActionBar();
        ab.setTitle(text);
    }

    public void showLoadingDialog() {
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    public void hideLoadingDialog() {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
    }

    public boolean isLoadingDialogShowing() {
        return mLoadingIndicator.getVisibility() == View.VISIBLE;
    }
}
