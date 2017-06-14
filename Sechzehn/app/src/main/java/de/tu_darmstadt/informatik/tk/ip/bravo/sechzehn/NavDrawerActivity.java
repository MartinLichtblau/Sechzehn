package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.ncapdevi.fragnav.FragNavController;
import com.ncapdevi.fragnav.FragNavTransactionOptions;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.BaseFragment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.SettingsFragment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.FriendsFragment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.VenuesFragment;

import java.util.ArrayList;
import java.util.List;

public class NavDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, BaseFragment.FragmentNavigation {
    //Better convention to properly name the indices what they are in your app
    private final int INDEX_VENUES = FragNavController.TAB1;
    private final int INDEX_FRIENDS = FragNavController.TAB2;
    private final int INDEX_SETTINGS = FragNavController.TAB3;
    private FragNavController mNavController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);

        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        List<Fragment> fragments = new ArrayList<>(3);

        fragments.add(VenuesFragment.newInstance(0));
        fragments.add(FriendsFragment.newInstance(0));
        fragments.add(SettingsFragment.newInstance(0));


        mNavController =
                FragNavController.newBuilder(savedInstanceState, getSupportFragmentManager(), R.id.container)
                        .rootFragments(fragments)
                        .defaultTransactionOptions(FragNavTransactionOptions.newBuilder().customAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left, R.anim.slide_in_from_left, R.anim.slide_out_to_right).build())
                        .build();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (mNavController.getCurrentStack().size() > 1) {
            mNavController.popFragment();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mNavController.onSaveInstanceState(outState);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void pushFragment(Fragment fragment) {
        mNavController.pushFragment(fragment);
    }
}
