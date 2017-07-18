package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.PicassoEngine;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.FragmentOwnerBinding;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.SzUtils;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.viewModels.OwnerViewModel;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

import static android.app.Activity.RESULT_OK;

public class OwnerFragment extends BaseFragment implements OnMapReadyCallback {
    private FragmentOwnerBinding binding;
    private OwnerViewModel viewModel;
    private SupportMapFragment mapFragment;
    GoogleMap map;
    User owner;

    public static OwnerFragment newInstance() {
        OwnerFragment fragment = new OwnerFragment();
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //Get OwnerViewModel from everywhere like below or like
        // ((BottomTabsActivity)getActivity()).getOwnerViewModel(); // > https://stackoverflow.com/questions/12659747/call-an-activity-method-from-a-fragment
        viewModel = ViewModelProviders.of(getActivity()).get(OwnerViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_owner, container, false);
        binding.setFrag(this); //Most important to actually bind the variables specified in layout.xml | The owner binding goes in ownerSetup() triggered by async map
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        updateOwner();
        return binding.getRoot();
    }

    @Override
    public void onPause() {
        super.onPause();
        //Important or map may crash app
        getChildFragmentManager().beginTransaction()
                .remove(mapFragment)
                .commit();
        map = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Correct usage of maps fragment > https://stackoverflow.com/a/33525515/3965610
        map = googleMap;
        updateOwner();
    }

    private void updateOwner(){
        viewModel.getOwner().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                /*Gets updates on change and updates the UI*/
                owner = user;
                binding.setUser(owner);

                Picasso.with(getActivity())
                        .load(user.getProfilePicture()) //Picasso needs "http://" or "https://" url
                        .placeholder(R.drawable.ic_portrait) //Placeholders and error images are not resized and must be fairly small images.
                        .error(R.drawable.ic_portrait)
                        //.centerCrop().resize(256,256) not neccessary since we do that for each uploaded img by default
                        .transform(new RoundedCornersTransformation(50,20))
                        .into(binding.ownerPicture);

                if(map != null){
                    LatLng pos = viewModel.getLatLng();
                    if(pos != null){
                        map.addMarker(new MarkerOptions().position(pos)
                                .title(viewModel.getOwnername()));
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 10));
                    }
                }
            }
        });
    }

    public void editProfile(View view){
        DialogFragment ownerDiaFrag = OwnerDiaFrag.newInstance("editProfile");
        mFragmentNavigation.showDialogFragment(ownerDiaFrag);
    }

    public void logout(View view){
        DialogFragment ownerDiaFrag = OwnerDiaFrag.newInstance("logout");
        mFragmentNavigation.showDialogFragment(ownerDiaFrag);
    }

    public void resetPassword(View view){
        DialogFragment ownerDiaFrag = OwnerDiaFrag.newInstance("resetPassword");
        mFragmentNavigation.showDialogFragment(ownerDiaFrag);
    }

    public void changePassword(View view){
        DialogFragment ownerDiaFrag = OwnerDiaFrag.newInstance("changePassword");
        mFragmentNavigation.showDialogFragment(ownerDiaFrag);
    }

    public void changeEmail(View view){
        DialogFragment ownerDiaFrag = OwnerDiaFrag.newInstance("changeEmail");
        mFragmentNavigation.showDialogFragment(ownerDiaFrag);
    }

    public void deleteAccount(View view){
        DialogFragment ownerDiaFrag = OwnerDiaFrag.newInstance("deleteAccount");
        mFragmentNavigation.showDialogFragment(ownerDiaFrag);
    }

    public void changePicture(View view){
        Matisse.from(OwnerFragment.this)
                .choose(MimeType.allOf())
                .maxSelectable(1)
                .countable(false)
                //.capture(true) // > https://github.com/zhihu/Matisse/issues/65
                .imageEngine(new PicassoEngine())
                .forResult(1);
        //Result receive in @onActivityResult
    }

    public String getAge(){
        return SzUtils.getAge(SzUtils.timestampToCal(owner.getDateOfBirth()));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Uri uri = Matisse.obtainResult(data).get(0);
            SzUtils.centerCropImage(getActivity(),uri).observe(this, new Observer<Bitmap>() {
                @Override
                public void onChanged(@Nullable Bitmap bitmap) {
                    viewModel.changePicture(bitmap);
                }
            });
        }
    }


}

