package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.FragmentSearchBinding;
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
        ownerVM = ViewModelProviders.of(this).get(OwnerViewModel.class);
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
        setUpMap(map);
    }

    private void setUpMap(GoogleMap map){
        if(map == null)
            return;
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(ownerVM.getLatLng(), 12));

        addUsersNearby();
        //addVenuesNearby();


    }

    private void addUsersNearby(){
        List<User> userList = searchVM.getUsersNearby();
        if(userList.isEmpty())
            return;
        for(userList){
            map.addMarker(new MarkerOptions()
                    .position(new LatLng(,))
                    .title(username));
        }
    }
}
