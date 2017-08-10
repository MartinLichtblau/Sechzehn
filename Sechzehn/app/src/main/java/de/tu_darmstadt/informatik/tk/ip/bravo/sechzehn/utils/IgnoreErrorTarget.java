package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * @author Alexander Geiß on 10.08.2017.
 */

public abstract class IgnoreErrorTarget implements Target {

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {

    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {

    }
}
