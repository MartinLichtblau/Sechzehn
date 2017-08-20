package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.lists;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;


import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Pagination;

import static de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.SzUtils.ceilDiv;

/**
 * @author Alexander Geiß on 19.08.2017.
 */

public abstract class ProxyList<T> extends AbstractList<T> {
    private List<Pagination<T>> list = new ArrayList<>();


    private int total;
    private int perPage = 10;
    private int currentTotal = 0;

    public ProxyList() {
        reset();
    }

    public ProxyList(int perPage) {
        this.perPage = perPage;
        reset();
    }

    @Override
    public T get(int index) {
        if (index >= currentTotal) {
            loadAndAddUpTo(index);
        }
        Pagination<T> currentPagination = list.get(index / perPage);
        return currentPagination.data.get(index % perPage);
    }

    private void loadAndAddUpTo(int index) {
        int lastPage = ceilDiv(index, perPage) + 1;
        for (int i = list.size() + 1; i <= lastPage; i++) {
            add(load(i, perPage));
        }
    }

    public int size() {
        return total;
    }

    protected abstract Pagination<T> load(int page, int perPage);

    public void add(Pagination<T> page) {
        if (page == null) return;
        if (page.perPage != perPage && page.data.size() <= perPage) {
            throw new IllegalArgumentException("Number of elements per page do not match");
        }
        currentTotal += page.data.size();
        total = page.total;
        list.add(page);
    }

    public void reset() {
        list.clear();
        total = 0;
        currentTotal = 0;
        add(load(1, perPage));
    }


}
