package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.activities;

import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.etiennelawlor.imagegallery.library.activities.FullScreenImageGalleryActivity;
import com.squareup.picasso.Picasso;

/**
 * The Image Gallery Activity. Shows images in Fullscreen with zoom, pan and swipe.
 * @author Alexander Geiß on 27.08.2017.
 */

public class ImageGalleryActivity extends FullScreenImageGalleryActivity {
    /**
     * Provides the implementation for loading the images.
     */
    @Override
    public void loadFullScreenImage(final ImageView iv, String imageUrl, int width, LinearLayout bglinearLayout) {
        if (!TextUtils.isEmpty(imageUrl)) {
            Picasso.with(iv.getContext())
                    .load(imageUrl)
                    .resize(width, 0)
                    .into(iv);
        } else {
            iv.setImageDrawable(null);
        }
    }

}
