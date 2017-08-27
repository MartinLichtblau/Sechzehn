package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.etiennelawlor.imagegallery.library.adapters.FullScreenImageGalleryAdapter;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * @author Alexander Geiß on 27.08.2017.
 */

public class ImageGallery extends ViewPager implements FullScreenImageGalleryAdapter.FullScreenImageLoader {
    public ImageGallery(Context context) {
        super(context);
    }

    public ImageGallery(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private final ViewPager.OnPageChangeListener viewPagerOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            setCurrentItem(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };


    public void setPhotos(List<String> imageList) {
        FullScreenImageGalleryAdapter fullScreenImageGalleryAdapter = new FullScreenImageGalleryAdapter(imageList);
        fullScreenImageGalleryAdapter.setFullScreenImageLoader(this);
        setAdapter(fullScreenImageGalleryAdapter);
        removeOnPageChangeListener(viewPagerOnPageChangeListener);
        addOnPageChangeListener(viewPagerOnPageChangeListener);
    }

    @Override
    public void loadFullScreenImage(ImageView iv, String imageUrl, int width, LinearLayout bglinearLayout) {
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
