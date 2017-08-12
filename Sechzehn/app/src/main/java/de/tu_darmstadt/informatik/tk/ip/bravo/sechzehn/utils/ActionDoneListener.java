package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils;

import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

/**
 * @author Alexander Geiß on 12.08.2017.
 */

public abstract class ActionDoneListener implements TextView.OnEditorActionListener {
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            onActionDone(v,actionId,event);
            return true;
        }
        return false;
    }

    public abstract void onActionDone(TextView v, int actionId, KeyEvent event) ;
}
