package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.lists;

import android.support.annotation.NonNull;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;


/**
 * @author Alexander Geiß on 19.08.2017.
 */

public class ConcatList<T> extends AbstractList<T> {
    @NonNull
    private List<T>[] lists;

    @SafeVarargs
    public ConcatList(@NonNull List<T>... lists) {
        this.lists = lists;
    }

    @SafeVarargs
    public static <S> ConcatList<S> ConcatList(@NonNull List<S>... lists) {
        return new ConcatList<>(lists);
    }

    @Override
    public T get(int index) {
        int newIndex = index;
        for (List<T> list : lists) {
            int size = list.size();
            if (newIndex < size) {
                return list.get(newIndex);
            }
            newIndex -= size;
        }
        return null;
    }

    @Override
    public int size() {
        int size = 0;
        for (List<T> list : lists) {
            size += list.size();
        }
        return size;
    }
}
