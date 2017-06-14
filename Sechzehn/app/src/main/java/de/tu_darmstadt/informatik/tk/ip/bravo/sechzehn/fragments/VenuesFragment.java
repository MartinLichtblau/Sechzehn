package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;



/**
 * Created by niccapdevila on 3/26/16.
 */
public class VenuesFragment extends BaseFragment  implements OnMapReadyCallback {

    private GoogleMap map;

    public static VenuesFragment newInstance(int instance) {
        Bundle args = new Bundle();
        args.putInt(ARGS_INSTANCE, instance);
        VenuesFragment fragment = new VenuesFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onStart() {
        super.onStart();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_venues, container, false);
        initializeMap();
        return view;
    }

    //Based on > https://stackoverflow.com/questions/35130433/how-to-use-google-map-in-android-fragment
    private void initializeMap() {
        if (map == null) {
            SupportMapFragment mapFrag = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            mapFrag.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        //setUpMap();// do your map stuff here
    }
}
