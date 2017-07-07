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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.DiafragChangePasswordBinding;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.viewModels.OwnerViewModel;

/**
 * Created by marti on 07.07.2017.
 */

public class changePasswordDiaFrag extends DialogFragment implements LifecycleRegistryOwner {
    LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);
    DiafragChangePasswordBinding binding;
    private OwnerViewModel ownerVM;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ownerVM = ViewModelProviders.of(getActivity()).get(OwnerViewModel.class);
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.diafrag_change_password, null, false);
        binding.setFrag(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(binding.getRoot());
        builder.setMessage("Change Password");
        return builder.create();
    }

    public void onCancel(View view){
        dismiss();
    }

    public void onSubmit(View view){
        ownerVM.changePassword(binding.oldPassword.getText().toString(), binding.newPassword.getText().toString()).observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String msg) {
                Log.d(getActivity().toString(),msg);
                Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });
    }

    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }
}
