package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


/**
 * Created by marti on 20.07.2017.
 */

//a generic class that describes a data with a status
//Ref > https://developer.android.com/topic/libraries/architecture/guide.html#addendum
public class Resource<T> {
    public enum Status {SUCCESS, ERROR, LOADING}
    @NonNull
    public Status status;
    @Nullable
    public final T data;
    @Nullable public final String message;
    private Resource(Status status, @Nullable T data, @Nullable String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static <T> Resource<T> success(@NonNull T data, @Nullable String message) {
        return new Resource<>(Status.SUCCESS, data, message);
    }

    public static <T> Resource<T> error(String msg, @Nullable T data) {
        return new Resource<>(Status.ERROR, data, msg);
    }

    public static <T> Resource<T> loading(@Nullable T data) {
        return new Resource<>(Status.LOADING, data, null);
    }
}
