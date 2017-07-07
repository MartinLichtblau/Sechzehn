package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;

import android.app.Dialog;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
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
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.activities.LoginActivity;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.DiafragCancelConfirmBinding;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.viewModels.OwnerViewModel;

/**
 * Created by marti on 07.07.2017.
 */

public class CancelConfirmDiaFrag extends DialogFragment implements LifecycleRegistryOwner {
    LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);
    DiafragCancelConfirmBinding binding;
    private OwnerViewModel ownerVM;
    private String function;

    public static CancelConfirmDiaFrag newInstance(String function) {
        CancelConfirmDiaFrag cancelConfirmDiaFrag = new CancelConfirmDiaFrag();
        Bundle args = new Bundle();
        args.putString("function", function);
        cancelConfirmDiaFrag.setArguments(args);
        return cancelConfirmDiaFrag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        function = getArguments().getString("function");
        ownerVM = ViewModelProviders.of(getActivity()).get(OwnerViewModel.class);
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.diafrag_cancel_confirm, null, false);
        binding.setFrag(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(binding.getRoot());
        builder.setMessage("Are you really sure?");
        return builder.create();
    }

    public void onCancel(View view){
        dismiss();
    }

    public void onSubmit(View view){
        if(function == "onResetPassword"){
            ownerVM.resetPassword().observe(this, new Observer<String>() {
                @Override
                public void onChanged(@Nullable String msg) {
                    Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            });
        }else if(function == "onLogout"){
            getActivity().getSharedPreferences("Sechzehn", 0).edit().clear().apply();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish(); //Finish BottomTabs
            Toast.makeText(getActivity(), "Logged Out", Toast.LENGTH_SHORT).show();
        }



    }

    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }
}
