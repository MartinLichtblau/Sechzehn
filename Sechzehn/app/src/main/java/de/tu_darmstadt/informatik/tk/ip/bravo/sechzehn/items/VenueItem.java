package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.items;


import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.mikepenz.fastadapter.items.AbstractItem;
import com.ncapdevi.fragnav.FragNavController;
import com.squareup.picasso.Downloader;
import com.squareup.picasso.Picasso;

import java.util.LinkedList;
import java.util.List;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.AnimatedFragNavController;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Venue;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.ItemUserBinding;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.ItemVenueBinding;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.DataBindingFragment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.MessageFragment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.VenueFragment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.SzUtils;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * Created by marti on 19.07.2017.
 */

public class VenueItem extends DataBindingItem<Venue, ItemVenueBinding, VenueItem, VenueItem.ViewHolder> {
    private AnimatedFragNavController fragNavController;
    public Bitmap pinIcon;

    public static VenueItem create(Venue venue, Bitmap pinIcon, AnimatedFragNavController fragNavController) {
        return new VenueItem(venue, pinIcon, fragNavController);
    }

    public VenueItem(Venue venue, Bitmap pinIcon, AnimatedFragNavController fragNavController) {
        super(venue);
        this.fragNavController = fragNavController;
        this.pinIcon = pinIcon;

    }

    public Venue getVenue() {
        return getData();
    }

    @Override
    public int getType() {
        return R.id.class_venue_item;
    }

    //The layout to be used for this type of item
    @Override
    public int getLayoutRes() {
        return R.layout.item_venue;
    }

    public void onMessageClick(View v){
        fragNavController.pushFragment(VenueFragment.newInstance(getVenue().id));
    }

    //Init the viewHolder for this Item
    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(ItemVenueBinding.bind(v));
    }

    protected class ViewHolder extends DataBindingItem<Venue, ItemVenueBinding, VenueItem, VenueItem.ViewHolder>.ViewHolder {

        /**
         * Creates a new ViewHolder with
         *
         * @param binding the binding used for this Item.
         */
        public ViewHolder(ItemVenueBinding binding) {
            super(binding);
            /*binding.itemVenueIcon.setImageBitmap(pinIcon);*/
        }

        @Override
        protected void bindItemImplementation(ItemVenueBinding binding, Venue venue) {
            binding.setVenue(venue);
            //binding.itemVenueIcon.setImageBitmap(pinIcon);
        }
    }
}