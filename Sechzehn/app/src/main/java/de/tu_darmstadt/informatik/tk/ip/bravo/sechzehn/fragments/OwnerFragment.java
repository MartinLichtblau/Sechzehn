package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.activities.BottomTabsActivity;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.activities.LoginActivity;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.FragmentOwnerBinding;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.viewModels.OwnerViewModel;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;


public class OwnerFragment extends BaseFragment implements OnMapReadyCallback {
    private Button logoutButton;
    private Button changePasswordButton;
    private Button resetPasswordButton;
    private Button deleteAccountButton;
    private FragmentOwnerBinding binding;
    private OwnerViewModel viewModel;
    SupportMapFragment mapFragment;
    GoogleMap mMap;

    public static OwnerFragment newInstance() {
        OwnerFragment fragment = new OwnerFragment();
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_owner, container, false);
        binding.setFrag(this); //Most important to actually bind the variables specified in layout.xml | The owner binding goes in ownerSetup() triggered by async map
        logoutButton = binding.ownerLogout;
        changePasswordButton = binding.ownerChangepassword;
        resetPasswordButton = binding.ownerResetpassword;
        deleteAccountButton = binding.ownerDeleteaccount;

        //Get OwnerViewModel from everywhere like
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
        setupOwner();
    }

    @Override
    public void onStart() {
        super.onStart();
        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "@TODO", Toast.LENGTH_SHORT).show();
            }
        });
        /*changeEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeEmail();
            }
        });*/
        deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "@TODO", Toast.LENGTH_SHORT).show();
                //viewModel.changeRealName();

            }
        });
    }

    private void setupOwner(){
        viewModel.getOwner().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                Toast.makeText(getActivity(), "onChanged owner", Toast.LENGTH_SHORT).show(); //@TODO bind a user object of fragment to layout, not the viewmodels
                binding.setUser(user);

                if(user.getProfilePicture() != null && user.getProfilePicture() != "")
                    Picasso.with(getActivity()).load("http://"+user.getProfilePicture()).transform(new RoundedCornersTransformation(10,10)).into(binding.ownerPicture); //Picasso needs "http://"

                LatLng pos = viewModel.getLatLng();
                if(pos != null){
                    mMap.addMarker(new MarkerOptions().position(pos)
                            .title(getArguments().getString("username")));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 10));
                }
            }
        });
    }

    public void onResetPassword(View view){
        DialogFragment cancelConfirmDiaFrag = CancelConfirmDiaFrag.newInstance("onResetPassword");
        mFragmentNavigation.showDialogFragment(cancelConfirmDiaFrag);
    }

    public void onLogout(View view){
        getActivity().getSharedPreferences("Sechzehn", 0).edit().clear().apply();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish(); //Finish BottomTabs
        Toast.makeText(getActivity(), "Logged Out", Toast.LENGTH_SHORT).show();
    }

    public void onChangePassword(View view){
        DialogFragment changePasswordDiaFrag = new changePasswordDiaFrag();
        mFragmentNavigation.showDialogFragment(changePasswordDiaFrag);
    }

    public void onChangeEmail(View view){
        DialogFragment changeEmailDiaFrag = new changeEmailDiaFrag();
        mFragmentNavigation.showDialogFragment(changeEmailDiaFrag);
    }
}

