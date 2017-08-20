package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.lists;

import android.support.annotation.NonNull;

import java.util.AbstractList;
import java.util.List;

/**
 * @author Alexander Geiß on 19.08.2017.
 */

public class HeaderList<T> extends AbstractList<T> {
    @NonNull
    private T header;
    @NonNull
    private List<T> list;

    public HeaderList(@NonNull T header, @NonNull List<T> list) {
        this.header = header;
        this.list = list;
    }

    public static <T> HeaderList<T> HeaderList(@NonNull T header, @NonNull List<T> list) {
        return new HeaderList<>(header, list);
    }

    @Override
    public T get(int index) {
        index--;
        if (index == -1) {
            return header;
        }
        return list.get(index);
    }

    @Override
    public int size() {
        return list.isEmpty() ? 0 : list.size() + 1;
    }
}
