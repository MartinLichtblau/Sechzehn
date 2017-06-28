package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;

import android.arch.lifecycle.LifecycleFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.tasks.RuntimeExecutionException;
import com.ncapdevi.fragnav.FragNavController;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.activities.BottomTabsActivity;

/**
 * Created by niccapdevila on 3/26/16.
 */
public abstract class BaseFragment extends LifecycleFragment {

    public static final String ARGS_INSTANCE = "de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.argsInstance";

    FragNavController mFragmentNavigation;
    int mInt = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(layoutID(), container, false);
        initView(view);
        return view;
    }

    protected int layoutID() {
        throw new RuntimeException("The child class must provide an layoutID or overwrite onCreateView().");
    }

    protected void initView(View view) {
    }

    public FragNavController fragNavController() {
        if(mFragmentNavigation==null){
            throw new NullPointerException("The FragNavController is not available.");
        }
        return mFragmentNavigation;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof NavController) {
            mFragmentNavigation = ((NavController) context).getNavController();
        }
    }

    public String getTextFrom(View v,int id){
        View v2 = v.findViewById(id);
        if(!(v.findViewById(id) instanceof TextInputEditText)){
           //throw new RuntimeException("The ID must refer to a TextInputEditText.") ;
        }
        return ((TextInputEditText)v.findViewById(id)).getText().toString();
    }

    public interface NavController {
        FragNavController getNavController();
    }

}
