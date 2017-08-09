package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.activities.BottomTabsActivity;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.FragmentProfileUserBinding;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.SzUtils;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.viewModels.UserProfileViewModel;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;


public class UserProfileFragment extends BaseFragment {
    private UserProfileViewModel userProfileVM;
    private FragmentProfileUserBinding binding;
    SupportMapFragment mapFragment;
    GoogleMap map;
    private User user;

    public static UserProfileFragment newInstance(String username) {
        Bundle bundle = new Bundle();
        bundle.putString("username", username);
        UserProfileFragment fragment = new UserProfileFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        userProfileVM = ViewModelProviders.of(this).get(UserProfileViewModel.class);
        userProfileVM.initUser(getArguments().getString("username"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile_user, container, false);
        binding.setFrag(this);

        userProfileVM.getUser().observe(this, new Observer<User>() {
            @Override
            public void onChanged(@Nullable User u) {
                user = u;
                binding.setUser(user);

                Picasso.with(getActivity())
                        .load(user.getProfilePicture()) //Picasso needs "http://" or "https://" url
                        .placeholder(R.drawable.ic_owner) //Placeholders and error images are not resized and must be fairly small images.
                        .error(R.drawable.ic_owner)
                        //.centerCrop().resize(256,256) not neccessary since we do that for each uploaded img by default
                        .transform(new CropCircleTransformation())
                        .into(binding.userprofilePicture);

                ;
                switch(user.getFriendshipStatus()){
                    case NONE:
                        binding.userprofileFriendshipStatus.setImageResource(R.drawable.ic_user_add);
                        break;
                    case RELATED_CONFIRMED:
                        binding.userprofileFriendshipStatus.setImageResource(R.drawable.ic_outgoing_request);
                        break;
                    case RELATING_CONFIRMED:
                        binding.userprofileFriendshipStatus.setImageResource(R.drawable.ic_incoming_request);
                        break;
                    case CONFIRMED:
                        binding.userprofileFriendshipStatus.setImageResource(R.drawable.ic_checkdone);
                        break;
                    default:
                        break;
                }

                mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        map = googleMap;
                        LatLng pos = userProfileVM.getLatLng();
                        if(map != null && pos != null){
                            map.addMarker(new MarkerOptions()
                                    .position(pos)
                                    .title(user.getUsername())
                                    .icon(BitmapDescriptorFactory.defaultMarker(340)));
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 10));
                        }
                    }
                });
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onPause() {
        super.onPause();
        getChildFragmentManager().beginTransaction()
                .remove(mapFragment)
                .commit();
        map = null;
    }

    public String getAge(String timestamp){
        if(timestamp != null)
            return SzUtils.getAge(SzUtils.timestampToCal(timestamp));
        else
            return  null;
    }

    public void addFriend(View view){
        userProfileVM.addFriend(userProfileVM.getUsername());
    }
}

