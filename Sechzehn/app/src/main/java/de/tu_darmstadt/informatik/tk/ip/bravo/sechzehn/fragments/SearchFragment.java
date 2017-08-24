package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.widget.ToggleButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
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
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.VenueSearch;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.BottomsheetSearchBinding;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.FragmentSearchBinding;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.services.LocationService;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.SzUtils;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.viewModels.OwnerViewModel;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.viewModels.SearchViewModel;

public class SearchFragment extends BaseFragment {
    private final String TAG = "SearchFragment";
    private FragmentSearchBinding binding;
    private SearchViewModel searchVM;
    private OwnerViewModel ownerVM;
    private MapView mapView;

    LatLng userSetMapCenter;
    Float userSetMapZoom;
    Boolean userMovedCamera = false;
    Float bsCollapsed;
    Float bsExpanded;
    BottomSheetBehavior bottomSheetBehavior;

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
        //binding.setActiveSearch(new VenueSearch()); //@TODO remove since not needed when using Livedata
        searchVM.lastVS.setValue(new VenueSearch());

        mapView = binding.mapView;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                setupMap(googleMap);
            }
        });
        setupBottomSheet();
        setupSearchbarViews();

        return binding.getRoot();
    }

    private void setupSearchbarViews(){
        final SearchView searchviewVenue = binding.bottomsheetSearch.searchviewVenue;
        searchviewVenue.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() == 0) {
                    alterSearchQuery(null);
                    searchviewVenue.clearFocus();
                }
                return false;
            }
            @Override
            public boolean onQueryTextSubmit(String query) {
                alterSearchQuery(query);
                return false;
            }
        });

        // Catch event on [x] button inside search view
        int searchCloseButtonId = searchviewVenue.getContext().getResources()
                .getIdentifier("android:id/search_close_btn", null, null);
        ImageView closeButton = (ImageView) searchviewVenue.findViewById(searchCloseButtonId);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchviewVenue.setQuery(null,true);
                alterSearchQuery(null);
            }
        });

    }

