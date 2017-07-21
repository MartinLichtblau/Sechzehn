package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.activities;

import  android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.ncapdevi.fragnav.FragNavController;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Resource;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.BaseFragment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.OwnerFragment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.FriendsFragment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.SearchFragment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.SzUtils;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.viewModels.OwnerViewModel;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;

@RuntimePermissions
public class BottomTabsActivity extends AppCompatActivity implements BaseFragment.NavController, FragNavController.TransactionListener, FragNavController.RootFragmentListener {
    //Better convention to properly realname the indices what they are in your app
    private final int INDEX_SEARCH = FragNavController.TAB1;
    private final int INDEX_FRIENDS = FragNavController.TAB2;
    private final int INDEX_OWNER = FragNavController.TAB3;
    private BottomBar mBottomBar;
    public FragNavController mNavController;
    public static OwnerViewModel ownerVM;
    public MutableLiveData<Integer> allOk = new MutableLiveData<>();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        //Accessed two times on first ever start of app: 1. to login 2.forwarded from loginfragment after succesfull login
        super.onCreate(savedInstanceState);
        String token = getSharedPreferences("Sechzehn",0).getString("JWT","");
        String ownername = getSharedPreferences("Sechzehn",0).getString("ownername","");
        if(TextUtils.isEmpty(token) || TextUtils.isEmpty(ownername)){
            Intent intent=new Intent(this,LoginActivity.class);
            startActivity(intent);
            finish(); //Finish does not stop the code here, for that you have to add return!!!!!!!!!!!
            return;
        }else{
            openApp(savedInstanceState);
        }
    }

    private void openApp(final Bundle savedInstanceState){
        SzUtils.initialize(getSharedPreferences("Sechzehn",0));
        ownerVM = ViewModelProviders.of(BottomTabsActivity.this).get(OwnerViewModel.class);
        setContentView(de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R.layout.activity_bottom_tabs);
        mBottomBar = (BottomBar) findViewById(R.id.bottomBar);
        checkRequirements().observeForever(new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer sum) {
                if(sum == 3){
                    mBottomBar.selectTabAtPosition(INDEX_SEARCH);
                    mNavController = FragNavController.newBuilder(savedInstanceState, getSupportFragmentManager(), R.id.container)
                            .transactionListener(BottomTabsActivity.this)
                            .rootFragmentListener(BottomTabsActivity.this, 3)
                            .build();
                    mBottomBar.setOnTabSelectListener(new OnTabSelectListener() {
                        @Override
                        public void onTabSelected(@IdRes int tabId) {
                            switch (tabId) {
                                case R.id.bb_menu_search:
                                    mNavController.switchTab(INDEX_SEARCH);
                                    break;
                                case R.id.bb_menu_friends:
                                    mNavController.switchTab(INDEX_FRIENDS);
                                    break;
                                case R.id.bb_menu_owner:
                                    mNavController.switchTab(INDEX_OWNER);
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
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public MutableLiveData<Integer> checkRequirements(){
        allOk.setValue(0);
        //1. Check Play Services are available
        if(checkPlayServices())
            allOk.setValue(allOk.getValue()+1);

        //2. Check permissions
        BottomTabsActivityPermissionsDispatcher.startSechzehnWithCheck(BottomTabsActivity.this);

        //3. Check server connection
        checkConnection();
        return allOk;
    }

    public void checkConnection(){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();
        ownerVM.initOwner(SzUtils.getOwnername()).observeForever(new Observer<Resource>() {
            @Override
            public void onChanged(@Nullable Resource resource) {
                if (resource.status == Resource.Status.LOADING){
                    progressDialog.setMessage("LOADING....");
                }else if (resource.status == Resource.Status.ERROR) {
                    progressDialog.setMessage("ERROR: " + resource.message);
                    new Handler().postDelayed(new Runnable() { //Test every 3 second again
                        public void run() {
                            ownerVM.initOwner(SzUtils.getOwnername());
                        }
                    }, 3000);
                } else if (resource.status == Resource.Status.SUCCESS) {
                    progressDialog.hide();
                    allOk.setValue(allOk.getValue()+1);
                }
            }
        });
    }

    public void factoryReset(){
        getSharedPreferences("Sechzehn", 0).edit().clear().apply();
        Intent intent = new Intent(this, LoginActivity.class);
        ownerVM = null;
        startActivity(intent);
        finish(); //Finish BottomTabs
    }

    @NeedsPermission({
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION})
    public void startSechzehn() {
        allOk.setValue(allOk.getValue()+1);
    }

    @OnShowRationale({
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION})
    public void showRationaleForAllPermissions(final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setMessage("To experience Sechzeh you must grant some permissions :-)")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton("Leave", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.cancel();
                        finish();
                    }
                })
                .setCancelable(false) // Ref > https://stackoverflow.com/questions/12102777/prevent-android-activity-dialog-from-closing-on-outside-touch
                .show();
    }

    @OnPermissionDenied({
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION})
    public void showDeniedForAllPermissions() {
        BottomTabsActivityPermissionsDispatcher.startSechzehnWithCheck(BottomTabsActivity.this);
    }

    @OnNeverAskAgain({
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION})
    public void showNeverAskForAllPermissions() {
        new AlertDialog.Builder(this)
                .setMessage("You checked *Never ask again*" +
                        "\nSechZehn can only serve you if you undo that selection manually in the following dialog.")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Ref > https://stackoverflow.com/questions/41256582/how-to-handle-never-ask-again-checkbox-in-android-m
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null); //Ref > https://stackoverflow.com/questions/6589797/how-to-get-package-name-from-anywhere
                        intent.setData(uri);
                        startActivityForResult(intent, 666);
                    }
                })
                .setNegativeButton("Leave", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        BottomTabsActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    public boolean checkPlayServices() {
        //Ref. > https://stackoverflow.com/questions/42005217/detecting-play-services-installed-and-play-services-used-in-app
        final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int resultCode = api.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (api.isUserResolvableError(resultCode))
                api.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            else {
                Toast.makeText(this, "This device is not supported, since it does not have Google Play Services installed.", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    public static OwnerViewModel getOwnerViewModel() {
        //Use OwnerViewModel to access all data and functions concerning the owner
        //It in here and not in BaseFragment since this MainActivity is the one and only element that we can access always
        //also from Fragments and so not related to BaseFragment
        return ownerVM;
    }


        //-------------------------------------Frag Nav Code------->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    public FragNavController getNavController(){
        return mNavController;
    }

    @Override
    public void onBackPressed() {
        if (mNavController.isRootFragment()) { //Bottom of fragment stack is reached
            //go back to home tab fragment
            switchTabAndBar(2);
        } else {
            mNavController.popFragment();
        }
    }

    //To switch tab fragment, and at the same time, select the according tab in bottom bar
    public void switchTabAndBar(int tabId){
        mBottomBar.selectTabAtPosition(tabId);
        mNavController.switchTab(tabId);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mNavController != null) {
            mNavController.onSaveInstanceState(outState);
        }
    }

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
        // If we have a backstack, show the back button @TODO perhaps we should show the title bar with back button. What do you think?
        if (getSupportActionBar() != null && mNavController != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(!mNavController.isRootFragment());
        }
    }

    @Override
    public Fragment getRootFragment(int index) {
        switch (index) {
            case INDEX_SEARCH:
                return SearchFragment.newInstance();
            case INDEX_FRIENDS:
                return FriendsFragment.newInstance();
            case INDEX_OWNER:
                return OwnerFragment.newInstance();         //In Background gets owners data from server
        }
        throw new IllegalStateException("Need to send an index that we know");
    }

}



