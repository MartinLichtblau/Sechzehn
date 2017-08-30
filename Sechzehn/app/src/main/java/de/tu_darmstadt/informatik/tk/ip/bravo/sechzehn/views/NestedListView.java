package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.views;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.BR;

/**
 * @author Alexander Geiß on 22.08.2017.
 */

public class NestedListView extends LinearLayout {

    public NestedListView(Context context) {
        super(context);
    }

    public NestedListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public NestedListView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public NestedListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * Add an item
     * @param item the item
     */
    public void add(Item item) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(item.getLayoutRes(), this, false);
        item.bind(v);
        addView(v);
    }

    /**
     * Add item at position
     * @param position the position
     * @param item item
     */
    public void add(int position, Item item) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(item.getLayoutRes(), this, false);
        item.bind(v);
        addView(v,position);
    }

    /**
     * Add an item list.
     * @param items list of items.
     */
    public void add(Iterable<? extends Item> items) {
        for (Item i : items) {
            add(i);
        }
    }

    /**
     * Item interface
     */
    public interface Item {
        /**
         *
         * @return The items layout id.
         */
        @LayoutRes
        int getLayoutRes();

        /**
         * Called when the item should bind to a view.
         * @param view
         */
        void bind(View view);
    }

    /**
     * Helper calss for DataBinding Items.
     * @param <Binding> the binding of the item.
     */
    public static abstract class ItemDB<Binding extends ViewDataBinding> implements Item {
        protected Binding binding;

        public void bind(View view) {
            binding = initDataBinding(view);
            bindSelf(binding);

        }

        private void bindSelf(Binding binding) {
            if (!binding.setVariable(BR.self, this)) {
                Log.w("NestedListView.ItemDB", this.getClass().getName() + ": Self not bound");
            }
        }

        protected abstract Binding initDataBinding(View view);

        protected void useDataBinding(Binding binding) {
        }


    }

}
