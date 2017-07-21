package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;

import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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

import static android.databinding.DataBindingUtil.inflate;

public class SearchFragment extends BaseFragment  implements OnMapReadyCallback {
    private FragmentSearchBinding binding;
    private SearchViewModel searchVM;
    private OwnerViewModel ownerVM;
    private SupportMapFragment mapFragment;
    private GoogleMap map;

    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        searchVM = ViewModelProviders.of(this).get(SearchViewModel.class);
        ownerVM = BottomTabsActivity.getOwnerViewModel();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false);
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(SearchFragment.this);

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
        setUpMap();
    }

    private void setUpMap(){
        if(ownerVM.getLatLng() != null){
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(ownerVM.getLatLng(), 12));

            addUsersNearby();
            //addVenuesNearby();
        }

    }

    private void addUsersNearby(){
        searchVM.getXUsersNearby(100, ownerVM.getLatLng().latitude, ownerVM.getLatLng().longitude, 100.0).observe(this, new Observer<Resource>() {
                    @Override
                    public void onChanged(@Nullable Resource resource) {
                        if (resource.status == Resource.Status.LOADING)
                            Toast.makeText(getContext(), "Loading, please wait...", Toast.LENGTH_SHORT).show();
                        else if (resource.status == Resource.Status.ERROR) {
                            Toast.makeText(getContext(), "Error: " + resource.message, Toast.LENGTH_SHORT).show();
                        } else if (resource.status == Resource.Status.SUCCESS) {
                            Toast.makeText(getContext(), "SUCCESS", Toast.LENGTH_SHORT).show();
                            if (resource.data != null && resource.data.getClass().equals(Pagination.class)) {
                                Toast.makeText(getContext(), "got Class Pagination", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

/*
                    if(pagination == null)
                    return;
            }
        });
        if(userList.isEmpty())
            return;
        for (User u : .data) {
        }
            map.addMarker(new MarkerOptions()
                    .position(new LatLng(,))
                    .title(username));*/
}
