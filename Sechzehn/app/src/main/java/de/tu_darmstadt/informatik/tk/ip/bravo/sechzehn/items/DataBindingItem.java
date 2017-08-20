package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.items;

import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.mikepenz.fastadapter.IClickable;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.BR;

/**
 * Provides support for DataBinding for FastAdapter Items.
 *
 * @param <Data>           The data class which will be bound.
 * @param <Binding>        The data binding of the item.
 * @param <Item>           The item which uses this DataBindingItem.
 * @param <ItemViewHolder> The ViewHolder if the item.
 * @author Alexander Geiß on 18.08.2017.
 */
public abstract class DataBindingItem<
        Data,
        Binding extends ViewDataBinding,
        Item extends DataBindingItem<Data, Binding, Item, ItemViewHolder>,
        ItemViewHolder extends DataBindingItem<Data, Binding, Item, ItemViewHolder>.ViewHolder
        > extends AbstractItem<Item, ItemViewHolder> {


    private Data data;

    /**
     * Creates a new item without data.
     * <p>
     * It is necessary to override {@link #getData()}
     */
    public DataBindingItem() {
    }

    /**
     * Creates a new item with data.
     *
     * @param data The data of the new item.
     */
    public DataBindingItem(Data data) {
        this.data = data;
    }

    /**
     * @return An object representing the current Data.
     */
    protected Data getData() {
        return data;
    }

    /**
     * @return The current Object.
     */
    @SuppressWarnings("unchecked")
    protected Item getSelf() {
        return (Item) this;
    }

    @Override
    public void bindView(ItemViewHolder holder, List<Object> payloads) {
        super.bindView(holder, payloads);
        holder.bind(getSelf(), getData());
    }

    @Override
    public void unbindView(ItemViewHolder holder) {
        super.unbindView(holder);
        holder.bind(null, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DataBindingItem<?, ?, ?, ?> that = (DataBindingItem<?, ?, ?, ?>) o;

        return data != null ? data.equals(that.data) : that.data == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
    }

    protected abstract class ViewHolder extends RecyclerView.ViewHolder {
        private final Binding binding;

        /**
         * Creates a new ViewHolder with
         *
         * @param binding the binding used for this Item.
         */
        public ViewHolder(Binding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * Performs the binding.
         *
         * @param item The item.
         * @param data The data object.
         */
        public final void bind(Item item, Data data) {
            bindItemImplementation(binding, data);
            bindSelf(item);
            binding.executePendingBindings();
        }

        /**
         * Should set the data property of the binding to data.
         * Never call this directly, use always {@link ViewHolder#bind(DataBindingItem, Object)}
         *
         * @param binding The binding.
         * @param data    The data object.
         */
        protected abstract void bindItemImplementation(Binding binding, Data data);

        private void bindSelf(Item self) {
            if (!binding.setVariable(BR.self, self)) {
                Log.w("DataBindingFragment", self.getClass().getName() + ": Self not bound");
            }
        }
    }

}

