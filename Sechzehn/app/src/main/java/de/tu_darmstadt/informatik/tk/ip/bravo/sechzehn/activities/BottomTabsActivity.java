package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.activities;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.ncapdevi.fragnav.FragNavController;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.BaseFragment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.SettingsFragment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.FriendsFragment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.VenuesFragment;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;

public class BottomTabsActivity extends AppCompatActivity implements BaseFragment.FragmentNavigation, FragNavController.TransactionListener, FragNavController.RootFragmentListener {
    //Better convention to properly name the indices what they are in your app
    private final int INDEX_VENUES = FragNavController.TAB1;
    private final int INDEX_FRIENDS = FragNavController.TAB2;
    private final int INDEX_SETTINGS = FragNavController.TAB3;
    private BottomBar mBottomBar;
    private FragNavController mNavController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R.layout.activity_bottom_tabs);

        mBottomBar = (BottomBar) findViewById(R.id.bottomBar);
        mBottomBar.selectTabAtPosition(INDEX_VENUES);
        mNavController = FragNavController.newBuilder(savedInstanceState, getSupportFragmentManager(), R.id.container)
                .transactionListener(this)
                .rootFragmentListener(this, 3)
                .build();


        mBottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                switch (tabId) {
                    case R.id.bb_menu_nearby:
                        mNavController.switchTab(INDEX_VENUES);
                        break;
                    case R.id.bb_menu_friends:
                        mNavController.switchTab(INDEX_FRIENDS);
                        break;
                    case R.id.bb_menu_favorites:
                        mNavController.switchTab(INDEX_SETTINGS);
                        break;
                }
            }
        });

        mBottomBar.setOnTabReselectListener(new OnTabReselectListener() {
            @Override
            public void onTabReSelected(@IdRes int tabId) {
                mNavController.clearStack();
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (!mNavController.isRootFragment()) {
            mNavController.popFragment();
        } else {
            mNavController.switchTab(INDEX_VENUES);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mNavController != null) {
            mNavController.onSaveInstanceState(outState);
        }
    }

    @Override
    public void pushFragment(Fragment fragment) {
        if (mNavController != null) {
            mNavController.pushFragment(fragment);
        }
    }

    @Override
    public void onTabTransaction(Fragment fragment, int index) {
        // If we have a backstack, show the back button
        if (getSupportActionBar() != null && mNavController != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(!mNavController.isRootFragment());
        }
    }


    @Override
    public void onFragmentTransaction(Fragment fragment, FragNavController.TransactionType transactionType) {
        //do fragmentty stuff. Maybe change title, I'm not going to tell you how to live your life
        // If we have a backstack, show the back button
        if (getSupportActionBar() != null && mNavController != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(!mNavController.isRootFragment());
        }
    }

    @Override
    public Fragment getRootFragment(int index) {
        switch (index) {
            case INDEX_VENUES:
                return VenuesFragment.newInstance(0);
            case INDEX_FRIENDS:
                return FriendsFragment.newInstance(0);
            case INDEX_SETTINGS:
                return SettingsFragment.newInstance(0);
        }
        throw new IllegalStateException("Need to send an index that we know");
    }


}



