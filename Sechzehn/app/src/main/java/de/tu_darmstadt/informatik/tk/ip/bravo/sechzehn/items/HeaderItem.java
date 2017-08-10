package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.items;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.textservice.TextInfo;
import android.widget.TextView;

import com.google.gson.annotations.SerializedName;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;

/**
 * @author Alexander Geiß on 20.07.2017.
 */

public class HeaderItem extends AbstractItem<HeaderItem, HeaderItem.ViewHolder> {
    public String title;

    public HeaderItem() {
    }

    public HeaderItem(String title) {
        this.title = title;
    }


    @Override
    public int getType() {
        return R.id.class_header_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_header;
    }

    //The logic to bind your data to the view
    @Override
    public void bindView(ViewHolder viewHolder, List<Object> payloads) {
        //call super so the selection is already handled for you
        super.bindView(viewHolder, payloads);

        //bind our data
        viewHolder.title.setText(title);
    }

    //reset the view here (this is an optional method, but recommended)
    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);
        holder.title.setText(null);
    }


    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.item_header_title);
        }
    }
}
