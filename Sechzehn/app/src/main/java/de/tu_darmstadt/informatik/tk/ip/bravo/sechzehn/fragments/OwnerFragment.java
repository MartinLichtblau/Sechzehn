package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.FragmentOwnerBinding;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.FragmentProfileUserBinding;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.viewModels.OwnerViewModel;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;


public class OwnerFragment extends BaseFragment implements OnMapReadyCallback {
    private ImageView addFriendButton;
    private ImageView optionsButton;
    private static final String USERNAME = "username";
    private OwnerViewModel viewModel;
    private FragmentOwnerBinding binding;
    SupportMapFragment mapFragment;
    GoogleMap mMap;

    public static OwnerFragment newInstance(String username) {
        Bundle bundle = new Bundle();
        bundle.putString(USERNAME, username);
        OwnerFragment fragment = new OwnerFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_owner, container, false);
        addFriendButton = binding.userprofileAddfriend;
        optionsButton = binding.userprofileOptions;

        //Get ViewModel from everywhere like
        viewModel = ViewModelProviders.of(getActivity()).get(OwnerViewModel.class);
        // or: viewModel = ((BottomTabsActivity)getActivity()).getOwnerViewModel(); // > https://stackoverflow.com/questions/12659747/call-an-activity-method-from-a-fragment

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setup();
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

    private void setup(){
        viewModel.getOwner().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                binding.setUser(user);

                if(user.getProfilePicture() != null && user.getProfilePicture() != "")
                    Picasso.with(getActivity()).load("http://"+user.getProfilePicture()).transform(new RoundedCornersTransformation(10,10)).into(binding.userprofilePicture); //Picasso needs "http://"

                LatLng pos = viewModel.getLatLng();
                if(pos != null){
                    mMap.addMarker(new MarkerOptions().position(pos)
                            .title(getArguments().getString("username")));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 10));
                }
            }
        });
    }
}

