package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.items;


import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import com.mikepenz.fastadapter.items.AbstractItem;
import java.util.List;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;

/**
 * Created by marti on 19.07.2017.
 */

public class FriendItem extends AbstractItem<FriendItem, FriendItem.ViewHolder> {
    private final int ID = 1;
    public String name = "TestName";
    public String description = "TestDescription";

    //The unique ID for this type of item
    @SuppressWarnings("ResourceType") //@TODO //should be a defined int ID unique for this type of item
    @Override
    public int getType() {
        return ID;
    }

    //The layout to be used for this type of item
    @Override
    public int getLayoutRes() {
        return R.layout.item_friend;
    }

    //The logic to bind your data to the view
    @Override
    public void bindView(ViewHolder viewHolder, List<Object> payloads) {
        //call super so the selection is already handled for you
        super.bindView(viewHolder, payloads);

        //bind our data
        //set the text for the name
        viewHolder.name.setText(name);
        //set the text for the description or hide
        viewHolder.description.setText(description);
    }

    //reset the view here (this is an optional method, but recommended)
    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);
        holder.name.setText(null);
        holder.description.setText(null);
    }

    //Init the viewHolder for this Item
    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    //The viewHolder used for this item. This viewHolder is always reused by the RecyclerView so scrolling is blazing fast
    protected static class ViewHolder extends RecyclerView.ViewHolder {
        protected TextView name;
        protected TextView description;

        public ViewHolder(View view) {
            super(view);
            this.name = (TextView) view.findViewById(R.id.material_drawer_name);
            this.description = (TextView) view.findViewById(R.id.material_drawer_description);
        }
    }
}