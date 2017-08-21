package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.activities.BottomTabsActivity;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.UserToken;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.FragmentLoginBinding;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.ServiceGenerator;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services.LoginService;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.ActionDoneListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services.LoginService.LoginService;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an UserService of this fragment.
 */
public class LoginFragment extends DataBindingFragment<FragmentLoginBinding> {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new UserService of
     * this fragment using the provided parameters.
     *
     * @return A new UserService of fragment LoginFragment.
     */
    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    private User user = new User();

    @Override
    protected FragmentLoginBinding initDataBinding(LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentLoginBinding.inflate(inflater, container, false);
    }

    @Override
    protected void useDataBinding(FragmentLoginBinding binding) {
        binding.setUser(user);
        binding.loginPassword.setOnEditorActionListener(new ActionDoneListener() {
            @Override
            public void onActionDone(TextView v, int actionId, KeyEvent event) {
                login(v);
            }
        });
    }

    public void toRegister(View view) {

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.loginContainer, RegisterFragment.newInstance());
        transaction.commit();
    }

    public void toForgotpassword(View view) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.loginContainer, ForgotPwFragment.newInstance());
        transaction.commit();
    }

    public void login(View view) {
        LoginService.login(user).enqueue(new Callback<UserToken>() {
            @Override
            public void onResponse(Call<UserToken> call, Response<UserToken> response) {
                if (response.isSuccessful()) {
                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                            .putString("JWT", response.body().token)
                            .putString("ownername", response.body().user.getUsername()).apply(); // > https://stackoverflow.com/questions/28096876/how-to-store-multiple-key-value-pairs-in-shared-preferences
                    Intent intent = new Intent(getActivity(), BottomTabsActivity.class);
                    startActivity(intent);
                    getActivity().finish(); //Finish LoginFragment or it bugs and resources
                } else {
                    binding.loginEmail.setError("Email or password wrong.");
                    binding.loginPassword.setError("Email or password wrong.");
                }
            }

            @Override
            public void onFailure(Call<UserToken> call, Throwable t) {
                Toast.makeText(getActivity(), "Connectivity error!", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
