package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.BaseFragment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.Api;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.UserToken;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegisterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterFragment extends BaseFragment {


    public RegisterFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment RegisterFragment.
     */
    public static RegisterFragment newInstance() {
        return new RegisterFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        initView(view);
        return view;
    }

    protected void initView(View v) {
        v.findViewById(R.id.registerToLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragNavController().popFragment();
            }
        });
        v.findViewById(R.id.registerButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User u = new User();
                u.email=getTextFrom(v,R.id.registerEmail);
                u.password=getTextFrom(v,R.id.registerPassword);
                u.passwordConfirmation=getTextFrom(v,R.id.registerPasswordConfirmation);
                try {
                   Response<UserToken> response=  Api.getInstance().createUser(u).execute();
                    if(response.isSuccessful()){
                     response.body().token
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
