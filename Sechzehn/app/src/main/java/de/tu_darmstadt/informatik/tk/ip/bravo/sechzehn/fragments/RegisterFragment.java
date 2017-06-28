package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;


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
    }
}
