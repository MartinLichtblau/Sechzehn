package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.FragmentForgotPwBinding;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.ActionDoneListener;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.ApiMessage;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.DefaultCallback;
import retrofit2.Call;
import retrofit2.Response;

import static de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services.LoginService.LoginService;

/**
 * Provides the user with the aibility to reset his password.
 */
public class ForgotPwFragment extends DataBindingFragment<FragmentForgotPwBinding> {

    User user = new User();

    public ForgotPwFragment() {
        // Required empty public constructor
    }

    public static ForgotPwFragment newInstance() {
        return new ForgotPwFragment();
    }

    @Override
    protected FragmentForgotPwBinding initDataBinding(LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentForgotPwBinding.inflate(inflater, container, false);
    }

    /**
     * Goes back to {@link LoginFragment}
     *
     * @param view not used
     */
    public void backToLogin(View view) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.loginContainer, LoginFragment.newInstance());
        transaction.commit();
    }

    @Override
    protected void useDataBinding(FragmentForgotPwBinding binding, Bundle savedInstanceState) {
        binding.setUser(user);
        binding.forgotPwEmail.setOnEditorActionListener(new ActionDoneListener() {
            @Override
            public void onActionDone(TextView v, int actionId, KeyEvent event) {
                confirmReset(v);
            }
        });
    }

    /**
     * Executes the reset by sending a request to the server.
     *
     * @param view not used
     */
    public void confirmReset(View view) {
        LoginService.requestResetPassword(user).enqueue(new DefaultCallback<ApiMessage>(getActivityEx()) {
            @Override
            public void onResponse(Call<ApiMessage> call, Response<ApiMessage> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getActivity(), "Email sent", Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}


