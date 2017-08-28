package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.viewModels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;

import java.util.HashMap;
import java.util.Map;

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
    public MutableLiveData<HashMap<Marker,MarkerOptions>> usersOnMap = new MutableLiveData<>();
    public MutableLiveData<Resource> venueResults = new MutableLiveData<>();
    public HashMap<Marker,MarkerOptions> venuesOnMap = new HashMap<>();
    public MutableLiveData<VenueSearch> lastVS = new MutableLiveData<>();


    public Boolean lastStateSaved = false;
    public GoogleMap map;
    private CameraPosition cameraPosition;
    public Boolean userToggle = true;
    public Boolean venueToggle = true;


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

/*    public void searchXVenuesNearby(Integer numberVenues, Double lat, Double lng, Double radius){
        getVenues(null, numberVenues, lat, lng, radius, null, null, null, null, null);
    }*/

    public void newSearchSectionHere (String section) {
        LatLng center = map.getCameraPosition().target;
        Double radius = getVisibleRadius();
        VenueSearch vs = lastVS.getValue();
        vs.setSection(Venue.Section.valueOf(section));
        vs.setLat(center.latitude); vs.setLng(center.longitude); vs.setRadius(radius);
        getVenues(vs);
    }

    public void alterLastVenueSearch(Integer page, Integer perPage, Double lat, Double lng, Double radius, String section, String query, Integer price, String time, Boolean sortByDistance){
        if(null != query){

        }
        if(null != section){

        }


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

    public void createAddVenueMarkers(HashMap<Venue, Bitmap> venueIconMap) {
        HashMap<Marker, MarkerOptions> tempMarkerMap = new HashMap<>();
        for (Map.Entry<Venue, Bitmap> e : venueIconMap.entrySet()) {
            Venue venue = e.getKey();
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(new LatLng(venue.lat, venue.lng))
                    .title(venue.name)
                    .snippet("View Venue Rating: " + venue.rating)
                    .infoWindowAnchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromBitmap(e.getValue()));
            Marker marker = map.addMarker(markerOptions);
            marker.setTag(venue);
            tempMarkerMap.put(marker, markerOptions);
        }
        venuesOnMap = tempMarkerMap;
    }

    public void reAddUserMarkersOnMap(HashMap<Marker,MarkerOptions> markerMap){
        //Add markers through markerOptions and save them
        usersOnMap.setValue(reAddMarkersOnMap(markerMap));
    }

    public void reAddVenueMarkersOnMap(HashMap<Marker,MarkerOptions> markerMap){
        //Add markers through markerOptions and save them
        venuesOnMap = reAddMarkersOnMap(markerMap);
    }

    public HashMap<Marker,MarkerOptions> reAddMarkersOnMap(HashMap<Marker,MarkerOptions> markerMap){
        if(markerMap == null || markerMap.isEmpty())
            return null;
        HashMap<Marker,MarkerOptions> markersOnMap = new HashMap<>();
        for (Map.Entry<Marker, MarkerOptions> entry : markerMap.entrySet()) {
            Marker marker = map.addMarker(entry.getValue());
            marker.setTag(entry.getKey().getTag()); //Retrieve saved Object (User or Venue) in Tag
            markersOnMap.put(marker, entry.getValue());
        }
        return markersOnMap;
    }

    public void toggleUsers(View view){
        if(userToggle){
            userToggle = false;
            hideMarkersOnMap(usersOnMap.getValue());
        } else{
            userToggle = true;
            showMarkersOnMap(usersOnMap.getValue());
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

    private void showMarkersOnMap(HashMap<Marker,MarkerOptions> markersOnMap) {
        if(markersOnMap == null || markersOnMap.isEmpty())
            return;
        for (Map.Entry<Marker, MarkerOptions> entry : markersOnMap.entrySet()) {
            entry.getKey().setVisible(true); //Set Marker visibility
            entry.getValue().visible(true); //persist visibility also in markeroptions
        }
    }

    private void hideMarkersOnMap(HashMap<Marker,MarkerOptions> markersOnMap) {
        if(markersOnMap == null || markersOnMap.isEmpty())
            return;
        for (Map.Entry<Marker, MarkerOptions> entry : markersOnMap.entrySet()) {
            entry.getKey().setVisible(false);
            entry.getValue().visible(false);
        }
    }

    public void removeMarkersOnMap(HashMap<Marker,MarkerOptions> markersOnMap) {
        if(markersOnMap == null || markersOnMap.isEmpty())
            return;
        for (Map.Entry<Marker, MarkerOptions> entry : markersOnMap.entrySet()) {
            entry.getKey().remove();
        }
    }

    public void removeAllUsers(){
        removeMarkersOnMap(usersOnMap.getValue());
    }

    public void removeAllVenues(){
        removeMarkersOnMap(venuesOnMap);
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
        reAddUserMarkersOnMap(usersOnMap.getValue());
        //restore Venue Data
        reAddVenueMarkersOnMap(venuesOnMap);
        //restore Camera
        map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
}
