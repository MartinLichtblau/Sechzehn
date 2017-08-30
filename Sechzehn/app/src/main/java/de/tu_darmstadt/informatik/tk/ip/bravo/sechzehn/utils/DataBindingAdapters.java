package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils;

import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.os.Handler;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;

/**
 * @author Alexander Geiß on 18.08.2017.
 */

public class DataBindingAdapters {
    @BindingAdapter({"profilePictureUrl"})
    public static void loadProfilePicture(ImageView view, String url) {
        Picasso.with(view.getContext())
                .load(url)
                .placeholder(R.drawable.ic_owner) //Placeholders and error images are not resized and must be fairly small images.
                .centerCrop().resize(40, 40)
                .transform(SzUtils.CROP_CIRCLE_TRANSFORMATION)
                .into(view);
    }

    @BindingAdapter({"kingPictureUrl"})
    public static void loadKingPicture(ImageView view, String url) {
        Picasso.with(view.getContext())
                .load(url)
                .placeholder(R.drawable.ic_owner) //Placeholders and error images are not resized and must be fairly small images.
                .centerCrop().resize(80, 80)
                .transform(SzUtils.CROP_CIRCLE_TRANSFORMATION)
                .into(view);
    }

    @BindingAdapter({"venuePictureUrl"})
    public static void loadVenuePicture(ImageView view, String url) {
        Picasso.with(view.getContext())
                .load(url)
                .placeholder(R.drawable.profile_coverfoto_default) //Placeholders and error images are not resized and must be fairly small images.
                .centerCrop().resize(1080, 688)
                .into(view);
    }

    @BindingAdapter({"venuePictureBitmap"})
    public static void setVenuePicture(final ImageView view, final Bitmap bitmap) {
        view.setImageBitmap(bitmap);
        // When a bitmap is downloaded you do:
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                view.setImageBitmap(bitmap);
            }
        });
    }

}
