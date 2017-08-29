package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.items;

import android.view.View;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.AnimatedFragNavController;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.venue.Comment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.ItemCommentBinding;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.UserProfileFragment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.DefaultCallback;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.views.NestedListView;
import retrofit2.Call;
import retrofit2.Response;

import static de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services.VenueService.VenueService;

/**
 * @author Alexander Geiß on 21.08.2017.
 */

public class CommentItem implements NestedListView.Item {
    public static final Comment DOWNVOTE = new Comment(-1);
    public static final Comment UPVOTE = new Comment(1);
    /**
     * Binding for NestedListView
     */
    private Comment comment;
    private ItemCommentBinding binding;
    private AnimatedFragNavController fragNavController;

    public CommentItem(Comment comment, AnimatedFragNavController fragNavController) {
        this.comment = comment;
        this.fragNavController = fragNavController;
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
                        comment.user.getUsername()
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
        binding.setSelf(this);
        binding.setComment(comment);
    }


    public void voteUp(View v) {
        deactivateRating();
        VenueService.rateComment(comment.id, UPVOTE).enqueue(new DefaultCallback<Comment>(v.getContext()) {
            @Override
            public void onResponse(Call<Comment> call, Response<Comment> response) {
                if (response.isSuccessful()) {
                    updateRating(response.body());

                }
            }
        });
    }

    private void updateRating(Comment body) {
        comment.rating = body.rating;
        binding.setComment(comment);
    }

    private void deactivateRating() {
        binding.upVote.setEnabled(false);
        binding.downVote.setEnabled(false);
        binding.downVote.setAlpha(0.26f);
        binding.upVote.setAlpha(0.26f);
    }

    public void voteDown(View v) {
        deactivateRating();
        VenueService.rateComment(comment.id, DOWNVOTE).enqueue(new DefaultCallback<Comment>(v.getContext()) {
            @Override
            public void onResponse(Call<Comment> call, Response<Comment> response) {
                if (response.isSuccessful()) {
                    updateRating(response.body());
                }
            }

        });
    }

    public static String formatRating(Integer rating) {
        if (rating == null) return "0";
        return rating.toString();
    }
}
