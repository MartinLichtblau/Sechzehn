package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.items;

import android.view.View;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.AnimatedFragNavController;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services.VenueService;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.DefaultCallback;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.views.NestedListView;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.venue.Comment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.ItemCommentBinding;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.UserProfileFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services.VenueService.VenueService;

/**
 * @author Alexander Geiß on 21.08.2017.
 */

public class CommentItem extends DataBindingItem<Comment, ItemCommentBinding, CommentItem, CommentItem.ViewHolder> implements NestedListView.Item {
    /**
     * Binding for NestedListView
     */
    private ItemCommentBinding binding;
    private AnimatedFragNavController fragNavController;

    public CommentItem(Comment comment, AnimatedFragNavController fragNavController) {
        super(comment);
        this.fragNavController = fragNavController;
    }

    @Override
    public int getType() {
        return 0;
    }

    /**
     * Returns the corresponding layout.
     * Shared between {@link com.mikepenz.fastadapter.items.AbstractItem} and {@link
     * NestedListView.Item}.
     *
     * @return the corresponding layout.
     */
    @Override
    public int getLayoutRes() {
        return R.layout.item_comment;
    }

    public void showUserProfile(View v) {
        fragNavController.pushFragment(
                UserProfileFragment.newInstance(
                        getData().user.getUsername()
                )
        );
    }

    /**
     * Binding for {@link NestedListView.Item}
     *
     * @param view The view to be bound.
     */
    @Override
    public void bind(View view) {
        binding = ItemCommentBinding.bind(view);
        binding.setSelf(getSelf());
        binding.setComment(getData());
    }

    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(ItemCommentBinding.bind(v));
    }

    public class ViewHolder extends DataBindingItem<Comment, ItemCommentBinding, CommentItem, CommentItem.ViewHolder>.ViewHolder {
        /**
         * Creates a new ViewHolder with
         *
         * @param binding the binding used for this Item.
         */
        public ViewHolder(ItemCommentBinding binding) {
            super(binding);
        }

        @Override
        protected void bindItemImplementation(ItemCommentBinding binding, Comment comment) {
            binding.setComment(comment);
        }
    }

    public void voteUp(View v) {
        VenueService.rateComment(getData().id, new Comment(1)).enqueue(new DefaultCallback<Comment>(v.getContext()) {
            @Override
            public void onResponse(Call<Comment> call, Response<Comment> response) {
                // TODO
            }
        });
    }

    public void voteDown(View v) {
        VenueService.rateComment(getData().id, new Comment(1)).enqueue(new DefaultCallback<Comment>(v.getContext()) {
            @Override
            public void onResponse(Call<Comment> call, Response<Comment> response) {
                // TODO
            }
        });
    }
}
