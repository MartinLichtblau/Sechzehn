package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;


import android.app.Fragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.FragmentForgotPwBinding;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.ServiceGenerator;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.Services.LoginService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
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

    @Override
    protected void useDataBinding(FragmentForgotPwBinding binding) {
        binding.setUser(user);
    }

    public void confirmReset(View view) {
        ServiceGenerator.createService(LoginService.class).resetPassword(user).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getActivity(), "Email sent", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Toast.makeText(getActivity(), "Connectivity error!", Toast.LENGTH_SHORT).show();
            }
        });

    }
}