/*    private void hideKeyBoard(){
        // Check if no view has focus:

        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }*/

    public void alterSearchQuery(String query){
        Toast.makeText(getActivity(), "alterSearchQuery: "+query, Toast.LENGTH_SHORT).show();
        VenueSearch alteredVS = searchVM.lastVS.getValue();
        alteredVS.setQuery(query);
        searchVM.getVenues(alteredVS);
    }

    public void alterSearchSection(View view) {
        String section = null;
        VenueSearch alteredVS = searchVM.lastVS.getValue();
        ToggleButton toggleButton = (ToggleButton) view;
        if(toggleButton.isChecked())
            section = view.getTag().toString();
        alteredVS.setSection(section);
        searchVM.getVenues(alteredVS);
    }

    public void alterSearchPrice(View view){
        Integer price = Integer.valueOf(view.getTag().toString());
        VenueSearch alteredVS = searchVM.lastVS.getValue();
        if(price == 0) //price 0 is Any price > means price is null
            price = null;
        alteredVS.setPrice(price);
        searchVM.getVenues(alteredVS);
    }

    public void alterSearchOpennow(View view){
        Boolean opennow = binding.bottomsheetSearch.opennow.isChecked();
        String nowDate = null;
        VenueSearch alteredVS = searchVM.lastVS.getValue();
        Toast.makeText(getActivity(), "alterSearchOpennow: "+opennow.toString(), Toast.LENGTH_SHORT).show();
        if(opennow)
            nowDate = SzUtils.getNowDate("yyyy-MM-dd hh:mm");
        alteredVS.setTime(nowDate);
        searchVM.getVenues(alteredVS);
    }

    private void setupBottomSheet(){
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomsheetSearch.getRoot());
        bsCollapsed = getActivity().getResources().getDimension(R.dimen.search_bottomsheet_collapsed);
        bsExpanded = getActivity().getResources().getDimension(R.dimen.search_bottomsheet_expanded);

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            CameraUpdate cu;
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        setMapPaddingBotttom(1f);
                        cu = CameraUpdateFactory.zoomTo(userSetMapZoom - 1.1f);
                        searchVM.map.animateCamera(cu, 500, null);
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        setMapPaddingBotttom(0f);
                        cu = CameraUpdateFactory.zoomTo(userSetMapZoom);
                        searchVM.map.animateCamera(cu, 500, null);
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                switch (bottomSheetBehavior.getState()) {
                    case BottomSheetBehavior.STATE_DRAGGING:
                        setMapPaddingBotttom(slideOffset);
                        searchVM.map.moveCamera(CameraUpdateFactory.newLatLng(userSetMapCenter));
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        setMapPaddingBotttom(slideOffset);
                        searchVM.map.moveCamera(CameraUpdateFactory.newLatLng(userSetMapCenter));
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        break;
                }
            }
        });

        /*AppBarLayout appBarLayout = binding.bottomsheetSearch.searchBottomsheetAppbarlayout;
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if(verticalOffset == 0)
                    Toast.makeText(getContext(), "onOffsetChanged", Toast.LENGTH_SHORT).show();
            }
        });*/

        updateActiveSearchHint();
    }

    private void updateActiveSearchHint(){
        final BottomsheetSearchBinding bss = binding.bottomsheetSearch;
        //observe the currenlty active Venue Search to dynamically adapt the active_search_layout
        searchVM.lastVS.observe(this, new Observer<VenueSearch>() {
            @Override
            public void onChanged(@Nullable VenueSearch vs) {
                Toast.makeText(getActivity(), "venueSearch onChanged: ", Toast.LENGTH_SHORT).show();

                //Active Query
                if(vs.getQuery() != null){
                    bss.activeQuery.setText(vs.getQuery());
                    bss.activeQuery.setChecked(true);
                }else{
                    bss.activeQuery.setText("");
                    bss.activeQuery.setChecked(false);
                    bss.activeQuery.setClickable(false);
                }

                //Active Section
                String selectedSection = vs.getSection();
                if(! TextUtils.isEmpty(selectedSection)){
                    ToggleButton selectedToggleButton = (ToggleButton) getView().findViewWithTag(selectedSection);
                    Drawable relatedDrawable = selectedToggleButton.getButtonDrawable();
                    bss.activeSection.setButtonDrawable(relatedDrawable);
                    bss.activeSection.setChecked(true);
                }else{
                    bss.activeSection.setChecked(false);
                    bss.activeSection.setButtonDrawable(null);
                }

                //Active Price
                if(vs.getPrice() != null){
                    // create a string made up of n copies of string s
                    String s = "$";
                    Integer n = vs.price;
                    bss.activePrice.setText(String.format("%0" + n + "d", 0).replace("0",s));
                    bss.activePrice.setChecked(true);
                }else{
                    bss.activePrice.setChecked(false);
                    bss.activePrice.setText("");
                }

                //Active Opennow
                if(null != vs.getTime()){
                    bss.activeOpennow.setChecked(true);
                }else{
                    bss.activeOpennow.setChecked(false);
                }



            }
        });
    }

    public void setupMap(GoogleMap googleMap){
        searchVM.map = googleMap;
        searchVM.map.setMyLocationEnabled(true);
        searchVM.map.getUiSettings().setMapToolbarEnabled(false);
        searchVM.map.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.search_map_style));

        if(searchVM.lastStateSaved){
            searchVM.restoreLastState(); //show last state
        }else{
            searchVM.map.moveCamera(CameraUpdateFactory.newLatLngZoom(ownerVM.getLatLng(), 12));
            userSetMapCenter = searchVM.map.getCameraPosition().target;
            userSetMapZoom = searchVM.map.getCameraPosition().zoom;
            initalSearch(); //Initialize anew
        }
        setupMapListeners();
    }

    private void setupMapListeners(){
        searchVM.map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
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
        });
        searchVM.map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                BottomSheetBehavior.from(binding.bottomsheetSearch.getRoot()).setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
        searchVM.map.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                //Ref.: https://developers.google.com/maps/documentation/android-api/events
                if(REASON_GESTURE == i){
                    userMovedCamera = true;
                    if(bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED){
                        //User should not scroll when the BottomSheet is expanded and map is super tiny small
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                }
            }
        });
        searchVM.map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                if(userMovedCamera ){
                    userSetMapCenter = searchVM.map.getCameraPosition().target;
                    userSetMapZoom = searchVM.map.getCameraPosition().zoom;
                    userMovedCamera = false;
                    new Handler().postDelayed(new Runnable() {
                        //Maps Bug UI Hang while replacing fragment
                        // Ref. > http://www.javacms.tech/questions/1113754/ui-hang-while-replacing-fragment-from-setoninfowindowclicklistener-interface-met
                        @Override
                        public void run() {
                            binding.searchAgainHere.setVisibility(View.VISIBLE);
                        }
                    }, 1000);
                }
            }
        });
        searchVM.map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                Location loc = LocationService.getPreviousBestLocation();
                userSetMapCenter = new LatLng(loc.getLatitude(),loc.getLongitude());
                return false;
            }
        });
    }

    private void setMapPaddingBotttom(Float offset){
        //From 0.0 (min) - 1.0 (max)
        Float maxMapPaddingBottom = bsExpanded - bsCollapsed;
        //left,top,right,bottom
        searchVM.map.setPadding(0, 0, 0, Math.round(offset * maxMapPaddingBottom));
    }

    public void initalSearch(){
        //Show only nearby users and venues
        searchVM.searchXUsersNearby(50, ownerVM.getLatLng().latitude, ownerVM.getLatLng().longitude, searchVM.getVisibleRadius());
        //searchVM.searchXVenuesNearby(50, ownerVM.getLatLng().latitude, ownerVM.getLatLng().longitude, searchVM.getVisibleRadius());
        /*searchVM.instantSearchVenues("coffee");*/

        binding.bottomsheetSearch.sectionCoffee.performClick();
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
                        binding.searchAgainHere.setVisibility(View.INVISIBLE);
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

    public void fab(View view){
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
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


