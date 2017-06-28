package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;


import android.app.Fragment;
import android.view.View;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ForgotPwFragment extends BaseFragment {


    public ForgotPwFragment() {
        // Required empty public constructor
    }

    public static ForgotPwFragment newInstance() {
        return new ForgotPwFragment();
    }

    @Override
    protected int layoutID() {
        return R.layout.fragment_forgot_pw;
    }

    protected void initView(View v) {
        v.findViewById(R.id.forgotPwToLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragNavController().popFragment();
            }
        });
    }

}
