package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.ServiceGenerator;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services.UserService;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.GenericBody;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.SzUtils;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//Ref. > https://github.com/nickfox/GpsTracker/blame/master/phoneClients/android/app/src/main/java/com/websmithing/gpstracker/LocationService.java
public class LocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = "LocationService";
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private boolean currentlyProcessingLocation = false;
    public static Location previousBestLocation = null;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    private static String token;
    private static String ownername;
    private static UserService userService;

    @Override
    public void onCreate() {
        //Is called multiple times again by Android System, after destroy, and runs also when main activity destroyed
        Log.d(TAG, "onCreate");
        super.onCreate();
        if (TextUtils.isEmpty(SzUtils.getOwnername()))
            SzUtils.initialize(getApplicationContext());
        ownername = SzUtils.getOwnername();
        token = SzUtils.getToken();
        userService = ServiceGenerator.createService(UserService.class, token);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Is called multiple times again by Android System, after destroy, and runs also when main activity destroyed
        Log.d(TAG, "onStartCommand");
        if (!currentlyProcessingLocation) {
            // if we are currently trying to get a location and the alarm manager has called this again,
            // no need to start processing a new location.
            //If we do it would crash....
            currentlyProcessingLocation = true;
            startTracking();
        }
        return START_STICKY;
    }

    private void startTracking() {
        Log.d(TAG, "startTracking");
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        if (!googleApiClient.isConnected() || !googleApiClient.isConnecting()) {
            googleApiClient.connect();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void stopLocationUpdates() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    /**
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(TWO_MINUTES); // milliseconds
        locationRequest.setFastestInterval(15 * 1000); // you will get updates faster if e.g. another app requests location before your interval
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        //Since our app assures that at all times all permissions are granted it's ok like this
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, this);
        //Fixes Bug: App does not start if location is not changed.
        // This is also needed to start the app in the emulator.
        previousBestLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed");
        stopLocationUpdates();
        stopSelf();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "GoogleApiClient connection has been suspend");
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        //Ref. > https://stackoverflow.com/a/14478281/3965610
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the senderUser has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged");
        //Update only if new location is better and moved more than 100 meters
        if (isBetterLocation(location, previousBestLocation)) {
            if (previousBestLocation == null) {
                updateLocation(location);
            } else if (previousBestLocation.getLatitude() != 0.0d && previousBestLocation.getLongitude() != 0.0d) {
                if (previousBestLocation.distanceTo(location) > 25)
                    updateLocation(location);
            }
        }
    }

    protected void updateLocation(Location location) {
        Log.d(TAG, "updateLocation() | ownername: " + ownername + " position: " + location.getLatitude() + ", " + location.getLongitude() + " accuracy: " + location.getAccuracy());
        previousBestLocation = location;
        RequestBody body = new GenericBody()
                .put("lat", String.valueOf(location.getLatitude()))
                .put("lng", String.valueOf(location.getLongitude())).generate();
        userService.updateLocation(ownername, body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    //Toast.makeText(LocationService.this, "Location successfully updated", Toast.LENGTH_SHORT).show();
                } else {
                    //Toast.makeText(LocationService.this, NetworkUtils.parseError(response).getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                //Toast.makeText(LocationService.this, "Error: "+t.getCause(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static Location getPreviousBestLocation() {
        return previousBestLocation;
    }
}