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

import java.io.IOException;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.UserToken;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.FragmentRegisterBinding;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.ActionDoneListener;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.DefaultCallback;
import retrofit2.Call;
import retrofit2.Response;

import static de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services.UserService.UserService;


/**
 * Provides the user with the registration screen.
 */
public class RegisterFragment extends DataBindingFragment<FragmentRegisterBinding> {


    public RegisterFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new UserService of fragment RegisterFragment.
     */
    public static RegisterFragment newInstance() {
        return new RegisterFragment();
    }

    private User user = new User();


    @Override
    protected FragmentRegisterBinding initDataBinding(LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentRegisterBinding.inflate(inflater, container, false);
    }

    public void backToLogin(View view) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.loginContainer, LoginFragment.newInstance());
        transaction.commit();
    }

    @Override
    protected void useDataBinding(FragmentRegisterBinding binding, Bundle savedInstanceState) {
        binding.setUser(user);
        binding.registerPasswordConfirmation.setOnEditorActionListener(new ActionDoneListener() {
            @Override
            public void onActionDone(TextView v, int actionId, KeyEvent event) {
                register(v);
            }
        });
    }

    /**
     * handles the registration process. Account is created and email is sent if credentials are
     * valid.
     *
     * @param v
     */
    public void register(View v) {
        if (!binding.registerPassword.getText().toString().equals(binding.registerPasswordConfirmation.getText().toString())) {
            binding.registerPasswordConfirmation.setError("Passwords do not match.");
            return;
        } else {
            binding.registerPasswordConfirmation.setError(null);
        }
        UserService.createUser(user).enqueue(new DefaultCallback<UserToken>(getActivity()) {
            @Override
            public void onResponse(Call<UserToken> call, Response<UserToken> response) {
                if (response.isSuccessful()) {
                    binding.registerEmail.setError(null);
                    binding.registerUsername.setError(null);
                    Toast.makeText(getActivity(), "Registration successful.", Toast.LENGTH_SHORT).show();
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.loginContainer, LoginFragment.newInstance()).commit();
                } else {
                    try {
                        String error = response.errorBody().string();
                        if (error.contains("email")) {
                            binding.registerEmail.setError("Email address already in use!");
                        }
                        if (error.contains("username")) {
                            binding.registerUsername.setError("Username is already taken.");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
