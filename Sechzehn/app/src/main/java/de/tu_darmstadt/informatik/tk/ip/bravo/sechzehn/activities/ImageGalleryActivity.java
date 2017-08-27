package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.activities;

import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.etiennelawlor.imagegallery.library.activities.FullScreenImageGalleryActivity;
import com.squareup.picasso.Picasso;

/**
 * @author Alexander Geiß on 27.08.2017.
 */

public class ImageGalleryActivity extends FullScreenImageGalleryActivity {

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
    /*
    , new Callback() {
                        @Override
                        public void onSuccess() {
                            Bitmap bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
                            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                                public void onGenerated(Palette palette) {
                                    applyPalette(palette, bgLinearLayout);
                                }
                            });
                        }
                        @Override
                        public void onError() {}
                    }
     */
}
