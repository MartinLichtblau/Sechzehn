package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
        void addComment(Comment comment);

        void addPhotoToNewComment();
    }

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

    private void newCommentSetEnabled(boolean b) {
        binding.body.setEnabled(b);
        binding.submit.setEnabled(b);
        binding.imageButton.setEnabled(b);
    }


    public void addPhotoToNewComment(View v) {
        newCommentListener.addPhotoToNewComment();
        //Result receive in @onActivityResult
    }


    public void setPhoto(@Nullable Uri imageUri) {
        if (imageUri != null) {
            startImageLoading();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
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

    private void endImageLoading() {
        binding.imageLoading.setVisibility(View.GONE);
        imageLoading = false;
        if (binding.body.getText().length() > 0) {
            binding.submit.setEnabled(true);
        }
    }

    private void startImageLoading() {
        imageLoading = true;
        binding.submit.setEnabled(false);
        binding.imageLoading.setVisibility(View.VISIBLE);
    }

    @NonNull
    private MultipartBody.Part bitmapToBodyPart(Bitmap bitmap) {
        byte[] byteArray = SzUtils.compressWithJpgToByte(bitmap);
        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), byteArray);
        return MultipartBody.Part.createFormData("photo", SzUtils.getOwnername() + "photo" + ".jpg", reqFile);
    }
}

