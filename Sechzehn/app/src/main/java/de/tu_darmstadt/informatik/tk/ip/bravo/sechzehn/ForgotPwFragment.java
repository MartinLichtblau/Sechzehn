package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.BaseFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class ForgotPwFragment extends BaseFragment {


    public ForgotPwFragment() {
        // Required empty public constructor
    }

public static ForgotPwFragment newInstance(){
    return new ForgotPwFragment();
}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_forgot_pw, container, false);
        initView(view);
        return view;
    }

    private void initView(View v){
v.findViewById(R.id.forgotPwToLogin).setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        mFragmentNavigation.popFragment();
    }
});
    }

}
