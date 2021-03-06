package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.view.View;

import java.io.IOException;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.activities.BottomTabsActivity;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.venue.Comment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.venue.Photo;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.ViewCommentNewBinding;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.DefaultCallback;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.OnTextChangedListener;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.SzUtils;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

import static de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services.VenueService.VenueService;

/**
 * The logic for the view for creating a new Comment
 *
 * @author Alexander Geiß on 28.08.2017.
 */

public class NewCommentView {
    private final Context context;
    private final ViewCommentNewBinding binding;
    private final String venueId;
    private final Listener newCommentListener;
    private final User owner = BottomTabsActivity.getOwnerViewModel().getOwner().getValue();
    private final OnTextChangedListener textChangedListener = new OnTextChangedListener() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!imageLoading) {
                binding.submit.setEnabled(!TextUtils.isEmpty(s));
            }
        }
    };
    ;
    private Comment newComment = new Comment();
    private boolean imageLoading = false;

    /**
     * Creates new comment view logic
     *
     * @param context            The context.
     * @param binding            The binding of the comment view.
     * @param venueId            The venueid of the containing venue.
     * @param newCommentListener an instance of the {@link NewCommentView.Listener} for
     *                           communication with the parent fragment.
     */
    public NewCommentView(@NonNull Context context, @NonNull final ViewCommentNewBinding binding, @NonNull String venueId, @NonNull Listener newCommentListener) {
        this.context = context;
        this.binding = binding;
        this.venueId = venueId;
        this.newCommentListener = newCommentListener;
        binding.setSelf(this);
        binding.setUser(owner);
        binding.setNewComment(newComment);
        binding.body.addTextChangedListener(textChangedListener);
    }

    public interface Listener {
        /**
         * Add a comment to the comment list.
         *
         * @param comment the newly created comment.
         */
        void addComment(Comment comment);

        /**
         * Start a request for a new photo.
         */
        void addPhotoToNewComment();
    }

    /**
     * Submits a new comment.
     *
     * @param v
     */
    public void submitNewComment(View v) {
        newCommentSetEnabled(false);
        binding.imageLoading.setVisibility(View.VISIBLE);
        VenueService.comment(venueId, newComment).enqueue(new DefaultCallback<Comment>(context) {
            @Override
            public void onResponse(Call<Comment> call, Response<Comment> response) {
                if (response.isSuccessful()) {
                    newComment.body = "";
                    newComment.photoId = null;
                    Comment myComment = response.body();
                    myComment.user = owner;
                    binding.setNewComment(newComment);
                    binding.imageView.setImageBitmap(null);
                    binding.imageView.setVisibility(View.GONE);
                    newCommentListener.addComment(myComment);
                }
                onFinally(call);
            }

            @Override
            public void onFinally(Call<Comment> call) {
                newCommentSetEnabled(true);
                binding.imageLoading.setVisibility(View.GONE);
            }
        });


    }

    /**
     * Sets enabled or disabled of the whole view.
     */
    private void newCommentSetEnabled(boolean b) {
        binding.body.setEnabled(b);
        binding.submit.setEnabled(b);
        binding.imageButton.setEnabled(b);
    }

    /**
     * Add a new photo to the comment.
     * Delegated to parent view.
     */
    public void addPhotoToNewComment(View v) {
        newCommentListener.addPhotoToNewComment();
        //Result receive in @onActivityResult
    }

    /**
     * Calculates the scale down image size.
     *
     * @param bitmap  the source bitmap.
     * @param maxSize maximal size.
     * @return he scale down image size.
     */
    private Pair<Integer, Integer> calcSize(Bitmap bitmap, int maxSize) {
        int outWidth;
        int outHeight;
        int inWidth = bitmap.getWidth();
        int inHeight = bitmap.getHeight();
        if (inWidth > inHeight) {
            outWidth = maxSize;
            outHeight = (inHeight * maxSize) / inWidth;
        } else {
            outHeight = maxSize;
            outWidth = (inWidth * maxSize) / inHeight;
        }
        return new Pair<>(outWidth, outHeight);
    }

    /**
     * Sets the new Photo from the image uri.
     *
     * @param imageUri The uri of the new photo.
     */
    public void setPhoto(@Nullable Uri imageUri) {
        if (imageUri != null) {
            startImageLoading();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
                Pair<Integer, Integer> size = calcSize(bitmap, 1920);
                bitmap = Bitmap.createScaledBitmap(bitmap, size.first, size.second, true);
                binding.imageView.setImageBitmap(bitmap);
                binding.imageView.setAlpha(0.5f);
                VenueService.addPhoto(venueId, bitmapToBodyPart(bitmap)).enqueue(new DefaultCallback<Photo>(context) {
                    @Override
                    public void onResponse(Call<Photo> call, Response<Photo> response) {
                        if (response.isSuccessful()) {
                            newComment.photoId = response.body().id;
                            binding.imageView.setAlpha(1.f);
                        } else {
                            binding.imageView.setImageBitmap(null);
                        }
                        onFinally(call);
                    }

                    @Override
                    public void onFinally(Call<Photo> call) {
                        endImageLoading();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * End loading indication.
     */
    private void endImageLoading() {
        binding.imageLoading.setVisibility(View.GONE);
        imageLoading = false;
        if (binding.body.getText().length() > 0) {
            binding.submit.setEnabled(true);
        }
    }

    /**
     * Start loading indication
     */

    private void startImageLoading() {
        imageLoading = true;
        binding.submit.setEnabled(false);
        binding.imageLoading.setVisibility(View.VISIBLE);
        binding.imageView.setVisibility(View.VISIBLE);
    }

    /**
     * Create MultipartBody.Part from bitmap.
     *
     * @param bitmap The source bitmap.
     * @return the part.
     */
    @NonNull
    private MultipartBody.Part bitmapToBodyPart(Bitmap bitmap) {
        byte[] byteArray = SzUtils.compressWithJpgToByte(bitmap);
        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), byteArray);
        return MultipartBody.Part.createFormData("photo", SzUtils.getOwnername() + "photo" + ".jpg", reqFile);
    }
}

