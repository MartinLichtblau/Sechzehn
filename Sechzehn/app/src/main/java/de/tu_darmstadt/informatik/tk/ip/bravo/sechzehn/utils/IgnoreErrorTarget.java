package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import retrofit2.Call;

/**
 * @author Alexander Geiß on 10.08.2017.
 */

public abstract class IgnoreErrorTarget implements Target {

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
        Log.e("Bitmap Error:", "Bitmap failed to load");
        onFinally();
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {

    }


    public void onFinally() {

    }
}
