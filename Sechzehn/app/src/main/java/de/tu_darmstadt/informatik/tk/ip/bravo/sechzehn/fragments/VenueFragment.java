package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;


import android.R;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.MarkerOptions;
import com.gordonwong.materialsheetfab.MaterialSheetFab;

import java.text.DecimalFormat;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.AnimatedFAB;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Venue;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.venue.CheckIn;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.venue.Hour;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.FragmentVenueBinding;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.items.CommentItem;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.items.HourItem;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services.CheckInService;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services.VenueService;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.DefaultCallback;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.SzUtils;
import retrofit2.Call;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VenueFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VenueFragment extends DataBindingFragment<FragmentVenueBinding> implements OnMapReadyCallback, RatingBar.OnRatingBarChangeListener {
    private static final String ARG_PARAM1 = "venueId";
    public static final HourItem UNKNOWN_HOUR = new HourItem(new Hour(Hour.Day.UNKNOWN, null, null));

    private String venueId;
    private Venue venue;
    private MaterialSheetFab<AnimatedFAB> fabSheet;

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

        fabSheet = new MaterialSheetFab<>(binding.checkin, binding.fabSheet, binding.overlay,
                getResources().getColor(R.color.white, null),
                SzUtils.getThemeColor(getActivityEx(), R.attr.colorAccent));


        VenueService.VenueService.getVenue(venueId).enqueue(new DefaultCallback<Venue>(getActivityEx()) {
            @Override
            public void onResponse(Call<Venue> call, Response<Venue> response) {
                if (response.isSuccessful()) {
                    venue = response.body();
                    binding.setVenue(venue);
                    binding.mapView.getMapAsync(VenueFragment.this);
                    for (int i = 0; i < 12; i++) {
                        binding.comments.add(new CommentItem());
                    }
                    displayHours();
                }
            }
        });

        binding.ratingBar.setOnRatingBarChangeListener(this);

    }

    private void displayHours() {
        if (venue.hours.isEmpty()) {
            binding.hours.add(UNKNOWN_HOUR);
        } else {
            for (Hour hour : venue.hours) {
                binding.hours.add(new HourItem(hour));
            }
        }
    }

    public void checkIn(View v) {
        fabSheet.showSheet();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.addMarker(new MarkerOptions().position(venue.getPosition()));
    }

    @Override
    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        if (fromUser) {
            fabSheet.hideSheet();
            CheckInService.CheckInService.checkIn(venueId, new CheckIn((int) rating))
                    .enqueue(new DefaultCallback<CheckIn>(getActivityEx()) {
                                 @Override
                                 public void onResponse(Call<CheckIn> call, Response<CheckIn> response) {
                                     binding.checkin.setEnabled(false);
                                 }
                             }
                    );
        }
    }

    private static DecimalFormat ratingFormatter = new DecimalFormat("##.0");

    @Nullable
    public static String formatRating(@Nullable @FloatRange(from = 0.0, to = 10.0) Double rating) {
        if (rating != null) {
            return ratingFormatter.format(rating);
        }
        return null;
    }

    @Nullable
    public static String formatPrice(@Nullable @IntRange(from = 1, to = 5) Integer price) {
        if (price != null) {
            switch (price) {
                case 1:
                    return "€ ·";
                case 2:
                    return "€€ ·";
                case 3:
                    return "€€€ ·";
                case 4:
                    return "€€€€ ·";
                case 5:
                    return "€€€€€ ·";
            }
        }
        return null;
    }

    public static int objectToVisibility(Object obj) {
        return booleanToVisibility(obj == null);
    }

    public static int booleanToVisibility(boolean bool) {
        return bool ? View.GONE : View.VISIBLE;
    }
}
