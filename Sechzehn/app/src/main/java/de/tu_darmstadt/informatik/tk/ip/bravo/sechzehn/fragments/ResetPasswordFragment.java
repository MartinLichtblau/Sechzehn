package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.content.Intent;
import android.view.View;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.UserToken;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.BaseFragment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.ForgotPwFragment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.LoginFragment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.RegisterFragment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.ResetPasswordFragment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.ServiceGenerator;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.Services.LoginService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.view.ViewGroup;
import android.widget.Toast;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.FragmentForgotPwBinding;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.ServiceGenerator;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.Services.LoginService;

import java.util.List;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.FragmentResetPasswordBinding;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.ServiceGenerator;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.Services.LoginService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ResetPasswordFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ResetPasswordFragment extends DataBindingFragment<FragmentResetPasswordBinding> {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "token";

    User user = new User();


    // TODO: Rename and change types of parameters
    private String token;


    public ResetPasswordFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ResetPasswordFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ResetPasswordFragment newInstance(String token) {
        ResetPasswordFragment fragment = new ResetPasswordFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, token);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            token = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public FragmentResetPasswordBinding initDataBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentResetPasswordBinding.inflate(inflater, container, false);
    }

    @Override
    protected void useDataBinding(FragmentResetPasswordBinding binding) {
        binding.setUser(user);
        binding.setSelf(this);
    }

public void confirm(View view){

    if (!binding.resetPasswordPassword.getText().toString().equals(binding.resetPasswordPasswordConfirmation.getText().toString())) {
        binding.resetPasswordPasswordConfirmation.setError("Passwords do not match.");
        return;
    } else {
        binding.resetPasswordPasswordConfirmation.setError(null);
    }

                ServiceGenerator.createService(LoginService.class).resetPasswordConfirm(token, user).enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        if (response.isSuccessful()) {
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.loginContainer,LoginFragment.newInstance()).commitAllowingStateLoss();
                            Toast.makeText(getActivity(), "new password set successful", Toast.LENGTH_LONG).show();}
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {
                        Toast.makeText(getActivity(), "Connectivity error.", Toast.LENGTH_SHORT).show();
                    }
                });


    }
}
