package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.IllegalFormatException;
import java.util.List;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

import static de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.SzUtils.ThumbType.USER;
import static de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.SzUtils.ThumbType.VENUE;

/**
 * Created by marti on 12.07.2017.
 */

public final class SzUtils {
    public static final CropCircleTransformation CROP_CIRCLE_TRANSFORMATION = new CropCircleTransformation();
    private static SharedPreferences sharedPreferences; //SharedPreferences are null when app is not running
    private static String ownername;
    private static String token;
    private static Context context;
    private static Bitmap user_background;
    private static Bitmap venue_background;
    public enum ThumbType {USER, VENUE}
    final static List<Target> strongReferenceTargetList = new ArrayList<>();

    public final static Gson gson = new Gson();

    private void SzUtils(){}

    public static void initialize(Context cx){
        context = cx;
        sharedPreferences = cx.getSharedPreferences("Sechzehn",0);
        ownername = sharedPreferences.getString("ownername","");
        token = sharedPreferences.getString("JWT","");

        user_background = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_person_pin), 120, 120, false);

        venue_background = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_search), 120, 120, false);
    }

    public static String getToken(){
        return token;
    }

    public static String getOwnername(){
        return ownername;
    }

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

    public static MutableLiveData<Bitmap> createThumb(@NonNull ThumbType type, @Nullable String url){
        final MutableLiveData<Bitmap> scaledImg = new MutableLiveData<>();
        final Bitmap background;
        switch (type){
            case VENUE:
                background = venue_background;
            break;
            default:
                background = user_background;
        }
        if(TextUtils.isEmpty(url))
            scaledImg.setValue(background);

        final Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                strongReferenceTargetList.remove(this);
                scaledImg.setValue(mergeToPin(background, bitmap));
            }
            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                strongReferenceTargetList.add(this);
                scaledImg.setValue(background);
            }
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };
        strongReferenceTargetList.add(target);

        Picasso.with(context)
                .load(url)
                .centerCrop().resize(100, 100)
                .transform(CROP_CIRCLE_TRANSFORMATION)
                .into(target);

        return scaledImg;
    }

    public static Bitmap mergeToPin(Bitmap back, Bitmap front) {
        //Ref. > https://stackoverflow.com/questions/31813638/how-to-merge-bitmaps-in-android
        Bitmap result = Bitmap.createBitmap(back.getWidth(), back.getHeight(), back.getConfig());
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(back, 0f, 0f, null);
        canvas.drawBitmap(front, 10, 4, null);
        return result;
    }

    public static Calendar timestampToCal(String timestamp){
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            cal.setTime(sdf.parse(timestamp));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return cal;
    }

    public static String calToTimestamp(Calendar cal){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(cal.getTime());
    }

    public static String getAge(Calendar dob){
        //Ref > https://stackoverflow.com/questions/5291503/how-to-create-method-for-age-calculation-method-in-android
        if (dob == null)
            return  null;
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
            age--;
        }
        return String.valueOf(age);
    }




}
