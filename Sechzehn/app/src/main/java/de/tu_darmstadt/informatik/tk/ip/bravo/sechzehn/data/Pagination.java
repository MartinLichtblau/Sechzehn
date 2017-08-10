package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data;

import android.support.annotation.NonNull;

import java.util.LinkedList;
import java.util.List;

/**
 * Provides a generic Wrapper for paginated responses
 * Created by JohannesTP on 19.07.2017.
 */

public class Pagination<T> {
    /**
     * The total number of elements
     */
    public int total;
    /**
     * The number of the elements per page
     */
    public int perPage;
    /**
     * The number of the current page
     */
    public int currentPage;
    /**
     * The number of the last page
     */
    public int lastPage;
    /**
     * Data of this Page
     */
    @NonNull
    public List<T> data = new LinkedList<>();
}
