package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.items;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.mikepenz.fastadapter.items.AbstractItem;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;

/**
 * @author Alexander Geiß on 11.08.2017.
 */

public class FriendshipItem extends AbstractItem<FriendshipItem, FriendshipItem.ViewHolder> {
    @Override
    public ViewHolder getViewHolder(View v) {
        return null;
    }

    @Override
    public int getType() {
        return R.id.class_friendship_item;
    }

    @Override
    public int getLayoutRes() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
