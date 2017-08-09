package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;

import android.app.ProgressDialog;
import android.arch.lifecycle.LiveData;
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
    private FragmentSearchBinding binding;
    private SearchViewModel searchVM;
    private OwnerViewModel ownerVM;
    private SupportMapFragment mapFragment;
    public GoogleMap map;

    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        searchVM = ViewModelProviders.of(this).get(SearchViewModel.class);
        ownerVM = BottomTabsActivity.getOwnerViewModel();

        observeSearchResults();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false);
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
        //Important or map may crash app
        getChildFragmentManager().beginTransaction()
                .remove(mapFragment)
                .commit();
        map = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMyLocationEnabled(true);
        map.setOnInfoWindowClickListener(this);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(ownerVM.getLatLng(), 10));

        if(null == searchVM.usersOnMap.getValue()){
            initalSearch(); //Initialize anew
        }else{
            showUsersOnMap(searchVM.usersOnMap.getValue()); //show last state
        }
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
                        showUsers(usersPage.data);
                        break;
                }
            }
        });

        //Observe Venue search result changes
    }

    public void showUsers(List<User> userList){
        drawUsersOnMap(userList);
        /*showUsersOnList(userList);*/
    }

    private void drawUsersOnMap(List<User> userList) {
        final HashMap<Marker,MarkerOptions> usersOnMap = new HashMap<>();
        for (final User user :  userList) {
            if (!TextUtils.equals(user.getUsername(), ownerVM.getOwnername())) {
                //Filter our owner
                SzUtils.createThumb(SzUtils.ThumbType.USER, user.getProfilePicture()).observe(this, new Observer<Bitmap>() {
                    @Override
                    public void onChanged(@Nullable Bitmap bitmap) {
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(new LatLng(user.getLat(), user.getLng()))
                                .title(user.getUsername())
                                .snippet("Open Profile")
                                .infoWindowAnchor(0.5f, 0.5f)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitmap));
                        Marker marker = map.addMarker(markerOptions);
                        usersOnMap.put(marker, markerOptions);
                    }
                });
            }
        }
        searchVM.usersOnMap.setValue(usersOnMap);
    }

    public Double getVisibleRange(GoogleMap map){
        VisibleRegion visibleRegion = map.getProjection().getVisibleRegion();
        return SphericalUtil.computeDistanceBetween(
                visibleRegion.farLeft, map.getCameraPosition().target) / 1000;
    }

    private void showUsersOnMap(HashMap<Marker,MarkerOptions> userMap) {
        for(Map.Entry<Marker,MarkerOptions> entry : userMap.entrySet()){
            map.addMarker(entry.getValue());
        }
    }

    private void hideUsersOnMap(HashMap<Marker,MarkerOptions> userMap) {
        for(Map.Entry<Marker,MarkerOptions> entry : userMap.entrySet()){
            entry.getKey().remove();
        }
    }
}

