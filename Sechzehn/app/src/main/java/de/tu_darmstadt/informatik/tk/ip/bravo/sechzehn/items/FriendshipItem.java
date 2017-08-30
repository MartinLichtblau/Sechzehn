package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.items;

import android.view.View;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Friendship;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.ItemFriendshipBinding;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.FriendsFragment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services.FriendshipService;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.DefaultCallback;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Displays a friendship request.
 *
 * @author Alexander Geiß on 11.08.2017.
 */

public class FriendshipItem extends DataBindingItem<Friendship, ItemFriendshipBinding, FriendshipItem, FriendshipItem.ViewHolder> {


    private static final Friendship CONFIRMED_FRIENDSHIP = new Friendship(Friendship.Status.CONFIRMED);
    private static final Friendship DECLINED_FRIENDSHIP = new Friendship(Friendship.Status.DECLINED);
    private final FriendsFragment friendsFragment;

    private DefaultCallback<Friendship> answerFriendshipCallback;

    public FriendshipItem(Friendship friendship, final FriendsFragment friendsFragment) {
        super(friendship);

        this.friendsFragment = friendsFragment;
        /**
         * Update after request answered.
         */
        answerFriendshipCallback = new DefaultCallback<Friendship>(friendsFragment.getActivityEx()) {
            @Override
            public void onResponse(Call<Friendship> call, Response<Friendship> response) {
                friendsFragment.updateDefaultListAsync();
            }
        };
    }

    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(ItemFriendshipBinding.bind(v));
    }

    @Override
    public int getType() {
        return R.id.class_friendship_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_friendship;
    }

    public Friendship getFriendship() {
        return getData();
    }

    /**
     * Accept friendship request.
     *
     * @param v
     */
    public void onAccept(View v) {
        FriendshipService.FriendshipService.answerFriendship(getFriendship().relatedUser.getUsername(), CONFIRMED_FRIENDSHIP).enqueue(answerFriendshipCallback);
    }

    /**
     * Decline friendship request.
     *
     * @param v
     */
    public void onDecline(View v) {
        FriendshipService.FriendshipService.answerFriendship(getFriendship().relatedUser.getUsername(), DECLINED_FRIENDSHIP).enqueue(answerFriendshipCallback);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        FriendshipItem that = (FriendshipItem) o;

        return friendsFragment.equals(that.friendsFragment);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + friendsFragment.hashCode();
        return result;
    }

    public class ViewHolder extends DataBindingItem<Friendship, ItemFriendshipBinding, FriendshipItem, FriendshipItem.ViewHolder>.ViewHolder {

        public ViewHolder(ItemFriendshipBinding binding) {
            super(binding);
        }

        @Override
        protected void bindItemImplementation(ItemFriendshipBinding binding, Friendship data) {
            binding.setFriendship(data);
        }
    }
}
