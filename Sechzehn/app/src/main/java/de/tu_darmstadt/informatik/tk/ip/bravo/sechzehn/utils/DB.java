package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils;

import android.view.View;

/**
 * @author Alexander Geiß on 28.08.2017.
 */

public class DB {
    public static int objectToVisibility(Object obj) {
        return booleanToVisibility(obj != null);
    }

    public static int booleanToVisibility(boolean bool) {
        return bool ? View.VISIBLE : View.GONE;
    }

    public static int booleanToVisibility(boolean bool, boolean invisibleInsteadOfGone) {
        if (invisibleInsteadOfGone)
            return bool ? View.VISIBLE : View.INVISIBLE;
        else
            return bool ? View.VISIBLE : View.GONE;
    }
}
