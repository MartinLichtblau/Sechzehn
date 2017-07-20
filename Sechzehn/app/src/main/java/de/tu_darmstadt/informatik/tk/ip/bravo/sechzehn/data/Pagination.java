package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data;

import java.util.List;

/**
 * Created by JohannesTP on 19.07.2017.
 */

public class Pagination<T> {
    public int total;
    public int perPage;
    public int currentPage;
    public int lastPage;
    public List<T> data;
}
