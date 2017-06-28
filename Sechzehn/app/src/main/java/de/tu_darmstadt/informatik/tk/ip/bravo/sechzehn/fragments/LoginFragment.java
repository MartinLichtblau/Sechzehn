package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;

import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Toast;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.UserToken;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.ServiceGenerator;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.Services.LoginService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends BaseFragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LoginFragment.
     */
    public static LoginFragment newInstance() {
        return new LoginFragment();
    }


    @Override
    protected int layoutID() {
        return R.layout.fragment_login;
    }

    @Override
    protected void initView(final View v) {
        v.findViewById(R.id.loginToRegister).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragNavController().pushFragment(RegisterFragment.newInstance());
            }
        });
        v.findViewById(R.id.loginToForgotpassword).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragNavController().pushFragment(ForgotPwFragment.newInstance());
            }
        });
        v.findViewById(R.id.loginButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User u = new User();
                u.setEmail(getTextFrom(v, R.id.loginEmail));
                u.setPassword(getTextFrom(v, R.id.loginPassword));
                ServiceGenerator.createService(LoginService.class).login(u).enqueue(new Callback<UserToken>() {
                    @Override
                    public void onResponse(Call<UserToken> call, Response<UserToken> response) {
                        if (response.isSuccessful()) {
                            getActivity().getPreferences(0).edit().putString("JWT", response.body().token).commit();
                            fragNavController().popFragment();
                        } else {
                            ((TextInputEditText) v.findViewById(R.id.loginEmail)).setError("Email or password wrong.");
                            ((TextInputEditText) v.findViewById(R.id.loginPassword)).setError("Email or password wrong.");
                        }
                    }

                    @Override
                    public void onFailure(Call<UserToken> call, Throwable t) {
                        Toast.makeText(getActivity(), "Connectivity error!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

}
