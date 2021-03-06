package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.activities;

import android.Manifest;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.arch.lifecycle.LifecycleActivity;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.ncapdevi.fragnav.FragNavController;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.AnimatedFragNavController;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Resource;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.BaseFragment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.FriendsFragment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.MessageFragment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.OwnerFragment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.SearchFragment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.services.ChatNotificationService;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.services.LocationService;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.SzUtils;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.viewModels.OwnerViewModel;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

/**
 * Our main activity. It checks for all prerequisites and displays the main UI.
 */
@RuntimePermissions
public class BottomTabsActivity extends LifecycleActivity implements BaseFragment.NavController, FragNavController.TransactionListener, FragNavController.RootFragmentListener {

    public static final int EXTRA_INTENT_SHOW_MESSAGE = 1;

    //Better convention to properly realname the indices what they are in your app
    private final int INDEX_SEARCH = FragNavController.TAB1;
    private final int INDEX_FRIENDS = FragNavController.TAB2;
    private final int INDEX_OWNER = FragNavController.TAB3;
    private BottomBar mBottomBar;
    public AnimatedFragNavController mNavController;
    public static OwnerViewModel ownerVM;
    public MutableLiveData<Integer> checkStages = new MutableLiveData<>();

    /**
     * Accessed two times on first ever start of app: 1. to login 2. forwarded from loginfragment
     * after successful.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(null); //don't do super.onCreate(savedInstanceState) or it will load e.g. searchFragment before fully checks and initialization
        checkRequirements().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean requirementsOK) {
                if (requirementsOK) {
                    runApp();
                }
            }
        });
    }

    /**
     * Animates setContentView.
     */
    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        LayoutInflater inflator = getLayoutInflater();
        View view = inflator.inflate(layoutResID, null, false);
        view.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        setContentView(view);
    }

    /**
     * Setup and start the main UI.
     */
    private void runApp() {
        setContentView(de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R.layout.activity_bottom_tabs);
        mBottomBar = (BottomBar) findViewById(R.id.bottomBar);
        mNavController = new AnimatedFragNavController(
                FragNavController.newBuilder(null, getSupportFragmentManager(), R.id.container)
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
        postOwnerToasts();
    }

    /**
     * Starts the check for requirements.
     *
     * @return A LiveDataObject which is updated if the status of the requirements changed.
     */

    public MutableLiveData<Boolean> checkRequirements() {
        final MutableLiveData<Boolean> requirementsOK = new MutableLiveData<>();
        requirementsOK.setValue(false);
        checkStages.setValue(0);

        checkStages.observeForever(new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer stage) {
                switch (stage) {
                    case 0:
                        //0.Check if senderUser is logged in
                        checkLoggedIn();
                        break;
                    case 1:
                        //1. Check if Play Services are available
                        checkPlayServices();
                        break;
                    case 2:
                        //2. Check permissions
                        Log.d("Has observer", String.valueOf(checkStages.hasActiveObservers()));
                        BottomTabsActivityPermissionsDispatcher.checkPermissionsWithCheck(BottomTabsActivity.this);
                        break;
                    case 3:
                        //3. Start/Check LocationService, Initialize/Check owner profile and location
                        startService(new Intent(BottomTabsActivity.this, LocationService.class));
                        startService(new Intent(BottomTabsActivity.this, ChatNotificationService.class));
                        checkProfile();
                        break;
                    case 4:
                        requirementsOK.setValue(true);
                        checkStages.removeObserver(this);
                        break;
                    default:
                        Toast.makeText(BottomTabsActivity.this, "Fatal ERROR in checkRequirements — You should not see that —", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return requirementsOK;
    }

    /**
     * Check if user is logged in, else forward to {@link LoginActivity}.
     */
    public void checkLoggedIn() {
        SzUtils.initialize(this);
        if (TextUtils.isEmpty(SzUtils.getToken()) || TextUtils.isEmpty(SzUtils.getOwnername())) {
            Intent intent = new Intent(this, LoginActivity.class);
            this.finish();
            startActivity(intent);
        } else {
            checkStages.setValue(checkStages.getValue() + 1);
        }
    }

    /**
     * Check for installed Google Play Services
     */
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
            new Handler().postDelayed(new Runnable() { //Try again in 5 second again
                public void run() {
                    checkStages.setValue(checkStages.getValue());
                }
            }, 5000);
        } else {
            checkStages.setValue(checkStages.getValue() + 1);
        }
    }

    /**
     * Check if the UserProfile is loaded and a GPS position can be obtained.
     */
    public void checkProfile() {
        ownerVM = ViewModelProviders.of(this).get(OwnerViewModel.class);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();
        ownerVM.initOwner(SzUtils.getOwnername()).observe(this, new Observer<Resource>() {
            @Override
            public void onChanged(@Nullable Resource resource) {
                if (resource.status == Resource.Status.LOADING) {
                    progressDialog.setMessage("LOADING....");
                } else if (resource.status == Resource.Status.ERROR) {
                    progressDialog.setMessage("ERROR: " + resource.message);
                    new Handler().postDelayed(new Runnable() { //Test every 3 second again
                        public void run() {
                            BottomTabsActivity.getOwnerViewModel().initOwner(SzUtils.getOwnername());
                        }
                    }, 3000);
                } else if (resource.status == Resource.Status.SUCCESS) {
                    //received owner/user object: check GPS
                    if (isGPSOk()) {
                        progressDialog.dismiss();
                        checkStages.setValue(checkStages.getValue() + 1);
                    } else {
                        progressDialog.setMessage("Waiting for your GPS-Location");
                    }
                }
            }
        });
    }

    /**
     * @return true if a GPS position can be obtained, else false.
     */
    private Boolean isGPSOk() {
        Location localLoc = LocationService.getPreviousBestLocation();
        LatLng remoteLatLng = ownerVM.getLatLng();
/*        Toast.makeText(this, "LocalLoc: "+localLoc.getLatitude()+" "+localLoc.getLatitude()+
                "RemoteLoc: "+remoteLoc.latitude+" "+remoteLoc.longitude, Toast.LENGTH_SHORT).show();*/
        //Toast.makeText(this, "isGPSOk", Toast.LENGTH_SHORT).show();

        if (null != localLoc && null != remoteLatLng) {
            Double distInMeter = SphericalUtil.computeDistanceBetween(new LatLng(localLoc.getLatitude(), localLoc.getLongitude()), remoteLatLng);
            if (distInMeter < 100)
                return true;
        }
        //else try again
        new Handler().postDelayed(new Runnable() { //Test every 3 second again
            public void run() {
                //try every 3seconds again till isGPSOk() == true
                BottomTabsActivity.getOwnerViewModel().initOwner(SzUtils.getOwnername());
            }
        }, 3000);
        return false;
    }

    /**
     * Checks for User permissons.
     */
    @NeedsPermission({
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION})
    public void checkPermissions() {
        checkStages.setValue(checkStages.getValue() + 1);
        Log.d("Has observer", String.valueOf(checkStages.hasActiveObservers()));
    }

    /**
     * Shows the reason why the permission is needed.
     *
     * @param request The permisson request.
     */
    @OnShowRationale({
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION})
    public void showRationaleForAllPermissions(final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setMessage("To experience Sechzehn you must grant some permissions :-)")
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

    /**
     *
     */
    @OnPermissionDenied({
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION})
    public void showDeniedForAllPermissions() {
        BottomTabsActivityPermissionsDispatcher.checkPermissionsWithCheck(BottomTabsActivity.this);
    }

    /**
     * Shows a message if user checked Never ask again.
     */
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

    /**
     * Listener for permission request results.
     * Delegated to {@link BottomTabsActivityPermissionsDispatcher}.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        BottomTabsActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    /**
     * Use OwnerViewModel to access all data and functions concerning the owner
     * It in here and not in BaseFragment since this MainActivity is the one and only element that
     * we can access always
     * also from Fragments and so not related to BaseFragment
     *
     * @return the owner view model.
     */
    public static OwnerViewModel getOwnerViewModel() {
        return ownerVM;
    }

    /**
     * Does a factory reset of our app by cleaning the user data.
     */
    public void factoryReset() {
        //Awesome new function > clearApplicationUserData > https://developer.android.com/reference/android/app/ActivityManager.html
        ((ActivityManager) getSystemService(ACTIVITY_SERVICE))
                .clearApplicationUserData();
    }

    /**
     * Enables toasts from the owner view model.
     */
    private void postOwnerToasts() {
        ownerVM.receiveToast().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                Toast.makeText(BottomTabsActivity.this, s, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Handles new Intents. Here the ContentIntent of our notifications.
     *
     * @param intent The new Intent.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getIntExtra(Intent.EXTRA_INTENT, 0) == EXTRA_INTENT_SHOW_MESSAGE) {
            mNavController.pushFragment(MessageFragment.newInstance(intent.getStringExtra(Intent.EXTRA_USER)));
        }
    }

    //-------------------------------------Frag Nav Code------->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    /**
     * @return the FragNavController which is used for all navigation between fragments in this
     * activity.
     */
    public AnimatedFragNavController getNavController() {
        return mNavController;
    }

    /**
     * Handles back button press. Delegates to FragNavController
     */
    @Override
    public void onBackPressed() {
        if (mNavController.isRootFragment()) { //Bottom of fragment stack is reached
            //go back to home tab fragment
            switchTabAndBar(INDEX_SEARCH);
        } else {
            mNavController.popFragment();
        }
    }

    /**
     * Switch tab fragment, and at the same time, select the according tab in bottom bar
     *
     * @param tabId the tabID to be selected.
     */
    public void switchTabAndBar(int tabId) {
        mBottomBar.selectTabAtPosition(tabId);
        mNavController.switchTab(tabId);
    }

    /**
     * Restores SaveInstanceState
     *
     * @param outState the state.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mNavController != null) {
            mNavController.onSaveInstanceState(outState);
        }
    }

    /**
     * Push a new fragment to the Stack.
     *
     * @param fragment the new fragment.
     */
    @Deprecated
    public void pushFragment(Fragment fragment) {
        if (mNavController != null) {
            mNavController.pushFragment(fragment);
        }
    }

    /**
     * On tab transaction listener
     */
    @Override
    public void onTabTransaction(Fragment fragment, int index) {
        // If we have a backstack, show the back button
        if (getActionBar() != null && mNavController != null) {
            getActionBar().setDisplayHomeAsUpEnabled(!mNavController.isRootFragment());
        }
    }

    /**
     * On fragment transaction listener
     */
    @Override
    public void onFragmentTransaction(Fragment fragment, FragNavController.TransactionType transactionType) {
        //do fragmentty stuff. Maybe change title, I'm not going to tell you how to live your life
        // If we have a backstack, show the back button @TODO perhaps we should show the title bar with back button. What do you think?
        if (getActionBar() != null && mNavController != null) {
            getActionBar().setDisplayHomeAsUpEnabled(!mNavController.isRootFragment());
        }
    }

    /**
     * Gets the root fragment of the tab.
     *
     * @param index index of the tab.
     * @return the corresponding root fragment.
     */
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