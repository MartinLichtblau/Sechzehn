package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;

import android.arch.lifecycle.LifecycleFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.AnimatedFragNavController;

/**
 * Created by niccapdevila on 3/26/16.
 */
public abstract class BaseFragment extends LifecycleFragment {
    NavController mFragmentNavigation;

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

    public AnimatedFragNavController fragNavController() {
        if(mFragmentNavigation==null){
            throw new NullPointerException("The FragNavController is not available.");
        }
        return mFragmentNavigation.getNavController();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof NavController) {
            mFragmentNavigation = ((NavController) context);
        }
    }

    public interface NavController {
        AnimatedFragNavController getNavController();
    }

}