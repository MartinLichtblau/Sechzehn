package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.items;


import android.content.Context;
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
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.ItemUserBinding;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.DataBindingFragment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.MessageFragment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.SzUtils;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * Created by marti on 19.07.2017.
 */

public class UserItem extends DataBindingItem<User, ItemUserBinding, UserItem, UserItem.ViewHolder> {

     private AnimatedFragNavController fragNavController;

    public static UserItem create(User u, AnimatedFragNavController fragNavController) {
        return new UserItem(u,fragNavController);
    }

    public UserItem(User user, AnimatedFragNavController fragNavController) {
        super(user);
        this.fragNavController = fragNavController;
    }

    public User getUser() {
        return getData();
    }

    @Override
    public int getType() {
        return R.id.class_user_item;
    }

    //The layout to be used for this type of item
    @Override
    public int getLayoutRes() {
        return R.layout.item_user;
    }

    public void onMessageClick(View v){
        fragNavController.pushFragment(MessageFragment.newInstance(getUser().getUsername()));
    }

    //Init the viewHolder for this Item
    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(ItemUserBinding.bind(v));
    }

    protected class ViewHolder extends DataBindingItem<User, ItemUserBinding, UserItem, UserItem.ViewHolder>.ViewHolder {

        protected ImageView message;

        /**
         * Creates a new ViewHolder with
         *
         * @param binding the binding used for this Item.
         */
        public ViewHolder(ItemUserBinding binding) {
            super(binding);
            this.message = binding.itemUserMessage;
        }

        @Override
        protected void bindItemImplementation(ItemUserBinding binding, User user) {
            binding.setUser(user);
        }
    }
}