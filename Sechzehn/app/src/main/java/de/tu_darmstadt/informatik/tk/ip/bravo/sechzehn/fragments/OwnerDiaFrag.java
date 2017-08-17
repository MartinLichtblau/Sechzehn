package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;

import android.app.DatePickerDialog;
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
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Toast;

import java.util.Calendar;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.activities.BottomTabsActivity;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.SzUtils;
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
        ownerVM = BottomTabsActivity.getOwnerViewModel();
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.diafrag_owner, null, false);
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

    public void changeBirthday(View view){
        final Calendar newCal = Calendar.getInstance();
        String dob = owner.getDateOfBirth();
        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                newCal.set(Calendar.YEAR, year);
                newCal.set(Calendar.MONTH, monthOfYear);
                newCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                owner.setDateOfBirth(SzUtils.calToTimestamp(newCal));
                binding.setUser(owner);
                Log.d("onDateSet", year+"-"+monthOfYear+"-"+dayOfMonth);
            }
        };

        if(dob == null){
            //If no date was ever set
            new DatePickerDialog(getContext(),date,1990,11,30).show();
        }else{
            //If some date is already set
            Calendar oldCal = SzUtils.timestampToCal(owner.getDateOfBirth());
            new DatePickerDialog(getContext(),date,
                    oldCal.get(Calendar.YEAR),
                    oldCal.get(Calendar.MONTH),
                    oldCal.get(Calendar.DAY_OF_MONTH)).show();
        }
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
            Toast.makeText(getActivity(), "Logged Out", Toast.LENGTH_SHORT).show();
            ((BottomTabsActivity)getActivity()).factoryReset();

        }else if(type == "changePassword") {
            if (TextUtils.equals(binding.newPasswordEdit.getText(), binding.newPasswordConfirmEdit.getText())) {
                binding.newPasswordEdit.setError(null);
                binding.newPasswordConfirmEdit.setError(null);
                ownerVM.changePassword(currentPassword, newPassword).observe(this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(@Nullable Boolean close) {
                        dismiss();
                    }
                });
            }else{
                binding.newPasswordEdit.setError("Passwords do not match.");
                binding.newPasswordConfirmEdit.setError("Passwords do not match.");
                return;
            }
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
            getDialog().setTitle("Edit Profile");
            owner = ownerVM.getOwner().getValue();
            binding.setUser(owner);
            binding.realname.setVisibility(View.VISIBLE);
            binding.ownerBirthday.setVisibility(View.VISIBLE);
            binding.address.setVisibility(View.VISIBLE);
            binding.incognitoSwitch.setVisibility(View.VISIBLE);
        }else if(type == "logout"){
            getDialog().setTitle("Logout");
        }else if(type == "changePassword") {
            getDialog().setTitle("Change Password");
            binding.currentPassword.setVisibility(View.VISIBLE);
            binding.newPassword.setVisibility(View.VISIBLE);
            binding.newPasswordConfirm.setVisibility(View.VISIBLE);
        }else if(type == "resetPassword") {
            getDialog().setTitle("Reset Password");
        }else if(type == "changeEmail") {
            getDialog().setTitle("Change Email");
            binding.newEmail.setVisibility(View.VISIBLE);
            binding.currentPassword.setVisibility(View.VISIBLE);
        }else if(type == "deleteAccount"){
            getDialog().setTitle("Delete Account");
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
