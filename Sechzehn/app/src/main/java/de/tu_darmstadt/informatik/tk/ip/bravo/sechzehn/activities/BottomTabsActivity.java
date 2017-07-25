package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.activities;

import  android.Manifest;
import android.app.ProgressDialog;
import android.arch.lifecycle.LifecycleActivity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
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
import android.util.Log;
import android.widget.Toast;




import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.ncapdevi.fragnav.FragNavController;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.AnimatedFragNavController;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Resource;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.BaseFragment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.OwnerFragment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.FriendsFragment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.SearchFragment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.services.LocationService;
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
public class BottomTabsActivity extends LifecycleActivity implements BaseFragment.NavController, FragNavController.TransactionListener, FragNavController.RootFragmentListener {
    //Better convention to properly realname the indices what they are in your app
    private final int INDEX_SEARCH = FragNavController.TAB1;
    private final int INDEX_FRIENDS = FragNavController.TAB2;
    private final int INDEX_OWNER = FragNavController.TAB3;
    private BottomBar mBottomBar;
    public AnimatedFragNavController mNavController;
    public static OwnerViewModel ownerVM;
    public MutableLiveData<Integer> checkStages = new MutableLiveData<>();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        //Accessed two times on first ever start of app: 1. to login 2.forwarded from loginfragment after succesfull
        super.onCreate(savedInstanceState);
        checkRequirements().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean requirementsOK) {
                if(requirementsOK){
                    runApp(savedInstanceState);
                }
            }
        });
    }

    private void runApp(Bundle savedInstanceState){
        setContentView(de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R.layout.activity_bottom_tabs);
        mBottomBar = (BottomBar) findViewById(R.id.bottomBar);
        mNavController = new AnimatedFragNavController(FragNavController.newBuilder(savedInstanceState, getSupportFragmentManager(), R.id.container)
                .transactionListener(this)
                .rootFragmentListener(this, 3)
                .build());
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

    public MutableLiveData<Boolean> checkRequirements(){
        final MutableLiveData<Boolean> requirementsOK = new MutableLiveData<>();
        requirementsOK.setValue(false);
        checkStages.setValue(0);

        checkStages.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer stage) {
                switch (stage){
                    case 0:
                        //0.Check if user is logged in
                        checkLoggedIn();
                        break;
                    case 1:
                        //1. Check if Play Services are available
                        checkPlayServices();
                        break;
                    case 2:
                        //2. Check permissions
                        BottomTabsActivityPermissionsDispatcher.checkPermissionsWithCheck(BottomTabsActivity.this);
                        break;
                    case 3:
                        //3. Start/Check LocationService, Initialize/Check owner profile and location
                        startService(new Intent(BottomTabsActivity.this, LocationService.class));
                        checkProfile();
                        break;
                    case 4:
                        requirementsOK.setValue(true);
                        break;
                    default:
                        Toast.makeText(BottomTabsActivity.this, "Fatal ERROR in checkRequirements — You should not see that —", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return requirementsOK;
    }

    public void checkLoggedIn() {
        SzUtils.initialize(getBaseContext());
        if(TextUtils.isEmpty(SzUtils.getToken()) || TextUtils.isEmpty(SzUtils.getOwnername())){
            factoryReset();
        }else{
            checkStages.setValue(checkStages.getValue()+1);
        }
    }

    public void checkPlayServices() {
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
            checkStages.setValue(checkStages.getValue());
        }
        checkStages.setValue(checkStages.getValue()+1);
    }

    public void checkProfile(){
        ownerVM = ViewModelProviders.of(this).get(OwnerViewModel.class);

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
                            BottomTabsActivity.getOwnerViewModel().initOwner(SzUtils.getOwnername());
                        }
                    }, 3000);
                } else if (resource.status == Resource.Status.SUCCESS) {
                    if(ownerVM.getLatLng() == null){
                        progressDialog.setMessage("Waiting for your GPS-Location");
                        new Handler().postDelayed(new Runnable() { //Test every 3 second again
                            public void run() {
                                BottomTabsActivity.getOwnerViewModel().initOwner(SzUtils.getOwnername());
                            }
                        }, 3000);
                    }else{
                        progressDialog.dismiss();
                        checkStages.setValue(checkStages.getValue()+1);
                    }
                }
            }
        });
    }

    @NeedsPermission({
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION})
    public void checkPermissions() {
        checkStages.setValue(checkStages.getValue()+1);
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
        BottomTabsActivityPermissionsDispatcher.checkPermissionsWithCheck(BottomTabsActivity.this);
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

    public static OwnerViewModel getOwnerViewModel() {
        //Use OwnerViewModel to access all data and functions concerning the owner
        //It in here and not in BaseFragment since this MainActivity is the one and only element that we can access always
        //also from Fragments and so not related to BaseFragment
        return ownerVM;
    }

    public void factoryReset(){
        getSharedPreferences("Sechzehn", 0).edit().clear().apply();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish(); //Finish BottomTabs
    }

    //-------------------------------------Frag Nav Code------->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    public AnimatedFragNavController getNavController(){
        return mNavController;
    }

    @Override
    public void onBackPressed() {
        if (mNavController.isRootFragment()) { //Bottom of fragment stack is reached
            //go back to home tab fragment
            switchTabAndBar(INDEX_SEARCH);
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
        if (getActionBar() != null && mNavController != null) {
            getActionBar().setDisplayHomeAsUpEnabled(!mNavController.isRootFragment());
        }
    }

    @Override
    public void onFragmentTransaction(Fragment fragment, FragNavController.TransactionType transactionType) {
        //do fragmentty stuff. Maybe change title, I'm not going to tell you how to live your life
        // If we have a backstack, show the back button @TODO perhaps we should show the title bar with back button. What do you think?
        if (getActionBar() != null && mNavController != null) {
            getActionBar().setDisplayHomeAsUpEnabled(!mNavController.isRootFragment());
        }
    }

    @Override
    public Fragment getRootFragment(int index) {
        switch (index) {
            case INDEX_SEARCH:
                return OwnerFragment.newInstance();         //In Background gets owners data from server
            case INDEX_FRIENDS:
                return FriendsFragment.newInstance();
            case INDEX_OWNER:
                return SearchFragment.newInstance();
        }
        throw new IllegalStateException("Need to send an index that we know");
    }

}