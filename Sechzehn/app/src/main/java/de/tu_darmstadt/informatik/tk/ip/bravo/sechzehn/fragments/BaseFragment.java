package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ncapdevi.fragnav.FragNavController;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.activities.BottomTabsActivity;

/**
 * Created by niccapdevila on 3/26/16.
 */
public class BaseFragment extends Fragment {
    public static final String ARGS_INSTANCE = "de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.argsInstance";

    Button mButton;
 protected FragNavController mFragmentNavigation;
    int mInt = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mInt = args.getInt(ARGS_INSTANCE);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mButton = (Button) view.findViewById(R.id.button);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BottomTabsActivity) {
            mFragmentNavigation = ((BottomTabsActivity) context).getNavController();
        }
    }

}
