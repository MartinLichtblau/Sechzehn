package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.MarkerOptions;

import javax.annotation.ParametersAreNonnullByDefault;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Venue;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.FragmentVenueBinding;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services.VenueService;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.DefaultCallback;
import retrofit2.Call;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VenueFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VenueFragment extends DataBindingFragment<FragmentVenueBinding> implements OnMapReadyCallback {
    private static final String ARG_PARAM1 = "venueId";

    private String venueId;
    private Venue venue;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param venueId The venue's id.
     * @return A new instance of fragment VenueFragment.
     */

    public static VenueFragment newInstance(String venueId) {
        VenueFragment fragment = new VenueFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, venueId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            venueId = getArguments().getString(ARG_PARAM1);
        }
    }


    @Override
    protected FragmentVenueBinding initDataBinding(LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentVenueBinding.inflate(inflater, container, false);
    }

    @Override
    protected void useDataBinding(final FragmentVenueBinding binding, Bundle savedInstanceState) {
        binding.mapView.onCreate(savedInstanceState);
        VenueService.VenueService.getVenue(venueId).enqueue(new DefaultCallback<Venue>(getActivityEx()) {
            @Override
            public void onResponse(Call<Venue> call, Response<Venue> response) {
                if (response.isSuccessful()) {
                    venue = response.body();
                    binding.setVenue(venue);
                    binding.mapView.getMapAsync(VenueFragment.this);
                }
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.addMarker(new MarkerOptions().position(venue.getPosition()));
    }
}
