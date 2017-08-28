package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.viewModels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.graphics.Bitmap;
import android.support.design.widget.BottomSheetBehavior;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.MarkerMarkerOptions;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Pagination;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Resource;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Venue;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.VenueSearch;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.ServiceGenerator;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services.UserService;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services.VenueService;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.NetworkUtils;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.SzUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by marti on 19.07.2017.
 */

public class SearchViewModel extends ViewModel{
    private final String TAG = "SearchViewModel";
    private static final UserService userService = ServiceGenerator.createService(UserService.class,SzUtils.getToken());
    private static final VenueService venueService = ServiceGenerator.createService(VenueService.class,SzUtils.getToken());

    public MutableLiveData<Resource> userResults = new MutableLiveData<>();
    public ArrayList<MarkerMarkerOptions> usersOnMap = new ArrayList<>();
    public MutableLiveData<Resource> venueResults = new MutableLiveData<>();
    public ArrayList<MarkerMarkerOptions> venuesOnMap = new ArrayList<>();
    public MutableLiveData<VenueSearch> lastVS = new MutableLiveData<>();

    public Boolean lastStateSaved = false;
    public GoogleMap map;
    private CameraPosition cameraPosition;
    public Float lastSetZoom = 12f; //Default is 12
    public Boolean userToggle = true;
    public Boolean venueToggle = true;
    public Marker selectedMarker;
    public Integer lastBssState = BottomSheetBehavior.STATE_COLLAPSED;

    public void searchXUsersNearby(Integer numberUsers, Double lat, Double lng, Double radius){
        getUsers(null, numberUsers, lat, lng,radius,null,null);
    }

    public void getUsers(Integer page, Integer perPage, Double lat, Double lng, Double radius, Boolean isFriend, String searchedUsername){
        userResults.setValue(Resource.loading(null));
        userService.getUsers(page, perPage, lat, lng, radius, isFriend, searchedUsername)
                .enqueue(new Callback<Pagination<User>>(){
            @Override
            public void onResponse(Call<Pagination<User>> call, Response<Pagination<User>> response) {
                if (response.isSuccessful())
                    userResults.setValue(Resource.success(response.body(),null));
                else
                    userResults.setValue(Resource.error(NetworkUtils.parseError(response).getMessage(), null));
            }
            @Override
            public void onFailure(Call<Pagination<User>> call, Throwable t) {
                if(null != t.getCause())
                    userResults.setValue(Resource.error(t.getCause().toString(),null));
            }
        });
    }


    public void getVenues (VenueSearch vs) {
        venueResults.setValue(Resource.loading(null));

        if(vs.getSearchHere()){
            LatLng center = map.getCameraPosition().target;
            Double radius = getVisibleRadius();
            vs.setLat(center.latitude); vs.setLng(center.longitude); vs.setRadius(radius);
        }

        venueService.getVenues(vs.page, vs.perPage, vs.lat, vs.lng, vs.radius, vs.section, vs.query, vs.price, vs.time) //@TODO include sortByDistance
                .enqueue(new Callback<Pagination<Venue>>() {
            @Override
            public void onResponse(Call<Pagination<Venue>> call, Response<Pagination<Venue>> response) {
                if (response.isSuccessful())
                    venueResults.setValue(Resource.success(response.body(), null));
                else
                    venueResults.setValue(Resource.error(NetworkUtils.parseError(response).getMessage(), null));
            }
            @Override
            public void onFailure(Call<Pagination<Venue>> call, Throwable t) {
                if (null != t.getCause())
                    venueResults.setValue(Resource.error(t.getCause().toString(), null));
            }
        });
        lastVS.setValue(vs);
    }

    public void searchHere(View view){
        LatLng center = map.getCameraPosition().target;
        Double radius = getVisibleRadius();
        VenueSearch vs = lastVS.getValue();
        vs.setLat(center.latitude); vs.setLng(center.longitude); vs.setRadius(radius);
        getVenues(vs);
    }

