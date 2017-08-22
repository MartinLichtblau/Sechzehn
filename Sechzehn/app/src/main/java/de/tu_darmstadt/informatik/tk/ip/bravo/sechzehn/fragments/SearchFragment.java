package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.activities.BottomTabsActivity;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Friendship;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Pagination;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Resource;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Venue;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.FragmentSearchBinding;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.SzUtils;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.viewModels.OwnerViewModel;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.viewModels.SearchViewModel;

public class SearchFragment extends BaseFragment implements GoogleMap.OnInfoWindowClickListener, OnMapReadyCallback, GoogleMap.OnMapClickListener {
    private final String TAG = "SearchFragment";
    private FragmentSearchBinding binding;
    private SearchViewModel searchVM;
    private OwnerViewModel ownerVM;
    private MapView mapView;

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
        binding.setFrag(this);
        binding.setSearchVM(searchVM);

        mapView = binding.mapView;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomsheetSearch.getRoot());
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            CameraPosition cameraPos;
            LatLng oldPos;
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        //does not exist , hideable =false
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        binding.searchFab.setVisibility(View.INVISIBLE);
                        oldPos = searchVM.map.getCameraPosition().target;
                        searchVM.map.setPadding(0,0,0, Math.round(
                                getActivity().getResources().getDimension(R.dimen.search_bottomsheet_expanded)
                                - getActivity().getResources().getDimension(R.dimen.search_bottomsheet_collapsed)));
                        /*searchVM.map.animateCamera(CameraUpdateFactory.zoomTo(11f), 1000, null);*/
                        cameraPos = new CameraPosition.Builder()
                                .target(oldPos)
                                .zoom(10)
                                .build();
                        searchVM.map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        binding.searchFab.setVisibility(View.VISIBLE);
                        oldPos = searchVM.map.getCameraPosition().target;
                        searchVM.map.setPadding(0,0,0,0);
                        /*searchVM.map.animateCamera(CameraUpdateFactory.zoomTo(11f), 1000, null);*/
                        cameraPos = new CameraPosition.Builder()
                                .target(oldPos)
                                .zoom(12)
                                .build();
                        searchVM.map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
  /*              Double dif;
                if(slideOffset > 0)
                    dif = 0.0003;
                else
                    dif = -0.0003;

                //mapView.setPadding(0,0,0, 100);
                LatLng pos = searchVM.map.getCameraPosition().target;
                LatLng newPos = new LatLng((pos.latitude - dif), pos.longitude);
                CameraPosition cameraPos = new CameraPosition.Builder().target(newPos)
                        .zoom(searchVM.map.getCameraPosition().zoom)
                        .build();
                searchVM.map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPos));*/
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        searchVM.map = googleMap;
        searchVM.map.setMyLocationEnabled(true);
        searchVM.map.setOnInfoWindowClickListener(this);
        searchVM.map.setOnMapClickListener(this);
        searchVM.map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        getContext(), R.raw.search_map_style));

        if(searchVM.lastStateSaved){
            searchVM.restoreLastState(); //show last state
        }else{
            searchVM.map.moveCamera(CameraUpdateFactory.newLatLngZoom(ownerVM.getLatLng(), 12));
            initalSearch(); //Initialize anew
        }
    }

    public void initalSearch(){
        //Show only nearby users and venues
        searchVM.searchXUsersNearby(50, ownerVM.getLatLng().latitude, ownerVM.getLatLng().longitude, searchVM.getVisibleRadius());
        //searchVM.searchXVenuesNearby(50, ownerVM.getLatLng().latitude, ownerVM.getLatLng().longitude, searchVM.getVisibleRadius());
        /*searchVM.instantSearchVenues("coffee");*/
        binding.bottomsheetSearch.toggleCoffee.performClick();
    }

    public void observeSearchResults(){
        searchVM.userResults.observe(this, new Observer<Resource>() {
            @Override
            public void onChanged(@Nullable Resource resource) {
                switch (resource.status){
                    case LOADING:
                        //Toast.makeText(getContext(), "Loading....", Toast.LENGTH_SHORT).show();
                        break;
                    case ERROR:
                        Toast.makeText(getContext(), "Error: " + resource.message, Toast.LENGTH_SHORT).show();
                        break;
                    case SUCCESS:
                        Pagination<User> userPagination = (Pagination<User>) resource.data;
                        searchVM.removeAllUsers();
                        addUsers(userPagination.data);
                        break;
                }
            }
        });
        searchVM.venueResults.observe(this, new Observer<Resource>() {
            @Override
            public void onChanged(@Nullable Resource resource) {
                switch (resource.status){
                    case LOADING:
                        Toast.makeText(getContext(), "Loading....", Toast.LENGTH_SHORT).show();
                        //@TODO show loading dialog progress bar
                        break;
                    case ERROR:
                        Toast.makeText(getContext(), "Error: " + resource.message, Toast.LENGTH_SHORT).show();
                        break;
                    case SUCCESS:
                        Pagination<Venue> venuePagination = (Pagination<Venue>) resource.data;
                        searchVM.removeAllVenues();
                        addVenues(venuePagination.data);
                        break;
                }
            }
        });
    }

    public void addUsers(List<User> userList){
        createAddUserMarkers(userList);
    }

    public void addVenues(List<Venue> venueList){
        createAddVenueMarkers(venueList);
        //showVenuesOnList(venueList); @TODO @Alexander
    }

    private void createAddUserMarkers(final List<User> userList) {
        final HashMap<Marker,MarkerOptions> tempMarkerMap = new HashMap<>();
        Boolean highlight = false;
        for (final User user :  userList) {
            if (!TextUtils.equals(user.getUsername(), SzUtils.getOwnername())) {
                if(user.getFriendshipStatus() == Friendship.Status.CONFIRMED)
                    highlight = true;
                SzUtils.createUserPin(getContext(), highlight, user.getProfilePicture()).observe(this, new Observer<Bitmap>() {
                    @Override
                    public void onChanged(@Nullable Bitmap bitmap) {
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(new LatLng(user.getLat(), user.getLng()))
                                .title(user.getUsername())
                                .snippet("Open Profile")
                                .infoWindowAnchor(0.5f, 0.5f)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitmap));
                        Marker marker = searchVM.map.addMarker(markerOptions);
                        marker.setTag(user);
                        tempMarkerMap.put(marker, markerOptions);
                        if (tempMarkerMap.size() >= (userList.size() - 1))
                            searchVM.usersOnMap.setValue(tempMarkerMap);
                    }
                });
            }
        }
    }

    private void createAddVenueMarkers(final List<Venue> venueList) {
        final HashMap<Marker,MarkerOptions> tempMarkerMap = new HashMap<>();
        for (final Venue venue :  venueList) {
                SzUtils.createVenuePin(getContext(), false, "placeholder url to picture").observe(this, new Observer<Bitmap>() {
                    @Override
                    public void onChanged(@Nullable Bitmap bitmap) {
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(new LatLng(venue.getLat(), venue.getLng()))
                                .title(venue.getName())
                                .snippet("View Venue")
                                .infoWindowAnchor(0.5f, 0.5f)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitmap));
                        Marker marker = searchVM.map.addMarker(markerOptions);
                        marker.setTag(venue);
                        tempMarkerMap.put(marker, markerOptions);
                        if (tempMarkerMap.size() >= (venueList.size()))
                            searchVM.venuesOnMap.setValue(tempMarkerMap);
                    }
                });
        }
    }

    @Override
    public void onInfoWindowClick(final Marker marker) {
        if(marker.getTag().getClass().equals(User.class)) {
            new Handler().postDelayed(new Runnable() {
                //Maps Bug UI Hang while replacing fragment
                // Ref. > http://www.javacms.tech/questions/1113754/ui-hang-while-replacing-fragment-from-setoninfowindowclicklistener-interface-met
                @Override
                public void run() {
                    fragNavController().pushFragment(UserProfileFragment.newInstance(((User) marker.getTag()).getUsername()));
                }
            }, 100);
        }else {
            //@TODO open detailed venue Fragment
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        BottomSheetBehavior.from(binding.bottomsheetSearch.getRoot()).setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    public void fab(View view){
        BottomSheetBehavior.from(binding.bottomsheetSearch.getRoot()).setState(BottomSheetBehavior.STATE_EXPANDED);
        //focusSearchView();
    }

    private void focusSearchView(){
       /* final SearchView searchView = binding.searchSearchbarView;
        searchView.setIconifiedByDefault(false);
        searchView.requestFocus();
        ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).
                toggleSoftInput(InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY);*/
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
            searchVM.saveCurrentState();
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


