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
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.MessageFragment;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * Created by marti on 19.07.2017.
 */

public class UserItem extends AbstractItem<UserItem, UserItem.ViewHolder> {

    public User user;
    public AnimatedFragNavController fragNavController;

    public static UserItem create(User u, AnimatedFragNavController fragNavController) {
        UserItem f = new UserItem();
        f.user = u;
        f.fragNavController = fragNavController;
        return f;
    }


    //The unique ID for this type of item
    @SuppressWarnings("ResourceType")

    @Override
    public int getType() {
        return R.id.class_user_item;
    }

    //The layout to be used for this type of item
    @Override
    public int getLayoutRes() {
        return R.layout.item_user;
    }

    private Context context;

    @Override
    public View createView(Context ctx, @Nullable ViewGroup parent) {
        View v = super.createView(ctx, parent);
        context = ctx;
        return v;
    }

    //The logic to bind your data to the view
    @Override
    public void bindView(ViewHolder viewHolder, List<Object> payloads) {
        //call super so the selection is already handled for you
        super.bindView(viewHolder, payloads);

        //bind our data
        //set the text for the realname
        if (user.getRealName() != null) {
            viewHolder.name.setText(user.getRealName());
            //set the text for the username or hide
            viewHolder.username.setText(user.getUsername());
        }else {
            viewHolder.name.setText(user.getUsername());
        }


        String profPic = user.getProfilePicture();
        if (profPic != null) {

            Picasso.with(context)
                    .load(profPic)
                    .placeholder(R.drawable.ic_owner) //Placeholders and error images are not resized and must be fairly small images.
                    .centerCrop().resize(40, 40)
                    .transform(new CropCircleTransformation())
                    .into(viewHolder.profilePicture);
        }
        viewHolder.message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragNavController.pushFragment(MessageFragment.newInstance(user.getUsername()));
            }
        });

    }

    //reset the view here (this is an optional method, but recommended)
    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);
        holder.name.setText(null);
        holder.username.setText(null);
        holder.profilePicture.setImageIcon(null);
        holder.message.setOnClickListener(null);
    }

    public static boolean isChanged(List<UserItem> a, List<User> b) {

        // Check for sizes and nulls
        if ((a.size() != b.size()) || (a == null && b != null) || (a != null && b == null)) {
            return true;
        }

        if (a == null && b == null) return false;

        for (int i = 0; i < a.size(); i++) {
            if (!a.get(i).user.equals(b.get(i))) {
                return true;
            }
        }
        return false;
    }

    //Init the viewHolder for this Item
    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    //The viewHolder used for this item. This viewHolder is always reused by the RecyclerView so scrolling is blazing fast
    protected static class ViewHolder extends RecyclerView.ViewHolder {
        protected TextView name;
        protected TextView username;
        protected ImageView profilePicture;
        protected ImageView message;


        public ViewHolder(View view) {
            super(view);
            this.name = (TextView) view.findViewById(R.id.item_user_realName);
            this.username = (TextView) view.findViewById(R.id.item_user_userName);
            this.profilePicture = (ImageView) view.findViewById(R.id.item_user_profilePicture);
            this.message = (ImageView) view.findViewById(R.id.item_user_message);
        }

    }
}