package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.items;

import android.support.annotation.NonNull;
import android.view.View;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.LastChat;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.ItemLastChatBinding;

/**
 * Display a recent chat.
 * @author Alexander Geiß on 19.08.2017.
 */

public class LastChatItem extends DataBindingItem<LastChat, ItemLastChatBinding, LastChatItem, LastChatItem.ViewHolder> {

    public LastChatItem(@NonNull LastChat lastChat) {
        super(lastChat);
    }

    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(ItemLastChatBinding.bind(v));
    }

    @Override
    public int getType() {
        return R.id.class_chat_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_last_chat;
    }

    public String getUsername() {
        return getData().user.getUsername();
    }

    protected class ViewHolder extends DataBindingItem<LastChat, ItemLastChatBinding, LastChatItem, LastChatItem.ViewHolder>.ViewHolder {
        /**
         * Creates a new ViewHolder with
         *
         * @param binding the binding used for this Item.
         */
        public ViewHolder(ItemLastChatBinding binding) {
            super(binding);
        }

        @Override
        protected void bindItemImplementation(ItemLastChatBinding binding, LastChat data) {
            binding.setLastChat(data);
        }
    }
}
