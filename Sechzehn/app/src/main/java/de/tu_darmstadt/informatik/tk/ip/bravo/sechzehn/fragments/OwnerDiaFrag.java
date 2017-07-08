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
import android.widget.Toast;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.activities.BottomTabsActivity;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.viewModels.OwnerViewModel;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.DiafragOwnerBinding;

/**
 * Created by marti on 07.07.2017.
 */

public class OwnerDiaFrag extends DialogFragment implements LifecycleRegistryOwner {
    LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);
    DiafragOwnerBinding binding;
    private OwnerViewModel ownerVM;
    private User owner;
    private String type;
    public String newEmail;
    public String currentPassword;
    public String newPassword;


    public static OwnerDiaFrag newInstance(String type) {
        OwnerDiaFrag ownerDiaFrag = new OwnerDiaFrag();
        Bundle args = new Bundle();
        args.putString("type", type);
        ownerDiaFrag.setArguments(args);
        return ownerDiaFrag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        type = getArguments().getString("type");
        ownerVM = ViewModelProviders.of(getActivity()).get(OwnerViewModel.class);
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.diafrag_owner, null, false);
        binding.setFrag(this);
        binding.setUser(owner);

        customizeFragment();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(binding.getRoot());
        builder.setMessage("Are you sure?");
        return builder.create();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding.setUser(ownerVM.getOwner().getValue());
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void onCancel(View view){
        dismiss();
    }

    public void onSubmit(View view){
        if(type == "editProfile"){
            ownerVM.editProfile(owner).observe(this, new Observer<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean close) {
                    dismiss();
                }
            });

        }else if(type == "logout"){
            Toast.makeText(getActivity(), "Logging Out", Toast.LENGTH_SHORT).show();
            ((BottomTabsActivity)getActivity()).factoryReset();

        }else if(type == "changePassword") {
            ownerVM.changePassword(currentPassword, newPassword).observe(this, new Observer<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean close) {
                    dismiss();
                }
            });

        }else if(type == "resetPassword") {
            ownerVM.resetPassword().observe(this, new Observer<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean close) {
                    dismiss();
                }
            });

        }else if(type == "changeEmail") {
            ownerVM.changeEmail(currentPassword, newEmail).observe(this, new Observer<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean close) {
                    dismiss();
                }
            });

        }else if(type == "deleteAccount"){
            ownerVM.deleteAccount(currentPassword).observe(this, new Observer<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean aBoolean) {
                    ((BottomTabsActivity)getActivity()).factoryReset();
                }
            });
        }
    }

    private void customizeFragment(){
        if(type == "editProfile"){
/*            owner = ownerVM.getOwner().getValue();
            ownerVM.setToast(owner.toString());
            binding.setUser(owner);*/
            binding.realname.setVisibility(View.VISIBLE);
            binding.age.setVisibility(View.VISIBLE);
            binding.address.setVisibility(View.VISIBLE);
            binding.incognitoSwitch.setVisibility(View.VISIBLE);
        }else if(type == "logout"){
        }else if(type == "changePassword") {
            binding.currentPassword.setVisibility(View.VISIBLE);
            binding.newPassword.setVisibility(View.VISIBLE);
        }else if(type == "resetPassword") {
        }else if(type == "changeEmail") {
            binding.newEmail.setVisibility(View.VISIBLE);
            binding.currentPassword.setVisibility(View.VISIBLE);
        }else if(type == "deleteAccount"){
            binding.currentPassword.setVisibility(View.VISIBLE);
        }else {
            throw new IllegalArgumentException("Invalid Owner DialogFragment Type");
        }
    }

    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }
}
