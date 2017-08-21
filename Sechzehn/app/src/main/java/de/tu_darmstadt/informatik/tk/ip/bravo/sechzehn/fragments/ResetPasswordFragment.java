package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.FragmentResetPasswordBinding;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.ActionDoneListener;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.ApiMessage;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.DefaultCallback;
import retrofit2.Call;
import retrofit2.Response;

import static de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services.LoginService.LoginService;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ResetPasswordFragment#newInstance} factory method to
 * create an UserService of this fragment.
 */
public class ResetPasswordFragment extends DataBindingFragment<FragmentResetPasswordBinding> {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "token";

    User user = new User();


    private String token;


    public ResetPasswordFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new UserService of
     * this fragment using the provided parameters.
     *
     * @return A new UserService of fragment ResetPasswordFragment.
     */
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
    protected void useDataBinding(FragmentResetPasswordBinding binding, Bundle savedInstanceState) {
        binding.setUser(user);
        binding.setSelf(this);
        binding.resetPasswordPasswordConfirmation.setOnEditorActionListener(new ActionDoneListener() {
            @Override
            public void onActionDone(TextView v, int actionId, KeyEvent event) {
                confirm(v);
            }
        });
    }

    public void confirm(View view) {
        if (!binding.resetPasswordPassword.getText().toString().equals(binding.resetPasswordPasswordConfirmation.getText().toString())) {
            binding.resetPasswordPasswordConfirmation.setError("Passwords do not match.");
            return;
        } else {
            binding.resetPasswordPasswordConfirmation.setError(null);
        }

        LoginService.confirmResetPassword(token, user).enqueue(new DefaultCallback<ApiMessage>(getActivity()) {
            @Override
            public void onResponse(Call<ApiMessage> call, Response<ApiMessage> response) {
                if (response.isSuccessful()) {
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.loginContainer, LoginFragment.newInstance()).commitAllowingStateLoss();
                    Toast.makeText(getActivity(), "new password set successful", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
