package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Alexander Geiß on 29.06.2017.
 */

public abstract class DataBindingFragment<Binding extends android.databinding.ViewDataBinding> extends BaseFragment {

    protected Binding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = initDataBinding(inflater, container);
        bindSelf(binding);
        useDataBinding(binding);
        View view = binding.getRoot();
        initView(view);
        return view;
    }

    protected abstract Binding initDataBinding(LayoutInflater inflater, @Nullable ViewGroup container);

    protected void runOnUiThread(Runnable runnable) {
        getActivity().runOnUiThread(runnable);
    }

    protected void useDataBinding(Binding binding) {
    }

    private void bindSelf(Binding binding) {
        try {
            Method setSelfMethod = binding.getClass().getMethod("setSelf", this.getClass());
            setSelfMethod.invoke(binding, this);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            Log.w("DataBindingFragment", e.getMessage());
        }
    }
}
