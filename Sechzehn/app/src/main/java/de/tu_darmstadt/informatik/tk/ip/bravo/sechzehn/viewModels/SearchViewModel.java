package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.viewModels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.maps.android.SphericalUtil;

import java.util.HashMap;
import java.util.Map;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Pagination;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Resource;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Venue;
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
    public MutableLiveData<HashMap<Marker,MarkerOptions>> venuesOnMap = new MutableLiveData<>();


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

    public void searchXVenuesNearby(Integer numberVenues, Double lat, Double lng, Double radius){
        getVenues(null, numberVenues, lat, lng, radius, null, null,null,null);
    }

    public void instantSearchVenues(String section) {
        LatLng latLng = map.getCameraPosition().target;
        Double radius = getVisibleRadius();
        getVenues(null, null, latLng.latitude, latLng.longitude, radius, section, null,null,null);
    }
    public void instantSearchVenuesMock(View view) {
        String section = view.getTag().toString();
        instantSearchVenues(section);
    }

    public void getVenues (Integer page, Integer perPage, Double lat, Double lng, Double radius, String section, String query, Integer price, String time) {
        //the section for quick searches Example: food. Possible values:  food , drinks , coffee , shops , arts , outdoors , sights .
        //the price ranking Example: 1. Possible values:  1 , 2 , 3 , 4 , 5 .
        //time Example: 2017-08-17 15:20.
        if(null == perPage)
            perPage = 50;
        venueResults.setValue(Resource.loading(null));
        venueService.getVenues(page, perPage, lat, lng, radius, section, query, price, time).enqueue(new Callback<Pagination<Venue>>() {
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
    }

    public void reAddUserMarkersOnMap(HashMap<Marker,MarkerOptions> markerMap){
        //Add markers through markerOptions and save them
        usersOnMap.setValue(reAddMarkersOnMap(markerMap));
    }

    public void reAddVenueMarkersOnMap(HashMap<Marker,MarkerOptions> markerMap){
        //Add markers through markerOptions and save them
        venuesOnMap.setValue(reAddMarkersOnMap(markerMap));
    }

    public HashMap<Marker,MarkerOptions> reAddMarkersOnMap(HashMap<Marker,MarkerOptions> markerMap){
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
            hideMarkersOnMap(venuesOnMap.getValue());
        } else{
            venueToggle = true;
            showMarkersOnMap(venuesOnMap.getValue());
        }
    }

    private void showMarkersOnMap(HashMap<Marker,MarkerOptions> markersOnMap) {
        for (Map.Entry<Marker, MarkerOptions> entry : markersOnMap.entrySet()) {
            entry.getKey().setVisible(true); //Set Marker visibility
            entry.getValue().visible(true); //persist visibility also in markeroptions
        }
    }

    private void hideMarkersOnMap(HashMap<Marker,MarkerOptions> markersOnMap) {
        for (Map.Entry<Marker, MarkerOptions> entry : markersOnMap.entrySet()) {
            entry.getKey().setVisible(false);
            entry.getValue().visible(false);
        }
    }

    public void removeMarkersOnMap(HashMap<Marker,MarkerOptions> markersOnMap) {
        for (Map.Entry<Marker, MarkerOptions> entry : markersOnMap.entrySet()) {
            entry.getKey().remove();
        }
    }

    public void removeAllUsers(){
        if(null != usersOnMap.getValue())
            removeMarkersOnMap(usersOnMap.getValue());
    }

    public void removeAllVenues(){
        if(null != venuesOnMap.getValue())
            removeMarkersOnMap(venuesOnMap.getValue());
    }

    public Double getVisibleRadius(){
        //in Kilometer
        VisibleRegion visibleRegion = map.getProjection().getVisibleRegion();
        return SphericalUtil.computeDistanceBetween(
                visibleRegion.nearLeft, map.getCameraPosition().target) / 1000;
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
        reAddVenueMarkersOnMap(venuesOnMap.getValue());
        //restore Camera
        map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
}
