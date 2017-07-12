package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;

/**
 * Created by niccapdevila on 3/26/16.
 */
public class FriendsFragment extends BaseFragment {

    private Button mButton;



    public static FriendsFragment newInstance(int instance) {
        Bundle args = new Bundle();
        args.putInt(ARGS_INSTANCE, instance);
        FriendsFragment fragment = new FriendsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int layoutID() {
        return R.layout.fragment_main;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mInt = args.getInt(ARGS_INSTANCE);
        }
    }
    protected void initView(View view){
        mButton = (Button) view.findViewById(R.id.button);
    }

    @Override
    public void onStart() {
        super.onStart();
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFragmentNavigation != null) {
                    mFragmentNavigation.pushFragment(FriendsFragment.newInstance(mInt+1));

                }

            }
        });
        mButton.setText(getClass().getSimpleName() + " " + mInt);
    }
}
