package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;


import android.R;
import android.app.Fragment;
import android.content.Intent;
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

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.MarkerOptions;
import com.gordonwong.materialsheetfab.MaterialSheetFab;

import java.text.DecimalFormat;
import java.util.ArrayList;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.activities.BottomTabsActivity;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.activities.ImageGalleryActivity;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Venue;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.venue.CheckIn;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.venue.Comment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.venue.Hour;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.venue.Photo;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.FragmentVenueBinding;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.ViewCommentNewBinding;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.items.CommentItem;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.items.HourItem;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.DefaultCallback;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.OnTextChangedListener;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.SzUtils;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.views.AnimatedFAB;
import retrofit2.Call;
import retrofit2.Response;

import static de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services.VenueService.VenueService;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VenueFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VenueFragment extends DataBindingFragment<FragmentVenueBinding> implements OnMapReadyCallback, RatingBar.OnRatingBarChangeListener {
    private static final String ARG_PARAM1 = "venueId";
    public final HourItem UNKNOWN_HOUR = new HourItem(new Hour(Hour.Day.UNKNOWN, null, null));
    public static final User EMPTY_USER = new User();
    private final User owner = BottomTabsActivity.getOwnerViewModel().getOwner().getValue();

    private Comment newComment = new Comment();

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
        // args.putString(ARG_PARAM1, venueId);
        args.putString(ARG_PARAM1, "4bccb6ebb6c49c7418419491");
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
                    for (Comment comment : venue.comments) {
                        binding.comments.add(new CommentItem(comment, fragNavController()));
                    }
                    displayHours();
                }
            }
        });

        binding.ratingBar.setOnRatingBarChangeListener(this);

        initViewCommentNew(binding.viewCommentNew);


    }

    /**
     * Initialises the View for new comments.
     *
     * @param binding The Binding of the ViewCommentNew
     */
    private void initViewCommentNew(final ViewCommentNewBinding binding) {
        //Init
        binding.setSelf(this);
        binding.setUser(owner);
        binding.setNewComment(newComment);
        binding.body.addTextChangedListener(new OnTextChangedListener() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.submit.setEnabled(!TextUtils.isEmpty(s));
            }
        });
    }

    public void submitNewComment(View v) {
        binding.viewCommentNew.getRoot().setEnabled(false);
        VenueService.comment(venueId, newComment).enqueue(new DefaultCallback<Comment>(getActivityEx()) {
            @Override
            public void onResponse(Call<Comment> call, Response<Comment> response) {
                if (response.isSuccessful()) {
                    newComment.body = "";
                    newComment.photoId = null;
                    Comment myComment = response.body();
                    myComment.user = owner;
                    binding.comments.add(new CommentItem(myComment, fragNavController()));
                }
                onFinally(call);
            }

            @Override
            public void onFinally(Call<Comment> call) {
                binding.viewCommentNew.getRoot().setEnabled(true);
            }
        });


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
    public void onMapReady(GoogleMap googleMap) {
        googleMap.addMarker(new MarkerOptions().position(venue.getPosition()));
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
                                         binding.checkin.setEnabled(false);
                                         final Venue updatedVenue = response.body().venue;
                                         venue.checkinsCount = updatedVenue.checkinsCount;
                                         venue.rating = updatedVenue.rating;
                                         venue.ratingCount = updatedVenue.ratingCount;
                                     }
                                 }
                             }
                    );
        }
    }

    private static DecimalFormat ratingFormatter = new DecimalFormat("#0.0");

    /**
     * format the user rating to display exactly one mantissa
     *
     * @param rating user rating as double value
     * @return String containing the formatted rating
     */
    @Nullable
    public static String formatRating(@Nullable @FloatRange(from = 0.0, to = 10.0) Double rating) {
        if (rating != null) {
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
                sb.append('€');
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

    public static int objectToVisibility(Object obj) {
        return booleanToVisibility(obj != null);
    }

    public static int booleanToVisibility(boolean bool) {
        return bool ? View.VISIBLE : View.GONE;
    }

    public static int booleanToVisibility(boolean bool, boolean invisibleInsteadOfGone) {
        if (invisibleInsteadOfGone)
            return bool ? View.VISIBLE : View.INVISIBLE;
        else
            return bool ? View.VISIBLE : View.GONE;
    }

}
