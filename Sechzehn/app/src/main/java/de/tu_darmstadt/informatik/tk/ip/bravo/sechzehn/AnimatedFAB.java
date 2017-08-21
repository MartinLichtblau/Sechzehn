package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn;

/**
 * @author Alexander Geiß on 21.08.2017.
 */

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;

import com.gordonwong.materialsheetfab.AnimatedFab;

public class AnimatedFAB extends FloatingActionButton implements AnimatedFab {

    public AnimatedFAB(Context context) {
        super(context);
    }

    public AnimatedFAB(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AnimatedFAB(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Shows the FAB.
     */
    @Override
    public void show() {
        super.show();
    }

    /**
     * Shows the FAB and sets the FAB's translation.
     *
     * @param translationX translation X value
     * @param translationY translation Y value
     */
    @Override
    public void show(float translationX, float translationY) {
        super.show();
    }

    /**
     * Hides the FAB.
     */
    @Override
    public void hide() {
        super.hide();
    }
}