    public void createAddVenueMarkers(LinkedHashMap<Venue, Bitmap> venueIconMap) {
        Integer pos = 0;
        for (Map.Entry<Venue, Bitmap> e : venueIconMap.entrySet()) {
            Venue venue = e.getKey();
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(new LatLng(venue.lat, venue.lng))
                    .title(venue.name)
                    .flat(true) //To save performance
                    .zIndex((float) (venueIconMap.size() - pos)) //Top list items should show on top
                    .snippet("View Venue Rating: " + (venue.rating <= 0.0d ? "-.-" : venue.rating))
                    .infoWindowAnchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromBitmap(e.getValue()));
            Marker marker = map.addMarker(markerOptions);
            marker.setTag(pos++);
            venuesOnMap.add(new MarkerMarkerOptions(marker, markerOptions));
        }
    }

    public void reAddUserMarkersOnMap(ArrayList<MarkerMarkerOptions> userMmoList){
        //Add markers through markerOptions and save them
        usersOnMap = reAddMarkersOnMap(userMmoList);
    }

    public void reAddVenueMarkersOnMap(ArrayList<MarkerMarkerOptions> venueMmoList){
        //Add markers through markerOptions and save them
        venuesOnMap = reAddMarkersOnMap(venueMmoList);
    }

    public ArrayList<MarkerMarkerOptions> reAddMarkersOnMap(ArrayList<MarkerMarkerOptions> mmoList){
        if(mmoList == null || mmoList.isEmpty())
            return null;
        ArrayList<MarkerMarkerOptions> newMmoList = new ArrayList<>();
        for (MarkerMarkerOptions oldMmo : mmoList) {
            Marker newMarker = map.addMarker(oldMmo.markerOptions);
            newMarker.setTag(oldMmo.marker.getTag()); //Retrieve saved Object (User or Venue) in Tag
            if(selectedMarker.equals(oldMmo.marker)) //This marker was selecte on SaveLastState
                newMarker.showInfoWindow();
            newMmoList.add(new MarkerMarkerOptions(newMarker, oldMmo.markerOptions));
        }
        return newMmoList;
    }

    public void toggleUsers(View view){
        if(userToggle){
            userToggle = false;
            hideMarkersOnMap(usersOnMap);
        } else{
            userToggle = true;
            showMarkersOnMap(usersOnMap);
        }
    }

    public void toggleVenues(View view){
        if(venueToggle){
            venueToggle = false;
            hideMarkersOnMap(venuesOnMap);
        } else{
            venueToggle = true;
            showMarkersOnMap(venuesOnMap);
        }
    }

    private void showMarkersOnMap(ArrayList<MarkerMarkerOptions> mmoList) {
        if(mmoList == null || mmoList.isEmpty())
            return;
        for (MarkerMarkerOptions mmo : mmoList) {
            mmo.marker.setVisible(true);
            mmo.markerOptions.visible(true);
        }
    }

    private void hideMarkersOnMap(ArrayList<MarkerMarkerOptions> mmoList) {
        if(mmoList == null || mmoList.isEmpty())
            return;
        for (MarkerMarkerOptions mmo : mmoList) {
            mmo.marker.setVisible(false);
            mmo.markerOptions.visible(false);
        }
    }

    public void removeMarkersOnMap(ArrayList<MarkerMarkerOptions> mmoList) {
        if(mmoList == null || mmoList.isEmpty())
            return;
        for (MarkerMarkerOptions mmo : mmoList) {
            mmo.marker.remove();
        }
    }

    public void removeAllUsersOnMap(){
        removeMarkersOnMap(usersOnMap);
        usersOnMap = new ArrayList<>();
    }

    public void removeAllVenuesOnMap(){
        removeMarkersOnMap(venuesOnMap);
        venuesOnMap = new ArrayList<>();
    }

    public Double getVisibleRadius(){
        //Ref.: https://stackoverflow.com/questions/20422701/retrieve-distance-from-visible-part-of-google-map
        LatLng center = map.getCameraPosition().target;
        LatLng northeast = map.getProjection().getVisibleRegion().latLngBounds.northeast;
        Double distKM;

        LatLng middleRight = new LatLng(center.latitude, northeast.longitude);
        LatLng topMiddle = new LatLng(northeast.latitude, center.longitude);
        Double distWidth = SphericalUtil.computeDistanceBetween(center, middleRight);
        Double distHeight = SphericalUtil.computeDistanceBetween(center, topMiddle);
        //To get smallest visible radius is in height or width
        if(distHeight < distWidth){
            Log.d(TAG," getVisibleRadius | distHeight");
            distKM = (distHeight / 1000) * 0.50;
        } else{
            Log.d(TAG," getVisibleRadius | distWidth");
            distKM = (distWidth / 1000) * 0.8;
        }
        return distKM;
    }

    public void saveCurrentState(){
        cameraPosition = map.getCameraPosition();
        lastStateSaved = true;
    }

    public void restoreLastState(){
        lastStateSaved = false;
        //restore User Data
        reAddUserMarkersOnMap(usersOnMap);
        //restore Venue Data
        reAddVenueMarkersOnMap(venuesOnMap);
        //restore Camera
        map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        CameraUpdate cu = CameraUpdateFactory.newCameraPosition(cameraPosition);
        map.moveCamera(cu);
    }
}
