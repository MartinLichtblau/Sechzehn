package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;

import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.viewModels.UserProfileViewModel;

/**
 * Created by niccapdevila on 3/26/16.
 */
public class ProfileFragment extends BaseFragment {
    private Switch userProfileEditSwitch;
    private ImageView addFriendButton;
    private ImageView userOptionsButton;
    private FloatingActionButton userProfileSaveFab;

    private static final String USER_ID = "uid";
    private UserProfileViewModel viewModel;

    public static ProfileFragment newInstance(String userId) {
        Bundle bundle = new Bundle();
        bundle.putString(USER_ID, userId);
        ProfileFragment fragment = new ProfileFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);



        viewModel = ViewModelProviders.of(this).get(UserProfileViewModel.class);
        viewModel.init(USER_ID);
        viewModel.getUser().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                fillInUser(user);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        userProfileEditSwitch = (Switch) view.findViewById(R.id.user_profile_edit);
        addFriendButton = (ImageView) view.findViewById(R.id.user_addFriend);
        userOptionsButton = (ImageView) view.findViewById(R.id.user_options);
        userProfileSaveFab = (FloatingActionButton) view.findViewById(R.id.user_profile_save);

        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        userProfileEditSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(userProfileEditSwitch.isChecked()){
                    editableProfile(true);
                }else{
                    editableProfile(false);
                }
            }
        });

        userProfileSaveFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Saved", Toast.LENGTH_SHORT).show();
                editableProfile(false);
            }
        });

        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        userOptionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    public void editableProfile(Boolean status){
        getView().findViewById(R.id.user_profile_name_edit).setEnabled(status);
        getView().findViewById(R.id.user_profile_email_edit).setEnabled(status);
        getView().findViewById(R.id.user_profile_age_edit).setEnabled(status);
        getView().findViewById(R.id.user_profile_address_edit).setEnabled(status);

        userProfileEditSwitch.setChecked(status);
        if (status){
            userProfileSaveFab.show();
        }
        else{
            userProfileSaveFab.hide();
        }
    }

    private void fillInUser(User user){
        Toast.makeText(getActivity(), "Linked to user: " + user.getUsername(), Toast.LENGTH_SHORT).show();
        ((TextInputEditText) this.getView().findViewById(R.id.user_profile_name_edit)).setText(user.getUsername());
    }
}

