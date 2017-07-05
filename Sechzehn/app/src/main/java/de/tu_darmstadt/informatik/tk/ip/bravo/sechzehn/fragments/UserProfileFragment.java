package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.FragmentProfileUserBinding;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.viewModels.UserProfileViewModel;

/**
 * Created by niccapdevila on 3/26/16.
 */
public class UserProfileFragment extends BaseFragment {
    private Switch userProfileEditSwitch;
    private ImageView addFriendButton;
    private ImageView userOptionsButton;
    private FloatingActionButton userProfileSaveFab;

    private static final String USER_ID = "uid";
    private UserProfileViewModel viewModel;

    Integer i = 0;

    public static UserProfileFragment newInstance(String userId) {
        Bundle bundle = new Bundle();
        bundle.putString(USER_ID, userId);
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
        //View view = inflater.inflate(R.layout.fragment_profile_user, container, false);
        final FragmentProfileUserBinding binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_profile_user, container, false);
        binding.setUser(new User());

        addFriendButton = binding.userAddFriend;
        userOptionsButton = binding.userOptions;

        viewModel = ViewModelProviders.of(this).get(UserProfileViewModel.class);
        if (viewModel.getUser().getValue() == null)
            viewModel.initUser("parru");
        viewModel.getUser().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                binding.setUser(user);
            }
        });

        return binding.getRoot();
    }


    @Override
    public void onStart() {
        super.onStart();

        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i++;
                String username;
                if(i<2)
                    username = "bekgi";
                else
                    username = "socac";
                viewModel.initUser(username);
                //Toast.makeText(getActivity(), "onClick: " + username + " " + viewModel.getUser().hasObservers(), Toast.LENGTH_SHORT).show();
            }
        });

        userOptionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }


}

