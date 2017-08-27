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
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.ColorUtils;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

/**
 * Created by marti on 12.07.2017.
 */

public final class SzUtils {
    public static final CropCircleTransformation CROP_CIRCLE_TRANSFORMATION = new CropCircleTransformation();
    private static String ownername;
    private static String token;
    public enum ThumbType {USER, VENUE}
    final static List<Target> strongReferenceTargetList = new ArrayList<>();
    public final static Gson gson = new Gson();


    private void SzUtils(){}

    public static void initialize(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        ownername = prefs.getString("ownername","");
        token = prefs.getString("JWT","");
    }

    public static String getToken(){
        return token;
    }

    public static String getOwnername(){
        return ownername;
    }

    public static int ceilDiv(int x, int y) {
        return (x + y - 1) / y;
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

    @ColorInt
    public static int getThemeColor(@NonNull final Context context, @AttrRes final int attributeColor) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(attributeColor, value, true);
        return value.data;
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
                        scaledImg.postValue(bitmap);
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

    private static Bitmap compressBitmap(Bitmap original){
        return original;
      /*  ByteArrayOutputStream out = new ByteArrayOutputStream();
        original.compress(Bitmap.CompressFormat.PNG, 50, out);
        Bitmap compressed = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
        //Log.d("compress:", "Compressed = "+compressed.getByteCount()+" Original = "+original.getByteCount());
        return compressed;*/

        //return Bitmap.createScaledBitmap(original, 150, 150, false);
    }


    public static Bitmap userPinBackground = null;
    public static  Bitmap venuePinBackground = null;
    public static void checkInitDrawablesOnce(Context context){
        if(venuePinBackground == null){
            //Alpha/Transparency is first two chars Ref.: https://stackoverflow.com/a/17239853/3965610
            Integer color = Color.parseColor("#D9FFFFFF"); //BF Transparency is good
            venuePinBackground = tintBitmap(Bitmap.createScaledBitmap(
                    BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_venue_pin_background), 120, 120, false),color);
        }
        if(userPinBackground == null){
            userPinBackground = Bitmap.createScaledBitmap(
                    BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_user_pin_background), 120, 120, false);
        }
    }

    public static MutableLiveData<Bitmap> createUserPin(Context context, Boolean highlight, @Nullable String url){
        checkInitDrawablesOnce(context);

        final MutableLiveData<Bitmap> scaledImg = new MutableLiveData<>();
        final Bitmap background;
        if(highlight){
            background = tintBitmap(userPinBackground, Color.parseColor("#FF4081"));
        }else {
            background = userPinBackground;
        }

        final Bitmap defaultUserPin = compressBitmap(mergeToPin(background,
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_user_pin_pic_default), 100, 100, false)));

        if(TextUtils.isEmpty(url)){
            //User has no profile picture
            scaledImg.postValue(defaultUserPin);
        }else{
            final Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    strongReferenceTargetList.remove(this);
                    Bitmap original = mergeToPin(background, bitmap);
                    Bitmap compressed = compressBitmap(original);
                    Log.d("compress:", "Compressed = "+compressed.getByteCount()+" Original = "+original.getByteCount());
                    scaledImg.postValue(compressed);
                }
                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    strongReferenceTargetList.add(this);
                    scaledImg.postValue(defaultUserPin);
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
        }

        return scaledImg;
    }

    public static MutableLiveData<Bitmap> createVenuePin(Context context, Double rating, @Nullable String url){
        checkInitDrawablesOnce(context);
        final MutableLiveData<Bitmap> scaledImg = new MutableLiveData<>();
        //color background based on rating (copy background or it fucks up, since background is static var, still less resource consuming)
        final Bitmap coloredBack = tintBitmapByRating(venuePinBackground.copy(venuePinBackground.getConfig(),venuePinBackground.isMutable()), rating);
        final Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                strongReferenceTargetList.remove(this);
                Bitmap coloredIcon = tintBitmap(bitmap, Color.BLACK);
                scaledImg.postValue(compressBitmap(mergeToPin(coloredBack, coloredIcon)));
            }
            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                strongReferenceTargetList.add(this);
                scaledImg.postValue(venuePinBackground);
            }
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };
        strongReferenceTargetList.add(target);

        Picasso.with(context)
                .load(url)
                .centerCrop().resize(100, 100)
                .into(target);
        return scaledImg;
    }

    public static Bitmap tintBitmap(Bitmap bitmap, int color) {
        Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
        Bitmap bitmapResult = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapResult);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return bitmapResult;
    }

    public static Bitmap mergeToPin(Bitmap back, Bitmap front) {
        //Ref. > https://stackoverflow.com/questions/31813638/how-to-merge-bitmaps-in-android
        Bitmap result = Bitmap.createBitmap(back.getWidth(), back.getHeight(), back.getConfig());
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(back, 0f, 0f, null);
        canvas.drawBitmap(front, 10, 4, null);
        return result;
    }

    public static Bitmap tintBitmapByRating(Bitmap orig, Double rating){
        if(rating <= 0) //venue got no rating
            return orig;
        float[] hslColor = ratingToHslColor(rating);
        int color = ColorUtils.HSLToColor(hslColor);
        return tintBitmap(orig, color);
    }

    public static float[] ratingToHslColor(Double rating){
        //In Theory: Rating ranges from 0.0 to 10.0
        //In Reality: 99% have rating higher than 3.0
        // conclusion > Cut useless rating range and expand/broaden usefull range on whole color spectrum to to distinguish rating colors easier
        // How?  && expand range from 3-10 on larger previous 0-10 spectrum
        Double adaptedRating = rating -3.333333d; //Shift range -3.3 period (1/3)
        adaptedRating = adaptedRating < 0 ? 0 : adaptedRating; //Cut all ratings which were below -3.3
        adaptedRating = adaptedRating * 1.5; // expand ratings by 1.5 (1(1/2)) to map on range from 0 - 10 again

        Double colorPercentage = (adaptedRating / 10); //the actual ratingPercentage (0-1) to calculate the color gradient
        Integer minHue = 0; //full red
        Integer maxHue = 120; //120 = full green, 100 = still intense green
        float[] hslColor = percentageToHsl(colorPercentage, minHue, maxHue);
        Log.d("ratingToHslColor","rating: "+String.valueOf(rating)+" adaptedRating: "+String.valueOf(adaptedRating)+" Percentage: "+colorPercentage+" Hue: "+String.valueOf(hslColor[0]));
        return hslColor;
    }

    public static float[] percentageToHsl(Double percentage, Integer hue0, Integer hue1) {
        float[] hslColor = new float[3];
        //Ref: > https://stackoverflow.com/questions/17525215/calculate-color-values-from-green-to-red
        /*percentage: a value between 0 and 1
        hue0: the hue value of the color you want to get when the percentage is 0
        hue1: the hue value of the color you want to get when the percentage is 1*/
        float huePercentage = (float) ((percentage * (hue1 - hue0)) + hue0);
        hslColor[0] = huePercentage; //FUCK IT: Hue is the only value that the ColorUtils method does not want as ° between 0-1
        hslColor[1] = 1f; //Full saturation
        hslColor[2] = 0.5f; //Half/normal light
        return hslColor;
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

    public static String getNowDate(String format){
        String nowDate;
        Calendar now = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(format); //e.g. "yyyy-MM-dd hh:mm"
        nowDate = sdf.format(now.getTime());
        return nowDate;
    }




}
