package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.lists;

import android.support.annotation.NonNull;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


/**
 * @author Alexander Geiß on 19.08.2017.
 */

public abstract class MapList<T, S> extends AbstractList<S> {
    @NonNull
    private List<T> list;

    protected MapList(@NonNull List<T> list) {
        this.list = list;
    }

    protected abstract S map(T item);

    @Override
    public S get(int index) {
        return map(list.get(index));
    }

    @Override
    public S remove(int index) {
        return map(list.remove(index));
    }

    @NonNull
    @Override
    public ListIterator<S> listIterator() {
        return new LItr();
    }

    @NonNull
    @Override
    public ListIterator<S> listIterator(int index) {
        return new LItr(index);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @NonNull
    @Override
    public Iterator<S> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<S> {
        Iterator<T> iterator = list.iterator();

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public S next() {
            return map(iterator.next());
        }

        @Override
        public void remove() {
            iterator.remove();
        }
    }

    private class LItr implements ListIterator<S> {
        ListIterator<T> iterator;

        public LItr() {
            iterator = list.listIterator();
        }

        public LItr(int index) {
            iterator = list.listIterator(index);
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public S next() {
            return map(iterator.next());
        }

        @Override
        public boolean hasPrevious() {
            return iterator.hasPrevious();
        }

        @Override
        public S previous() {
            return map(iterator.previous());
        }

        @Override
        public int nextIndex() {
            return iterator.nextIndex();
        }

        @Override
        public int previousIndex() {
            return iterator.previousIndex();
        }

        @Override
        public void remove() {
            iterator.remove();
        }

        @Override
        public void set(S s) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(S s) {
            throw new UnsupportedOperationException();
        }


    }

    @Override
    public void clear() {
        list.clear();
    }
}
