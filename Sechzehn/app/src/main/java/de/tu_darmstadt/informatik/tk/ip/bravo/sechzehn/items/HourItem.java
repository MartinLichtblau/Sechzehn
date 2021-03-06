package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.items;

import android.view.View;
import android.widget.TextView;

import java.util.Calendar;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.views.NestedListView;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.venue.Hour;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.SzUtils;

/**
 * Display a opening hour.
 * @author Alexander Geiß on 22.08.2017.
 */

public class HourItem implements NestedListView.Item {

    private TextView textView;
    private Hour hour;

    public HourItem(Hour hour) {
        this.hour = hour;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_hour;
    }

    @Override
    public void bind(View view) {
        textView = (TextView) view.findViewById(R.id.hourItem);
        if (hour.day == Hour.Day.UNKNOWN) {
            textView.setText("Not available");
            return;
        }
        textView.setText(String.format("%s %s-%s", hour.day, hour.start, hour.end));
        colorIfToday(view);
    }

    private void colorIfToday(View view) {
        int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        if (hour.day.getDayNumber() == today) {
            int accentColor = SzUtils.getThemeColor(view.getContext(), R.attr.colorAccent);
            textView.setTextColor(accentColor);
        }
    }
}
