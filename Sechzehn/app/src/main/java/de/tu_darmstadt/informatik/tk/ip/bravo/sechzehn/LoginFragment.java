package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.BaseFragment;


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
    protected void initView(View v) {
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
    }

}
