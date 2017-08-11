package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;

import android.app.ProgressDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.maps.android.SphericalUtil;
import com.ncapdevi.fragnav.FragNavController;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.activities.BottomTabsActivity;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Pagination;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Resource;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.FragmentSearchBinding;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.SzUtils;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.viewModels.OwnerViewModel;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.viewModels.SearchViewModel;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

import static android.databinding.DataBindingUtil.inflate;

public class SearchFragment extends BaseFragment implements GoogleMap.OnInfoWindowClickListener, OnMapReadyCallback {
    private final String TAG = "SearchFragment";
    private FragmentSearchBinding binding;
    private SearchViewModel searchVM;
    private OwnerViewModel ownerVM;
    private MapView mapView;
    public GoogleMap map;
    public Boolean userToggle;

    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        searchVM = ViewModelProviders.of(this).get(SearchViewModel.class);
        ownerVM = BottomTabsActivity.getOwnerViewModel();
        observeSearchResults();
        userToggle = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false);
        binding.setFrag(this);
        mapView = binding.mapview;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        return binding.getRoot();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMyLocationEnabled(true);
        map.setOnInfoWindowClickListener(this);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(ownerVM.getLatLng(), 12));
        if(null == searchVM.usersOnMap.getValue()){
            initalSearch(); //Initialize anew
        }else{
            restoreLastState(); //show last state
        }
    }

    private void initalSearch(){
        //Show only nearby users and venues
        searchVM.searchXUsersNearby(100, ownerVM.getLatLng().latitude, ownerVM.getLatLng().longitude, getVisibleRange(map));
        /*searchVM.searchXVenuesNearby(...*/
    }

    public void observeSearchResults(){
        searchVM.searchResultUsers.observe(this, new Observer<Resource>() {
            @Override
            public void onChanged(@Nullable Resource resource) {
                switch (resource.status){
                    case LOADING:
                        Toast.makeText(getContext(), "Loading....", Toast.LENGTH_SHORT).show();
                        break;
                    case ERROR:
                        Toast.makeText(getContext(), "Error: " + resource.message, Toast.LENGTH_SHORT).show();
                        break;
                    case SUCCESS:
                        Pagination<User> usersPage = (Pagination<User>) resource.data;
                        Toast.makeText(getContext(), "Found"+usersPage.total+" users", Toast.LENGTH_SHORT).show();
                        addUsers(usersPage.data);
                        break;
                }
            }
        });
        //Observe Venue search result changes
    }

    public void addUsers(List<User> userList){
        //1.create MarkerOptions
        createUserMarkerOptions(userList).observe(this, new Observer<List<MarkerOptions>>() {
            @Override
            public void onChanged(@Nullable List<MarkerOptions> markerOptionsList) {
                //2. Add Markers on Map
                addUserMarkersOptionsOnMap(markerOptionsList);
            }
        });
        /*showUsersOnList(userList);*/
    }

    private MutableLiveData<List<MarkerOptions>> createUserMarkerOptions(final List<User> userList) {
        final MutableLiveData<List<MarkerOptions>> liveMarkerOList = new MutableLiveData<>();
        final List<MarkerOptions> tempMarkerOList= new ArrayList<>();
        for (final User user :  userList) {
            if (!TextUtils.equals(user.getUsername(), SzUtils.getOwnername())) {
                SzUtils.createThumb(SzUtils.ThumbType.USER, user.getProfilePicture()).observe(this, new Observer<Bitmap>() {
                    @Override
                    public void onChanged(@Nullable Bitmap bitmap) {
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(new LatLng(user.getLat(), user.getLng()))
                                .title(user.getUsername())
                                .snippet("Open Profile")
                                .visible(userToggle) //adheres to usertoggle visibility
                                .infoWindowAnchor(0.5f, 0.5f)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitmap));
                        tempMarkerOList.add(markerOptions);
                        if (tempMarkerOList.size() >= (userList.size() - 1))
                            liveMarkerOList.setValue(tempMarkerOList);
                    }
                });
            }
        }
        return liveMarkerOList;
    }

    public HashMap<Marker,MarkerOptions> addUserMarkersOptionsOnMap(List<MarkerOptions> markerOptionsList){
        HashMap<Marker,MarkerOptions> markersOnMap = new HashMap<>();
        for (MarkerOptions mo :  markerOptionsList) {
            Marker marker = map.addMarker(mo);
            markersOnMap.put(marker, mo);
        }
        //3. and save them
        searchVM.usersOnMap.setValue(markersOnMap);
        return markersOnMap;
    }

    public Double getVisibleRange(GoogleMap map){
        VisibleRegion visibleRegion = map.getProjection().getVisibleRegion();
        return SphericalUtil.computeDistanceBetween(
                visibleRegion.farLeft, map.getCameraPosition().target) / 1000;
    }

    public void toggleUsers(View view){
        if(userToggle)
            hideUsersOnMap();
        else
            showUsersOnMap();
    }

    private void showUsersOnMap() {
        userToggle = true;
        HashMap<Marker,MarkerOptions> markersOnMap = new HashMap<>();
        for (Map.Entry<Marker, MarkerOptions> entry : searchVM.usersOnMap.getValue().entrySet()) {
            entry.getKey().setVisible(true);
            entry.getValue().visible(true);
            markersOnMap.put(entry.getKey(),entry.getValue());
        }
        searchVM.usersOnMap.setValue(markersOnMap);
    }

    private void hideUsersOnMap() {
        userToggle = false;
        HashMap<Marker,MarkerOptions> markersOnMap = new HashMap<>();
        for (Map.Entry<Marker, MarkerOptions> entry : searchVM.usersOnMap.getValue().entrySet()) {
            entry.getKey().setVisible(false);
            entry.getValue().visible(false);
            markersOnMap.put(entry.getKey(),entry.getValue());
        }
        searchVM.usersOnMap.setValue(markersOnMap);
    }

    public void saveCurrentState(){
        Toast.makeText(getActivity(), "saveCurrentState", Toast.LENGTH_SHORT).show();
    }

    public void restoreLastState(){
        Toast.makeText(getActivity(), "restoreLastState", Toast.LENGTH_SHORT).show();
        List<MarkerOptions> markerOptionsList = new ArrayList<>(searchVM.usersOnMap.getValue().values());
        addUserMarkersOptionsOnMap(markerOptionsList);
    }

    @Override
    public void onInfoWindowClick(final Marker marker) {
        final String username = marker.getTitle();
        new Handler().postDelayed(new Runnable() {
            //Maps Bug UI Hang while replacing fragment
            // Ref. > http://www.javacms.tech/questions/1113754/ui-hang-while-replacing-fragment-from-setoninfowindowclicklistener-interface-met
            @Override
            public void run() {
                //@TODO differ between users and venues
                fragNavController().pushFragment(UserProfileFragment.newInstance(username));
            }
        }, 100);
    }

    //>>>>>>>>>>>>Forward Lifecycle for googlemaps MapView

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        if (mapView != null) {
            saveCurrentState();
            mapView.onPause();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mapView != null) {
            try {
                mapView.onDestroy();
            } catch (NullPointerException e) {
                Log.e(TAG, "Error while attempting MapView.onDestroy(), ignoring exception", e);
            }
        }
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
    }
}


