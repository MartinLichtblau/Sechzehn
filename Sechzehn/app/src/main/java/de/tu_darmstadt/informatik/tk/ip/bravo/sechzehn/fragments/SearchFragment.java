package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;

import android.app.ProgressDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
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

public class SearchFragment extends BaseFragment implements GoogleMap.OnInfoWindowClickListener, OnMapReadyCallback{
    private final String TAG = "SearchFragment";
    private FragmentSearchBinding binding;
    private SearchViewModel searchVM;
    private OwnerViewModel ownerVM;
    private MapView mapView;
    private Integer maxMapHeight;

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

        mapView = binding.mapview;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // get the bottom sheet view
        LinearLayout llBottomSheet = binding.bottomSheet;
        // init the bottom sheet behavior
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        // set callback for changes
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        shrinkMapFrameHeight(1);
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        shrinkMapFrameHeight(Math.round(convertDpToPx(getContext(),(float) 256)));
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        shrinkMapFrameHeight(Math.round(convertDpToPx(getContext(),(float) 80)));
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                //mapView.setPadding(0,0,0, 100);
                LatLng pos = searchVM.map.getCameraPosition().target;
                LatLng newPos = new LatLng(pos.latitude - 0.0003, pos.longitude);
                CameraPosition cameraPos = new CameraPosition.Builder().target(newPos)
                        .zoom(searchVM.map.getCameraPosition().zoom)
                        .build();
                searchVM.map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
            }
        });

        return binding.getRoot();
    }

    private void shrinkMapFrameHeight(Integer sub){
        //Max size is in my case 1573
        ViewGroup.LayoutParams params = binding.mapFrame.getLayoutParams();
        params.height = (maxMapHeight - sub);
        Toast.makeText(getActivity(), String.valueOf(maxMapHeight), Toast.LENGTH_SHORT).show();
        Toast.makeText(getActivity(), String.valueOf(params.height), Toast.LENGTH_SHORT).show();
        binding.mapFrame.setLayoutParams(params);
    }

    public float convertDpToPx(Context context, float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public float convertPxToDp(Context context, float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public void fab(View view){
       BottomSheetBehavior.from(binding.bottomSheet).setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        maxMapHeight = mapView.getHeight();
        searchVM.map = googleMap;
        searchVM.map.setMyLocationEnabled(true);
        searchVM.map.setOnInfoWindowClickListener(this);

        //Adapt default visuals
/*        positionLocationButton();*/

        if(searchVM.lastStateSaved){
            searchVM.restoreLastState(); //show last state
        }else{
            searchVM.map.moveCamera(CameraUpdateFactory.newLatLngZoom(ownerVM.getLatLng(), 12));
            initalSearch(); //Initialize anew
        }
    }


    private void positionLocationButton(){
        View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        // position on right bottom
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.setMargins(0, 0, 30, 30);

        //Change Button view
        /*ImageView locationButton = (ImageView) mapView.findViewById(2);*/
    }

    public void initalSearch(){
        //Show only nearby users and venues
        searchVM.searchXUsersNearby(100, ownerVM.getLatLng().latitude, ownerVM.getLatLng().longitude, searchVM.getVisibleMapRange());
        /*searchVM.searchXVenuesNearby(...*/
    }

    public void observeSearchResults(){
        searchVM.searchResultUsers.observe(this, new Observer<Resource>() {
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
        createAddUserMarkers(userList);
        /*showUsersOnList(userList);*/
    }

    private void createAddUserMarkers(final List<User> userList) {
        final HashMap<Marker,MarkerOptions> tempMarkerMap = new HashMap<>();
        for (final User user :  userList) {
            if (!TextUtils.equals(user.getUsername(), SzUtils.getOwnername())) {
                SzUtils.createThumb(SzUtils.ThumbType.USER, user.getProfilePicture()).observe(this, new Observer<Bitmap>() {
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
            //@ venues
        }
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


