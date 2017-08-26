package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.lifecycle.ViewModelStore;
import android.arch.lifecycle.ViewModelStores;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
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
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Resource;
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
                        .load(user.getProfilePicture())
                        .placeholder(R.drawable.ic_owner)
                        .error(R.drawable.ic_owner)
                        .transform(new CropCircleTransformation())
                        .into(binding.userprofilePicture);

                switch(user.getFriendshipStatus()){
                    case NONE:
                        binding.userprofileFriendshipStatus.setText("Add");
                        binding.userprofileFriendshipStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_user_add,0,0,0);
                        break;
                    case RELATED_CONFIRMED:
                        binding.userprofileFriendshipStatus.setText("Abort");
                        binding.userprofileFriendshipStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_outgoing_request,0,0,0);
                        break;
                    case RELATING_CONFIRMED:
                        binding.userprofileFriendshipStatus.setText("Answer");
                        binding.userprofileFriendshipStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_incoming_request,0,0,0);
                        break;
                    case CONFIRMED:
                        //binding.userprofileFriendshipStatus.setText("Unfriend");
                        binding.userprofileFriendshipStatus.setText("");
                        binding.userprofileFriendshipStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_heart,0,0,0);
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

    /**
     * use this method to calclate the age of a user based on a timestamp
     *
     * @param timestamp timestamp received from the Sechzehn API
     * @return Age of the user as String
     */
    public String getAge(String timestamp){
        if(timestamp != null)
            return SzUtils.getAge(SzUtils.timestampToCal(timestamp));
        else
            return  null;
    }

    /**
     * use this to infer the FriendshipDiafrag
     *
     * @param view
     */
    public void friendship(View view){
        fragNavController().showDialogFragment(FriendshipDiafrag.newInstance());
    }
}

