package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.FragmentProfileUserBinding;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.viewModels.UserProfileViewModel;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;


public class UserProfileFragment extends BaseFragment implements OnMapReadyCallback {
    private static final String USERNAME = "username";
    private ImageView addFriendButton;
    private ImageView optionsButton;
    private UserProfileViewModel viewModel;
    private FragmentProfileUserBinding binding;
    SupportMapFragment mapFragment;
    GoogleMap mMap;

    public static UserProfileFragment newInstance(String username) {
        Bundle bundle = new Bundle(); 
        bundle.putString(USERNAME, username);
        UserProfileFragment fragment = new UserProfileFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile_user, container, false);
        addFriendButton = binding.userprofileAddfriend;
        optionsButton = binding.userprofileOptions;
        binding.setUser(new User());

        viewModel = ViewModelProviders.of(this).get(UserProfileViewModel.class);
        if (viewModel.getUser().getValue() == null)
            viewModel.initUser(getArguments().getString("username"));

        SupportMapFragment mapFrag = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);

        return binding.getRoot();
    }

    @Override
    public void onPause() {
        super.onPause();
        getChildFragmentManager().beginTransaction()
                .remove(mapFragment)
                .commit();
        mMap = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;
        setup();
    }

    private void setup() {
        viewModel.getUser().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                binding.setUser(user);
                if(user.getProfilePicture() != null && user.getProfilePicture() != ""){
                    //Picasso.with(getActivity()).setLoggingEnabled(true);
                    Picasso.with(getActivity()).load("http://"+user.getProfilePicture()).transform(new RoundedCornersTransformation(10,10)).into(binding.userprofilePicture); //Picasso needs "http://"
                }
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(viewModel.getUser().getValue().getLat(),viewModel.getUser().getValue().getLng()))
                        .title(getArguments().getString("username")));
            }
        });
    }

}

