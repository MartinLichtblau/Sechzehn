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
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.widget.ToggleButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
    BottomsheetSearchBinding bss;
    BottomSheetBehavior bottomSheetBehavior;

    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        searchVM = ViewModelProviders.of(this).get(SearchViewModel.class);
        ownerVM = BottomTabsActivity.getOwnerViewModel();

        //Most IMPORTANT NOTE: Observer method must be called only once or multiple instances of the observer functions observe the same
        //This is why they must be put in onCreate!! because here they only get called once onCreate (and this is as long as the existing Observer functions exists)
        //@TODO check code where I fell for this tricky time dimension trap
        observeSearchResults();
        observeVenueSearch();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false);
        binding.setFrag(this);
        binding.setSearchVM(searchVM);

        mapView = binding.mapView;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                setupMap(googleMap);
            }
        });

        //binding.setActiveSearch(new VenueSearch()); //@TODO remove since not needed when using Livedata
        //Initialize first venueSearch object for proper functioning of UI(altough the initialSearch() will come shortly after)
        setupBottomSheet();
        //setupSearchbarViews();  now in on Resume because of Bug

        return binding.getRoot();
    }

    private void setupBottomSheet(){
        Log.d(TAG,"setupBottomSheet");
        bss = binding.bottomsheetSearch;
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
    }

    private void setupSearchbarViews(){
        Log.d(TAG,"setupSearchbarViews");
        final SearchView searchviewVenue = binding.bottomsheetSearch.detailedQuery;
        searchviewVenue.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG,"onQueryTextChange");
                if (newText.length() == 0) {
                    alterSearchQuery(searchviewVenue);
                    searchviewVenue.clearFocus();
                }
                return false;
            }
            @Override
            public boolean onQueryTextSubmit(String query) {
                alterSearchQuery(searchviewVenue);
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
                //alterSearchQuery(null); //Now that I know that's redundant: onQueryTextChange gets called also when (x) button clears text
            }
        });

    }

    private void updateActiveSearchHint(VenueSearch vs){
        Log.d(TAG,"updateActiveSearchHint");

/*        //Active Query
        if(vs.getQuery() != null){
            bss.activeQuery.setText(vs.getQuery());
            bss.activeQuery.setChecked(true);
            bss.activeQuery.setClickable(true);
        }else{
            //bss.activeQuery.setText(" ");
            bss.activeQuery.setChecked(false);
            //bss.activeQuery.setClickable(false);
        }*/

/*        //Active Section
        String selectedSection = vs.getSection();
        if(! TextUtils.isEmpty(selectedSection)){
            View rootView = bss.getRoot();
            ToggleButton selectedToggleButton = (ToggleButton) rootView.findViewWithTag(selectedSection);
            Drawable relatedDrawable = selectedToggleButton.getButtonDrawable();
            bss.activeSection.setButtonDrawable(relatedDrawable);
            bss.activeSection.setChecked(true);
        }else{
            bss.activeSection.setChecked(false);
            //Leave the old ButtonDrawable
        }*/

/*        //Active Price
        Integer price = (vs.price == null ? 0 : vs.price);
        ToggleButton detailedPrice = (ToggleButton) getView().findViewWithTag(price.toString());
        ToggleButton activePrice = bss.activePrice;
        if(vs.getPrice() != null){
            // create a string made up of n copies of string s
            String s = "$";
            Integer n = price;
            activePrice.setText(String.format("%0" + n + "d", 0).replace("0",s));
            activePrice.setTag(vs.price);
            activePrice.setChecked(true);
            detailedPrice.setChecked(true);
        }else{
            activePrice.setText("$"); //If any price is set show cheapest but deactivated
            activePrice.setTag("1");
            activePrice.setChecked(false);
            detailedPrice.setChecked(false);
        }*/

/*        //Active Opennow
        if(null != vs.getTime()){
            bss.activeOpennow.setChecked(true);
            bss.detailedOpennow.setChecked(true);
        }else{
            bss.activeOpennow.setChecked(false);
            bss.detailedOpennow.setChecked(false);
        }*/
    }

    public void setupMap(GoogleMap googleMap){
        searchVM.map = googleMap;
        searchVM.map.setMyLocationEnabled(true);
        searchVM.map.getUiSettings().setMapToolbarEnabled(false);
        searchVM.map.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.search_map_style));

        if(searchVM.lastStateSaved){
            Toast.makeText(getActivity(), "restore lastStateSaved", Toast.LENGTH_SHORT).show();
            searchVM.restoreLastState(); //show last state
            //updateActiveSearchHint(searchVM.lastVS.getValue()); //should go into restoreLastMapState but viewmodel can't reference activities/fragments
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

        VenueSearch initialVS = new VenueSearch(25);
        searchVM.getVenues(initialVS);
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
                        //Toast.makeText(getContext(), "Loading....", Toast.LENGTH_SHORT).show();
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

    private void observeVenueSearch(){
        //observe the currenlty active Venue Search to dynamically adapt the active_search_layout
        searchVM.lastVS.observe(this, new Observer<VenueSearch>() {
            @Override
            public void onChanged(@Nullable VenueSearch vs) {
                updateActiveSearchHint(vs);
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
                SzUtils.createVenuePin(getContext(), venue.rating, venue.category.icon).observe(this, new Observer<Bitmap>() {
                    @Override
                    public void onChanged(@Nullable Bitmap bitmap) {
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(new LatLng(venue.lat, venue.lng))
                                .title(venue.name)
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

    public void alterSearchQuery(View view){
        ToggleButton activeQuery = bss.activeQuery;
        SearchView detailedQuery = bss.detailedQuery;
        Boolean activeCalls = (view == activeQuery); //Did Active (Toggle button) or Detailed (SearchView) made the call?
        String query;

        //Adapt activeQuery and Sync Detailed~Active
        if(activeCalls){
            if(activeQuery.isChecked()){
                query = activeQuery.getText().toString();
            }else{
                query = null;
            }
            detailedQuery.setQuery(query,false);
        }else{
            query = detailedQuery.getQuery().toString();
            if(TextUtils.isEmpty(query)){
                //bss.activeQuery.setText(" ");
                activeQuery.setChecked(false);
                //bss.activeQuery.setClickable(false);
            }else{
                activeQuery.setText(query);
                activeQuery.setChecked(true);
                activeQuery.setClickable(true);
            }
        }

        if(activeCalls && !activeQuery.isChecked())
            return; //Or both activeQuery & detailed search, since the query was set empty and triggered the Searchviews onTextChangeListener
        //make the search
        VenueSearch alteredVS = searchVM.lastVS.getValue();
        alteredVS.setQuery(query);
        searchVM.getVenues(alteredVS);
    }

    public void alterSearchSection(View view) {
        Venue.Section section = Venue.Section.valueOf(view.getTag().toString());

        //Sync detailed_view and active_view
        ToggleButton activeSection = bss.activeSection;
        ToggleButton detailedSection = (ToggleButton) bss.getRoot().findViewWithTag(section);
        if(view == activeSection){
            //Sync Detailed
            if(activeSection.isChecked())
                detailedSection.setChecked(true);
            else{
                section = null;
                detailedSection.setChecked(false);
            }

        }else{
            //Sync Active
            if(detailedSection.isChecked()){
                activeSection.setTag(section);
                Drawable relatedDrawable = detailedSection.getButtonDrawable();
                activeSection.setButtonDrawable(relatedDrawable);
                activeSection.setChecked(true);
            }else{
                section = null;
                activeSection.setChecked(false);
                //Leave the old ButtonDrawable and Tag as is
            }
        }

        //make Search
        VenueSearch alteredVS = searchVM.lastVS.getValue();
        alteredVS.setSection(section); //Convert the string section to enum section
        searchVM.getVenues(alteredVS);
    }

    public void alterSearchPrice(View view){
        Integer price = Integer.valueOf(view.getTag().toString());
        VenueSearch alteredVS = searchVM.lastVS.getValue();
        ToggleButton activePrice = bss.activePrice;
        ToggleButton detailedPrice;

        //Sync detailed_view and active_view
        if(view == activePrice){
            //Update active according and sync Detailed
            if(!activePrice.isChecked())
                price = 0; //Set price tag only if activeButton is checked
            detailedPrice = (ToggleButton) getView().findViewWithTag(price.toString()); //check according detailedPrice
            detailedPrice.setChecked(true);
        }else{
            //Sync active
            Integer newPrice = (price == null || price == 0 ? 0 : price); //Never show price 0 / Any at least show 1
            if(price != 0){
                // create a string made up of n copies of string s
                activePrice.setText(String.format("%0" + newPrice + "d", 0).replace("0","$"));
                activePrice.setTag(newPrice);
                activePrice.setChecked(true);
            }else{
                activePrice.setText("$"); //If any price is set show cheapest but deactivated
                activePrice.setTag("1");
                activePrice.setChecked(false);
            }
        }

        alteredVS.setPrice(price);
        searchVM.getVenues(alteredVS);
    }

    public void alterSearchOpennow(View view){
        Boolean opennow;
        if(view instanceof CheckBox)
            opennow = ((CheckBox) view).isChecked();
        else
            opennow = ((ToggleButton) view).isChecked();
        String nowDate = null;
        VenueSearch alteredVS = searchVM.lastVS.getValue();
        //Toast.makeText(getActivity(), "alterSearchOpennow: "+opennow.toString(), Toast.LENGTH_SHORT).show();
        if(opennow)
            nowDate = SzUtils.getNowDate("yyyy-MM-dd hh:mm");
        alteredVS.setTime(nowDate);
        searchVM.getVenues(alteredVS);

        //Sync detailed_view and active_view
        if(view == bss.activeOpennow){
            //Update detailed
            //Toast.makeText(getActivity(), "view == binding.bottomsheetSearch.activeOpennow: ", Toast.LENGTH_SHORT).show();
            bss.detailedOpennow.setChecked(opennow);
        }else{
            bss.activeOpennow.setChecked(opennow);
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

        setupSearchbarViews();
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


