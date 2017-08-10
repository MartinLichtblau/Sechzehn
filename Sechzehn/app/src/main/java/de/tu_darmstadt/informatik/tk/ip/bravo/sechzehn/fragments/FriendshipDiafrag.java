package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;

import android.app.Dialog;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Friendship;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Resource;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.DiafragFriendshipBinding;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.viewModels.UserProfileViewModel;

/**
 * Created by marti on 10.08.2017.
 */

public class FriendshipDiafrag extends DialogFragment implements LifecycleRegistryOwner {
    LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);
    private Friendship.Status status;
    private UserProfileViewModel userProfileVM;
    private DiafragFriendshipBinding binding;

    public static FriendshipDiafrag newInstance() {
        FriendshipDiafrag friendshipDiafrag = new FriendshipDiafrag();
        return friendshipDiafrag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        userProfileVM = ViewModelProviders.of(getParentFragment()).get(UserProfileViewModel.class);
        status = userProfileVM.getUser().getValue().getFriendshipStatus();
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.diafrag_friendship, null, false);
        binding.setFrag(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(binding.getRoot());
        return builder.create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        customizeFragment();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void customizeFragment(){
        if(status == Friendship.Status.NONE){
            getDialog().setTitle("Add "+userProfileVM.getUsername()+" as Friend");
        }else if(status == Friendship.Status.RELATED_CONFIRMED){
            getDialog().setTitle("Abort Friend Request");
        }else if(status == Friendship.Status.RELATING_CONFIRMED){
            getDialog().setTitle("Accept "+userProfileVM.getUsername()+" Friend Request");
            binding.Negative.setVisibility(View.VISIBLE);
        }else if(status == Friendship.Status.CONFIRMED){
            getDialog().setTitle("Unfriend "+userProfileVM.getUsername());
        }else {
            throw new IllegalArgumentException("Invalid Frienship Status");
        }
    }

    public void onCancel(View view){
        dismiss();
    }

    public void onPositive(View view){
        if(status == Friendship.Status.NONE){
            userProfileVM.requestFriendship().observe(this, new Observer<Resource>() {
                @Override
                public void onChanged(@Nullable Resource resource) {
                    feedback(resource);
                }
            });
        }else if(status == Friendship.Status.RELATED_CONFIRMED){
            userProfileVM.deleteFriendship().observe(this, new Observer<Resource>() {
                @Override
                public void onChanged(@Nullable Resource resource) {
                    feedback(resource);
                }
            });
        }else if(status == Friendship.Status.RELATING_CONFIRMED){
            userProfileVM.answerFriendship("CONFIRMED").observe(this, new Observer<Resource>() {
                @Override
                public void onChanged(@Nullable Resource resource) {
                    feedback(resource);
                }
            });
        }else if(status == Friendship.Status.CONFIRMED){
            userProfileVM.deleteFriendship().observe(this, new Observer<Resource>() {
                @Override
                public void onChanged(@Nullable Resource resource) {
                    feedback(resource);
                }
            });
        }else {
            throw new IllegalArgumentException("Invalid Owner DialogFragment Type");
        }
    }

    public void onNegative(View view){
        if(status == Friendship.Status.RELATING_CONFIRMED){
            userProfileVM.answerFriendship("DECLINED").observe(this, new Observer<Resource>() {
                @Override
                public void onChanged(@Nullable Resource resource) {
                    feedback(resource);
                }
            });
        }
    }

    private void feedback(Resource resource){
        userProfileVM.refreshUser();
        //Toast.makeText(getActivity(), resource.message, Toast.LENGTH_SHORT).show();
        dismiss();
    }

    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }
}
