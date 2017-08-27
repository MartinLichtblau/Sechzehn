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
    private List<Item> items = new ArrayList<>();

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

    public void add(Item item) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(item.getLayoutRes(), this, false);
        item.bind(v);
        items.add(item);
        addView(v);
    }

    public void add(Iterable<? extends Item> items) {
        for (Item i : items) {
            add(i);
        }
    }

    public interface Item {
        @LayoutRes
        int getLayoutRes();

        void bind(View view);
    }

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
