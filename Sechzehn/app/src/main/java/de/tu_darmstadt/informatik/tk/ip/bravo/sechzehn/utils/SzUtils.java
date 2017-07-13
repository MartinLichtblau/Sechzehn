package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import java.io.ByteArrayOutputStream;

/**
 * Created by marti on 12.07.2017.
 */

public final class SzUtils {

    private void SzUtils(){}

    public static String getRealPathFromURI(Context context, Uri uri) {
        //Ref > https://stackoverflow.com/questions/30974359/how-to-get-full-path-if-image-to-send-to-server-by-multipart-in-android/30974742
        //IF that does not work that > https://stackoverflow.com/questions/13209494/how-to-get-the-full-file-path-from-uri
        //Log.e("in","conversion"+contentURI.getPath());
        String path;
        Cursor cursor = context.getContentResolver()
                .query(uri, null, null, null, null);
        if (cursor == null)
            path=uri.getPath();
        else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            path=cursor.getString(idx);
        }
        if(cursor!=null)
            cursor.close();
        return path;
    }

    public static byte[] compressWithJpgToByte(Bitmap bitmap){
        // Best version to dynamically adapt filesize >
        // https://stackoverflow.com/questions/477572/strange-out-of-memory-issue-while-loading-an-image-to-a-bitmap-object/823966#823966
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
        //bitmap.recycle();  //currently kills the app
        return stream.toByteArray();
    }

    public static MutableLiveData<Bitmap> centerCropImage(Context context,Uri uri){
        final MutableLiveData<Bitmap> scaledImg = new MutableLiveData<Bitmap>();
        Picasso.with(context)
                .load(uri)
                .centerCrop()
                .resize(512,512)
                .into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                //This gets called when your application has the requested resource in the bitmap object
                Log.i("onBitmapLoaded | ", "Bitmap size is: "+bitmap.getAllocationByteCount());
                scaledImg.setValue(bitmap);
            }
            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                //This gets called if the library failed to load the resource
            }
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                //This gets called when the request is started
            }
        });
        return scaledImg;
    }


}
