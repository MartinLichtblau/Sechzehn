package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;

import android.app.Activity;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.BR;

/**
 * Abstract Fragment for usage with Item
 *
 * @param <Binding> The Item class of the implementing Fragment
 * @author Alexander Geiß on 29.06.2017.
 */
public abstract class DataBindingFragment<Binding extends ViewDataBinding> extends BaseFragment {
    /**
     * The Item
     */
    protected Binding binding;
    /**
     * Containing activity. For {@link #getActivityEx()}.
     */
    private Activity activity;

    /**
     * Initialise the Item and create the view.
     * @param inflater  The LayoutInflater object that can be used to inflate
     *                  any views in the fragment.
     * @param container This is the parent view that the fragment's
     *                  UI should be attached to.
     * @param savedInstanceState The saved instance state, if the fragment is resumed.
     * @return The new view.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = initDataBinding(inflater, container);
        bindSelf(binding);
        useDataBinding(binding, savedInstanceState);
        View view = binding.getRoot();
        initView(view);
        activity = getActivity();

        return view;
    }


    /**
     * This method initialises the Item.
     *
     * @param inflater  The LayoutInflater object that can be used to inflate
     *                  any views in the fragment.
     * @param container This is the parent view that the fragment's
     *                  UI should be attached to.
     * @return The initialised Item.
     */
    protected abstract Binding initDataBinding(LayoutInflater inflater, @Nullable ViewGroup container);

    /**
     * Runs a runnable on the UI thread of the current activity.
     *
     * @param runnable Runnable to run.
     */
    protected void runOnUiThread(Runnable runnable) {
        if (getActivityEx() == null) {
            Log.d("DataBindingFragment", "runOnUiThread: getActivityEx returns null");
            return;
        }
        getActivityEx().runOnUiThread(runnable);
    }

    /**
     * This method can be overridden instead of the onCreateView method
     * to use the Item to initialize contained views.
     *
     * @param binding            The Item of this Fragment.
     * @param savedInstanceState The saved instance state, if the fragment is resumed.
     */
    protected void useDataBinding(Binding binding, Bundle savedInstanceState) {
    }

    /**
     * This method returns the parent activity.
     * It uses caching to reduce errors where getActivity returns null.
     *
     * @return The parent Activity.
     * @throws NullPointerException If activity is null. This makes finding bugs easier.
     */
    public Activity getActivityEx() {
        if (activity == null) {
            activity = getActivity();
        }
        if (activity != null) {
            return activity;
        }
        throw new NullPointerException("Activity is null");
    }

    /**
     * Bind the self variable if present.
     *
     * @param binding The Item.
     */
    private void bindSelf(Binding binding) {
        if (!binding.setVariable(BR.self, this)) {
            Log.w("DataBindingFragment", this.getClass().getName() + ": Self not bound");
        }
    }
}
