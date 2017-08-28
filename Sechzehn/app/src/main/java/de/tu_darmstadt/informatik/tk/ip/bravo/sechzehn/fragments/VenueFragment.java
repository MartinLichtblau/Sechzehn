package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;


import android.R;
import android.app.Fragment;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.gordonwong.materialsheetfab.MaterialSheetFab;

import java.text.DecimalFormat;
import java.util.ArrayList;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.activities.ImageGalleryActivity;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Pagination;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Venue;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.venue.CheckIn;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.venue.Comment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.venue.Hour;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.venue.Photo;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.FragmentVenueBinding;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.items.CommentItem;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.items.HourItem;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.DefaultCallback;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.ImagePicker;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.SzUtils;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.views.AnimatedFAB;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.views.NewCommentView;
import retrofit2.Call;
import retrofit2.Response;

import static de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services.VenueService.VenueService;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VenueFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VenueFragment extends DataBindingFragment<FragmentVenueBinding> implements OnMapReadyCallback, RatingBar.OnRatingBarChangeListener {

    public static final int PHOTO_NEW_COMMENT = 7634;
    private static final String ARG_PARAM1 = "venueId";
    private static DecimalFormat ratingFormatter = new DecimalFormat("#0.0");
    public final HourItem UNKNOWN_HOUR = new HourItem(new Hour(Hour.Day.UNKNOWN, null, null));
    public final NewCommentView.Listener newCommentListener = new NewCommentView.Listener() {
        @Override
        public void addComment(Comment comment) {
            binding.comments.add(1, new CommentItem(comment, fragNavController()));
        }

        @Override
        public void addPhotoToNewComment() {
            startActivityForResult(ImagePicker.getPickImageIntent(getContext()), PHOTO_NEW_COMMENT);
        }
    };
    private String venueId;
    private Venue venue;
    private MaterialSheetFab<AnimatedFAB> fabSheet;
    private NewCommentView newCommentView;

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
        //args.putString(ARG_PARAM1, "4bccb6ebb6c49c7418419491");
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * format the user rating to display exactly one mantissa
     *
     * @param rating user rating as double value
     * @return String containing the formatted rating
     */
    @Nullable
    public static String formatRating(@Nullable @FloatRange(from = -1.0, to = 10.0) Double rating) {
        if (rating != null) {
            if (rating < 0) {
                return "-.-";
            }
            return ratingFormatter.format(rating);
        }
        return null;
    }

    /**
     * maps the number price range to euro signs
     */
    @Nullable
    public static String formatPrice(@Nullable @IntRange(from = 1, to = 5) Integer price, @Nullable String category) {
        StringBuilder sb = new StringBuilder();
        if (price != null) {
            for (int i = 0; i < price; i++) {
                sb.append('$');
            }
            if (!TextUtils.isEmpty(category)) {
                sb.append(" · ");
            }
        }
        if (!TextUtils.isEmpty(category)) {
            sb.append(category);
        }

        return sb.toString();
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

    public void showTopVisitorProfile(int id) {
        if (venue.topVisitors.size() > id) {
            fragNavController().pushFragment(
                    UserProfileFragment.newInstance(
                            venue.topVisitors.get(id).user.getUsername()
                    )
            );
        }
    }

    public void showPhotos(int startIndex) {
        if (startIndex >= venue.photos.size()) return;
        ArrayList<String> urls = getPhotoUrls();
        Intent showPhoto = new Intent(getContext(), ImageGalleryActivity.class)
                .putStringArrayListExtra(ImageGalleryActivity.KEY_IMAGES, urls)
                .putExtra(ImageGalleryActivity.KEY_POSITION, startIndex);
        startActivity(showPhoto);
    }

    @NonNull
    private ArrayList<String> getPhotoUrls() {
        ArrayList<String> urls = new ArrayList<>();
        for (Photo p : venue.photos) {
            urls.add(p.url);
        }
        return urls;
    }

    @Override
    protected void useDataBinding(final FragmentVenueBinding binding, Bundle savedInstanceState) {
        binding.mapView.onCreate(savedInstanceState);

        fabSheet = new MaterialSheetFab<>(binding.checkin, binding.fabSheet, binding.overlay,
                getResources().getColor(R.color.white, null),
                SzUtils.getThemeColor(getActivityEx(), R.attr.colorAccent));


        VenueService.getVenue(venueId).enqueue(new DefaultCallback<Venue>(getActivityEx()) {
            @Override
            public void onResponse(Call<Venue> call, Response<Venue> response) {
                if (response.isSuccessful()) {
                    venue = response.body();
                    binding.setVenue(venue);
                    binding.mapView.getMapAsync(VenueFragment.this);
                    displayHours();
                }
            }
        });

        loadComments(binding);

        binding.ratingBar.setOnRatingBarChangeListener(this);

        newCommentView = new NewCommentView(getActivityEx(), binding.viewCommentNew, venueId, newCommentListener);


    }

    private void loadComments(final FragmentVenueBinding binding) {
        VenueService.getComments(venueId, null, null).enqueue(new DefaultCallback<Pagination<Comment>>(getActivityEx()) {
            @Override
            public void onResponse(Call<Pagination<Comment>> call, Response<Pagination<Comment>> response) {
                if (response.isSuccessful()) {
                    final Pagination<Comment> paginationMini = response.body();
                    for (Comment comment : paginationMini.data) {
                        binding.comments.add(new CommentItem(comment, fragNavController()));
                    }
                    if (paginationMini.total > paginationMini.data.size()) {
                        loadFurtherComments(paginationMini);
                    }
                }
            }

            private void loadFurtherComments(final Pagination<Comment> paginationMini) {
                VenueService.getComments(venueId, 1, paginationMini.total).enqueue(new DefaultCallback<Pagination<Comment>>(getActivityEx()) {
                    @Override
                    public void onResponse(Call<Pagination<Comment>> call, Response<Pagination<Comment>> response) {
                        if (response.isSuccessful()) {
                            final Pagination<Comment> pagination = response.body();
                            for (int i = paginationMini.data.size(); i < pagination.data.size(); i++) {
                                binding.comments.add(new CommentItem(pagination.data.get(i), fragNavController()));
                            }
                        }
                    }
                });
            }
        });
    }


    @Override
    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        if (fromUser) {
            fabSheet.hideSheet();
            int ourRating = ((int) rating) - 1;
            VenueService.checkIn(venueId, new CheckIn(ourRating))
                    .enqueue(new DefaultCallback<CheckIn>(getActivityEx()) {
                                 @Override
                                 public void onResponse(Call<CheckIn> call, Response<CheckIn> response) {
                                     if (response.isSuccessful()) {
                                         final Venue updatedVenue = response.body().venue;
                                         venue.checkinsCount = updatedVenue.checkinsCount;
                                         venue.rating = updatedVenue.rating;
                                         venue.ratingCount = updatedVenue.ratingCount;
                                         binding.ratingBar.setNumStars(0);
                                         binding.setVenue(venue);//To Update the view.
                                     }
                                 }
                             }
                    );
        }
    }


    @Override
    public void onActivityResult(final int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PHOTO_NEW_COMMENT) {
            final Uri imageUri = ImagePicker.getImageFromResult(getContext(), resultCode, data);
            newCommentView.setPhoto(imageUri);
        }
    }

    /**
     * displays the opening hours of a venue
     */
    private void displayHours() {
        if (venue.hours.isEmpty()) {
            binding.hours.add(UNKNOWN_HOUR);
        } else {
            for (Hour hour : venue.hours) {
                binding.hours.add(new HourItem(hour));
            }
        }
    }

    /**
     * let the current user check in into a venue
     *
     * @param v The View which called this method.
     */
    public void checkIn(View v) {
        fabSheet.showSheet();
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        SzUtils.createVenuePin(getContext(), venue.rating, venue.category.icon).observe(this, new Observer<Bitmap>() {
            @Override
            public void onChanged(@Nullable Bitmap bitmap) {
                googleMap.addMarker(new MarkerOptions()
                        .position(venue.getPosition())
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(venue.getPosition(), 10));
            }
        });

    }


}
