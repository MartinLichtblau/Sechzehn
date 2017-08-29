package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;

import android.arch.lifecycle.MutableLiveData;
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
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
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
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.activities.BottomTabsActivity;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Friendship;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.MarkerMarkerOptions;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Pagination;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Resource;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Venue;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.VenueSearch;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.BottomsheetSearchBinding;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.FragmentSearchBinding;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.items.VenueItem;
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

    LatLng lastValidMapCenter;
    Float lastValidMapZoom;
    Boolean userMovedCamera = false;
    Float bsCollapsed;
    Float bsExpanded;
    BottomsheetSearchBinding bss;
    BottomSheetBehavior bssBehavior;

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
        //observeVenueSearch();
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

        //Initialize first venueSearch object for proper functioning of UI(altough the initialSearch() will come shortly after)
        setupBottomSheet();
        //setupSearchbarViews();  now in on Resume because of Bug
        setupResultList();

        return binding.getRoot();
    }

    private void setupBottomSheet() {
        Log.d(TAG, "setupBottomSheet");
        bss = binding.bottomsheetSearch;
        bssBehavior = BottomSheetBehavior.from(binding.bottomsheetSearch.getRoot());
        bsCollapsed = getActivity().getResources().getDimension(R.dimen.search_bottomsheet_collapsed);
        bsExpanded = getActivity().getResources().getDimension(R.dimen.search_bottomsheet_expanded);

        if(searchVM.lastBssState == BottomSheetBehavior.STATE_EXPANDED) { //Open it collapsed or (in case it's onresumed) in it's last state
            bssBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }

        bssBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            CameraUpdate cu;
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                searchVM.lastBssState = newState;
                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        setMapPaddingBotttom(1f);
                        cu = CameraUpdateFactory.zoomTo(-1.1f + searchVM.lastSetZoom);
                        searchVM.map.animateCamera(cu, 300, null);
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        setMapPaddingBotttom(0f);
                        cu = CameraUpdateFactory.zoomTo(searchVM.lastSetZoom);
                        searchVM.map.animateCamera(cu, 300, null);
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
                switch (bssBehavior.getState()) {
                    case BottomSheetBehavior.STATE_DRAGGING:
                        setMapPaddingBotttom(slideOffset);
                        searchVM.map.moveCamera(CameraUpdateFactory.newLatLng(lastValidMapCenter));
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        setMapPaddingBotttom(slideOffset);
                        searchVM.map.moveCamera(CameraUpdateFactory.newLatLng(lastValidMapCenter));
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
    }

    private void setupSearchbarViews() {
        Log.d(TAG, "setupSearchbarViews");
        final SearchView searchviewVenue = binding.bottomsheetSearch.detailedQuery;
        searchviewVenue.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "onQueryTextChange");
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
                searchviewVenue.setQuery(null, true);
                //alterSearchQuery(null); //Now that I know that's redundant: onQueryTextChange gets called also when (x) button clears text
            }
        });

    }

    public void setupMap(GoogleMap googleMap) {
        searchVM.map = googleMap;
        searchVM.map.setMyLocationEnabled(true);
        searchVM.map.getUiSettings().setMapToolbarEnabled(false);
        searchVM.map.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.search_map_style));
        setupMapListeners();

        if (searchVM.lastStateSaved) {
            //Toast.makeText(getActivity(), "restore lastStateSaved", Toast.LENGTH_SHORT).show();
            restoreActiveBar();
            if(bssBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) { //Open it collapsed or (in case it's onresumed) in it's last state
                setMapPaddingBotttom(1f);
            }
            searchVM.restoreLastState(); //show last
        } else {
            searchVM.map.moveCamera(CameraUpdateFactory.newLatLngZoom(ownerVM.getLatLng(), searchVM.lastSetZoom));
            lastValidMapCenter = searchVM.map.getCameraPosition().target;
            lastValidMapZoom = searchVM.map.getCameraPosition().zoom;
            initalSearch(); //Initialize anew
        }
    }

    private void setupMapListeners() {
        searchVM.map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                searchVM.selectedMarker = marker;
                if (marker.getTag() instanceof User) {
                    return false;
                }else{
                    Integer pos = (Integer) marker.getTag();
                    /*fastAdapter.select(pos);
                    bss.venueList.scrollToPosition(pos);*/
                    return false;
                }
            }
        });
        searchVM.map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(final Marker marker) {
                if (marker.getTag() instanceof User) {
                    new Handler().postDelayed(new Runnable() {
                        //Maps Bug UI Hang while replacing fragment
                        // Ref. > http://www.javacms.tech/questions/1113754/ui-hang-while-replacing-fragment-from-setoninfowindowclicklistener-interface-met
                        @Override
                        public void run() {
                            fragNavController().pushFragment(UserProfileFragment.newInstance(((User) marker.getTag()).getUsername()));
                        }
                    }, 100);
                } else { //It's a venue with it's ListPosition as Tag
                    Integer pos = (Integer) marker.getTag();
                    final Venue venue = ((VenueItem) fastAdapter.getAdapterItem(pos)).getVenue();
                    new Handler().postDelayed(new Runnable() {
                        //Maps Bug UI Hang while replacing fragment
                        // Ref. > http://www.javacms.tech/questions/1113754/ui-hang-while-replacing-fragment-from-setoninfowindowclicklistener-interface-met
                        @Override
                        public void run() {
                            fragNavController().pushFragment(VenueFragment.newInstance(venue.id));
                        }
                    }, 100);
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
                if (REASON_GESTURE == i) {
                    userMovedCamera = true;
                    if (searchVM.lastBssState != BottomSheetBehavior.STATE_COLLAPSED) {
                        //User should not scroll when the BottomSheet is expanded and map is super tiny small
                        bssBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                }
            }
        });
        searchVM.map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                lastValidMapCenter = searchVM.map.getCameraPosition().target;
                lastValidMapZoom = searchVM.map.getCameraPosition().zoom;
                if (userMovedCamera) {
                    //Make user set zoom level the new baseZoomLevel, just
                    if(searchVM.lastBssState == BottomSheetBehavior.STATE_COLLAPSED)
                        searchVM.lastSetZoom = searchVM.map.getCameraPosition().zoom;

                    //show searchAgainHere Button if it's not already visible and not to start multiple Handlers
                    //INVISIBLE: (Handler already on it to show it) or VISIBLE
                    if(binding.searchAgainHere.getVisibility() == View.GONE){
                        binding.searchAgainHere.setVisibility(View.INVISIBLE);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                binding.searchAgainHere.setVisibility(View.VISIBLE);
                            }
                        }, 750);
                    }
                    userMovedCamera = false;
                }
            }
        });
        searchVM.map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                Location loc = LocationService.getPreviousBestLocation();
                lastValidMapCenter = new LatLng(loc.getLatitude(), loc.getLongitude());
                return false;
            }
        });
    }

    private void setMapPaddingBotttom(Float offset) {
        //From 0.0 (min) - 1.0 (max)
        Float maxMapPaddingBottom = bsExpanded - bsCollapsed;
        //left,top,right,bottom
        searchVM.map.setPadding(0, 0, 0, Math.round(offset * maxMapPaddingBottom));
    }

    public void initalSearch() {
        //Show only nearby users and venues
        searchVM.searchXUsersNearby(50, ownerVM.getLatLng().latitude, ownerVM.getLatLng().longitude, searchVM.getVisibleRadius());
        VenueSearch initialVS = new VenueSearch();
        searchVM.getVenues(initialVS);
    }

    public void observeSearchResults() {
        searchVM.userResults.observe(this, new Observer<Resource>() {
            @Override
            public void onChanged(@Nullable Resource resource) {
                switch (resource.status) {
                    case LOADING:
                        //Toast.makeText(getContext(), "Loading....", Toast.LENGTH_SHORT).show();
                        break;
                    case ERROR:
                        Toast.makeText(getContext(), "Error: " + resource.message, Toast.LENGTH_SHORT).show();
                        break;
                    case SUCCESS:
                        Pagination<User> userPagination = (Pagination<User>) resource.data;
                        searchVM.removeAllUsersOnMap();
                        addUsers(userPagination.data);
                        new Handler().postDelayed(new Runnable() {
                            //Maps Bug UI Hang while replacing fragment
                            // Ref. > http://www.javacms.tech/questions/1113754/ui-hang-while-replacing-fragment-from-setoninfowindowclicklistener-interface-met
                            @Override
                            public void run() {
                                //Live update User every 30 seconds
                                searchVM.searchXUsersNearby(50, ownerVM.getLatLng().latitude, ownerVM.getLatLng().longitude, 50.0);
                            }
                        }, 30000);
                        break;
                }
            }
        });
        searchVM.venueResults.observe(this, new Observer<Resource>() {
            @Override
            public void onChanged(@Nullable Resource resource) {
                switch (resource.status) {
                    case LOADING:
                        //Toast.makeText(getContext(), "Loading....", Toast.LENGTH_SHORT).show();
                        binding.searchAgainHere.setVisibility(View.GONE);
                        //@TODO show loading dialog progress bar
                        break;
                    case ERROR:
                        Toast.makeText(getContext(), "Error: " + resource.message, Toast.LENGTH_SHORT).show();
                        break;
                    case SUCCESS:
                        Pagination<Venue> venuePagination = (Pagination<Venue>) resource.data;
                        searchVM.removeAllVenuesOnMap();
                        clearVenueList();
                        addVenues(venuePagination.data);
                        break;
                }
            }
        });
    }

    public void addUsers(List<User> userList) {
        createAddUserMarkers(userList);
    }

    public void addVenues(final List<Venue> venueList) {
        createVenueMarkerPins(venueList).observe(this, new Observer<LinkedHashMap<Venue, Bitmap>>() {
            @Override
            public void onChanged(@Nullable LinkedHashMap<Venue, Bitmap> venueIconMap) {
                searchVM.createAddVenueMarkers(venueIconMap);
                addVenuesOnList(venueIconMap);
                //Open BottomSearchSheet
                bssBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
    }

    public MutableLiveData<LinkedHashMap<Venue, Bitmap>> createVenueMarkerPins(final List<Venue> venueList) {
        final MutableLiveData<LinkedHashMap<Venue, Bitmap>> liveVenueIconMap = new MutableLiveData<>();
        final LinkedHashMap<Venue, Bitmap> tempVenueIconMap = new LinkedHashMap<>();
        final int[] count = {0};
        for (final Venue venue : venueList) {
            final MutableLiveData liveBitmap = SzUtils.createVenuePin(getContext(), venue.rating, venue.category.icon);
            tempVenueIconMap.put(venue, null); //to set entry at correct position
            liveBitmap.observe(this, new Observer<Bitmap>() {
                @Override
                public void onChanged(@Nullable Bitmap bitmap) {
                    tempVenueIconMap.put(venue, bitmap); //Overwrite entry at already correctly set position
                    count[0]++;
                    if (count[0] >= (venueList.size())) //If the last bitmap returned (at some point in time)
                        liveVenueIconMap.setValue(tempVenueIconMap);  //setValue to call Observer
                    liveBitmap.removeObservers(SearchFragment.this);
                }
            });
        }
        return liveVenueIconMap;
    }

    private void createAddUserMarkers(final List<User> userList) {
        final ArrayList<MarkerMarkerOptions> tempMmoList = new ArrayList<>();
        Boolean highlight = false;
        for (final User user : userList) {
            if (!TextUtils.equals(user.getUsername(), SzUtils.getOwnername())) {
                if (user.getFriendshipStatus() == Friendship.Status.CONFIRMED)
                    highlight = true;
                final MutableLiveData liveBitmap = SzUtils.createUserPin(getContext(), highlight, user.getProfilePicture());
                liveBitmap.observe(this, new Observer<Bitmap>() {
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
                        tempMmoList.add(new MarkerMarkerOptions(marker, markerOptions));
                        if (tempMmoList.size() >= (userList.size() - 1))
                            searchVM.usersOnMap = tempMmoList;

                        liveBitmap.removeObserver(this);
                    }
                });
            }
        }
    }

    public void alterSearchQuery(View view) {
        ToggleButton activeQuery = bss.activeQuery;
        SearchView detailedQuery = bss.detailedQuery;
        Boolean activeCalls = (view == activeQuery); //Did Active (Toggle button) or Detailed (SearchView) made the call?
        String query;

        //Adapt activeQuery and Sync Detailed~Active
        if (activeCalls) {
            if (TextUtils.isEmpty(activeQuery.getText())) {
                //Don't serach, instead Open detailedSearch and focus querySearch
                activeQuery.setChecked(false);
                bssBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                bss.searchBottomsheetAppbarlayout.setExpanded(true);
                detailedQuery.findFocus();
                detailedQuery.setIconified(false);
                detailedQuery.requestFocusFromTouch();
                return;
            } else {
                if(activeQuery.isChecked()){
                    query = activeQuery.getText().toString();
                    detailedQuery.setQuery(query, false);
                }else{
                    query = "";
                }

            }
        } else {
            query = detailedQuery.getQuery().toString();
            if (TextUtils.isEmpty(query)) {
                activeQuery.setChecked(false);
            } else {
                activeQuery.setText(query);
                activeQuery.setChecked(true);
            }
        }
        //make the search
        VenueSearch alteredVS = searchVM.lastVS;
        alteredVS.setQuery(query);
        searchVM.getVenues(alteredVS);
    }

    public void alterSearchSection(View view) {
        Venue.Section section = Venue.Section.valueOf(view.getTag().toString());
        //Sync detailed_view and active_view
        ToggleButton activeSection = bss.activeSection;
        ToggleButton detailedSection = (ToggleButton) bss.getRoot().findViewWithTag(section);
        if (view == activeSection) {
            //Sync Detailed
            if (activeSection.isChecked())
                detailedSection.setChecked(true);
            else {
                section = null;
                detailedSection.setChecked(false);
            }

        } else {
            //Sync Active
            if(searchVM.lastBssState == BottomSheetBehavior.STATE_EXPANDED){
                if (detailedSection.isChecked()) {
                    activeSection.setTag(section);
                    Drawable relatedDrawable = detailedSection.getButtonDrawable();
                    activeSection.setButtonDrawable(relatedDrawable);
                    activeSection.setChecked(true);
                } else {
                    section = null;
                    activeSection.setChecked(false);
                    //Leave the old ButtonDrawable and Tag as is
                }
            }else{
                //Sections should act as instant search sections, making a new Search only with this section
                VenueSearch newVS = new VenueSearch();
                newVS.setSection(section);
                searchVM.getVenues(newVS);
            }

        }

        //make Search
        VenueSearch alteredVS = searchVM.lastVS;
        alteredVS.setSection(section); //Convert the string section to enum section
        searchVM.getVenues(alteredVS);
    }

    public void alterSearchPrice(View view) {
        Integer price = Integer.valueOf(view.getTag().toString());
        VenueSearch alteredVS = searchVM.lastVS;
        ToggleButton activePrice = bss.activePrice;
        ToggleButton detailedPrice;

        //Sync detailed_view and active_view
        if (view == activePrice) {
            //Update active according and sync Detailed
            if (!activePrice.isChecked())
                price = 0; //Set price tag only if activeButton is checked
            detailedPrice = (ToggleButton) getView().findViewWithTag(price.toString()); //check according detailedPrice
            detailedPrice.setChecked(true);
        } else {
            //Sync active
            Integer newPrice = (price == null || price == 0 ? 0 : price); //Never show price 0 / Any at least show 1
            if (price != 0) {
                // create a string made up of n copies of string s
                activePrice.setText(String.format("%0" + newPrice + "d", 0).replace("0", "$"));
                activePrice.setTag(newPrice);
                activePrice.setChecked(true);
            } else {
                activePrice.setText("$"); //If any price is set show cheapest but deactivated
                activePrice.setTag("1");
                activePrice.setChecked(false);
            }
        }

        alteredVS.setPrice(price);
        searchVM.getVenues(alteredVS);
    }

    public void alterSearchOpennow(View view) {
        Boolean opennow;
        if (view instanceof CheckBox)
            opennow = ((CheckBox) view).isChecked();
        else
            opennow = ((ToggleButton) view).isChecked();
        String nowDate = null;
        VenueSearch alteredVS = searchVM.lastVS;
        //Toast.makeText(getActivity(), "alterSearchOpennow: "+opennow.toString(), Toast.LENGTH_SHORT).show();
        if (opennow)
            nowDate = SzUtils.getNowDate("yyyy-MM-dd hh:mm");
        alteredVS.setTime(nowDate);
        searchVM.getVenues(alteredVS);

        //Sync detailed_view and active_view
        if (view == bss.activeOpennow) {
            //Update detailed
            //Toast.makeText(getActivity(), "view == binding.bottomsheetSearch.activeOpennow: ", Toast.LENGTH_SHORT).show();
            bss.detailedOpennow.setChecked(opennow);
        } else {
            bss.activeOpennow.setChecked(opennow);
        }
    }

    public void fab(View view) {
        bssBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        //focusSearchView();
    }

    private void focusSearchView() {
       /* final SearchView searchView = binding.searchSearchbarView;
        searchView.setIconifiedByDefault(false);
        searchView.requestFocus();
        ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).
                toggleSoftInput(InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY);*/
    }

    private void restoreActiveBar(){
        Log.d(TAG, "restoreActiveBar");
        VenueSearch vs = searchVM.lastVS;

        //Active Query
        if(vs.getQuery() != null){
            bss.activeQuery.setText(vs.getQuery());
            bss.activeQuery.setChecked(true);
            bss.activeQuery.setClickable(true);
        }else{
            bss.activeQuery.setChecked(false);
        }
        //Active Section
        String selectedSection = vs.section == null ? "" : vs.section.toString();
        if(! TextUtils.isEmpty(selectedSection)){
            int resId = getResources().getIdentifier("section_"+selectedSection.toLowerCase(), "id", getActivity().getPackageName());
            ToggleButton selectedToggleButton = (ToggleButton)  bss.getRoot().findViewById(resId);
            Drawable relatedDrawable = selectedToggleButton.getButtonDrawable();
            bss.activeSection.setTag(selectedSection);
            bss.activeSection.setButtonDrawable(relatedDrawable);
            bss.activeSection.setChecked(true);
        }else{
            bss.activeSection.setChecked(false);
        }
        //Active Price
        //never show price any (0) in active bar, better UXP if it has cheapest price filter by default
        Integer price = (vs.price == null ? 0 : vs.price);
        ToggleButton activePrice = bss.activePrice;
        if(price > 0){
            // create a string made up of price copies of string "$"
            activePrice.setText(String.format("%0" + price + "d", 0).replace("0","$"));
            activePrice.setTag(vs.price);
            activePrice.setChecked(true);
        }else{
            activePrice.setText("$");
            activePrice.setTag(1);
            activePrice.setChecked(false);
        }
        //Active Opennow
        if(null != vs.getTime()){
            bss.activeOpennow.setChecked(true);
        }
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


    //>>>>>>>>>>>> Venues endless List view with FastAdapter
    private FastItemAdapter fastAdapter = new FastItemAdapter<>();

    private void setupResultList(){
        Log.d(TAG, "setupResultList");
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        bss.venueList.setLayoutManager(llm);
        bss.venueList.setAdapter(fastAdapter);

        fastAdapter.withOnClickListener(new FastAdapter.OnClickListener() {
            @Override
            public boolean onClick(View v, IAdapter adapter, IItem item, int position) {
                VenueItem venueItem = (VenueItem) item;
                Marker marker = searchVM.venuesOnMap.get(position).marker;
                CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 1.1f + searchVM.lastSetZoom);
                searchVM.map.animateCamera(cu, 500, null);
                marker.showInfoWindow();
                searchVM.selectedMarker = marker; //Don't forget to save selected marker, for restoreLastState
                return true;
            }
        });
        fastAdapter.withOnLongClickListener(new FastAdapter.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v, IAdapter adapter, IItem item, int position) {
                Log.d(TAG,"fastAdapter.withOnLongClickListener | "+"pos = "+position);
                final Venue venue = ((VenueItem) item).getVenue();
                new Handler().postDelayed(new Runnable() {
                    //Maps Bug UI Hang while replacing fragment
                    // Ref. > http://www.javacms.tech/questions/1113754/ui-hang-while-replacing-fragment-from-setoninfowindowclicklistener-interface-met
                    @Override
                    public void run() {
                        /*openVenue(venue.id);*/
                        fragNavController().pushFragment(VenueFragment.newInstance(venue.id));
                    }
                }, 100);
                return false;
            }
        });
    }

    private void addVenuesOnList(LinkedHashMap<Venue, Bitmap> venueIconMap){
        Log.d(TAG, "addVenuesOnList");
        //Create out of the venueIconMap the list entries for the fastAdapter ResultList
        for(Map.Entry<Venue, Bitmap> e : venueIconMap.entrySet()){
            fastAdapter.add(new VenueItem(e.getKey(), e.getValue(), SearchFragment.this.fragNavController()));
        }
    }

    private  void clearVenueList(){
        fastAdapter.clear();
    }


}


