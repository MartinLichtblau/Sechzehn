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
import com.ncapdevi.fragnav.FragNavController;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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

public class SearchFragment extends BaseFragment implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {
    private FragmentSearchBinding binding;
    private SearchViewModel searchVM;
    private OwnerViewModel ownerVM;
    private SupportMapFragment mapFragment;
    private GoogleMap map;

    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        searchVM = ViewModelProviders.of(this).get(SearchViewModel.class);
        ownerVM = BottomTabsActivity.getOwnerViewModel();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false);
        Toast.makeText(getActivity(), "FragNavController is null = "+(mFragmentNavigation==null), Toast.LENGTH_SHORT).show();
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

    private void setUpMap() {
        if (ownerVM.getLatLng() != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(ownerVM.getLatLng(), 9));

            searchUsersNearby();
            //searchVenuesNearby();
        }
    }

    private void searchUsersNearby() {
        searchVM.getXUsersNearby(100, ownerVM.getLatLng().latitude, ownerVM.getLatLng().longitude, 100.0).observe(this, new Observer<Resource>() {
            @Override
            public void onChanged(@Nullable Resource resource) {
                if (resource.status == Resource.Status.ERROR) {
                    Toast.makeText(getContext(), "Error: " + resource.message, Toast.LENGTH_SHORT).show();
                } else if (resource.status == Resource.Status.SUCCESS) {
                    if (resource.data != null && resource.data.getClass().equals(Pagination.class)) {
                        Pagination<User> usersPage = (Pagination<User>) resource.data;
                        Toast.makeText(getContext(), usersPage.total + " users nearby", Toast.LENGTH_SHORT).show();
                        mapUsersNearby(usersPage);
                    }
                }
            }
        });
    }

    private void mapUsersNearby(Pagination<User> usersPage) {
        List<User> usersList = usersPage.data;
        List<MarkerOptions> markerOList = new ArrayList<>();
        for (final User u : usersList) {
            final MarkerOptions markerO = new MarkerOptions();
            markerOList.add(markerO);
            LiveData<Bitmap> icon = SzUtils.createThumb(SzUtils.ThumbType.USER, u.getProfilePicture());
            icon.observe(this, new Observer<Bitmap>() {
                @Override
                public void onChanged(@Nullable Bitmap bitmap) {
                    markerO.position(new LatLng(u.getLat(),u.getLng()))
                            .title(u.getUsername())
                            .snippet("Open Profile")
                            .infoWindowAnchor(0.5f, 0.5f)
                            /*.alpha(0.7f)*/
                            .icon(BitmapDescriptorFactory.fromBitmap(bitmap));
                    /*markerO.icon(BitmapDescriptorFactory.defaultMarker());*/
                    map.addMarker(markerO);
                }
            });
        }
      /*  if (!SzUtils.loadImage(getActivity(), "").hasObservers()){
            for (MarkerOptions markerO : markerOList) {
                map.addMarker(markerO);
            }
        }*/
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        setUpMap();
        map.setOnInfoWindowClickListener(this);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        final String username = marker.getTitle();
        new Handler().postDelayed(new Runnable() {
            //Maps Bug UI Hang while replacing fragment
            // Ref. > http://www.javacms.tech/questions/1113754/ui-hang-while-replacing-fragment-from-setoninfowindowclicklistener-interface-met
            @Override
            public void run() {
                fragNavController().pushFragment(UserProfileFragment.newInstance(username));
            }
        }, 100);
    }
}

