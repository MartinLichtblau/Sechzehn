package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.lists;

import android.support.annotation.NonNull;

import java.util.List;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Pagination;

import static de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.SzUtils.ceilDiv;

/**
 * @author Alexander Geiß on 19.08.2017.
 */

public class PaginatedList<T> {
    private List<T> list;

    public PaginatedList(@NonNull List<T> list) {
        this.list = list;
    }

    public static <T> PaginatedList<T> PaginatedList(@NonNull List<T> list) {
        return new PaginatedList<T>(list);
    }

    @NonNull
    public Pagination<T> get(int page, int perPage) {
        Pagination<T> pagination = createPagination(page, perPage);

        int fromIndex = perPage * (page - 1);
        int toIndex = fromIndex + perPage;

        if (toIndex > list.size()) {
            toIndex = list.size();
        }

        if (fromIndex <= toIndex) {
            pagination.data = list.subList(fromIndex, toIndex);
        }

        return pagination;
    }



    @NonNull
    private Pagination<T> createPagination(int page, int perPage) {
        Pagination<T> pagination = new Pagination<>();
        pagination.currentPage = page;
        pagination.lastPage = ceilDiv(list.size(),perPage);
        pagination.perPage = perPage;
        pagination.total = list.size();
        return pagination;
    }

}
