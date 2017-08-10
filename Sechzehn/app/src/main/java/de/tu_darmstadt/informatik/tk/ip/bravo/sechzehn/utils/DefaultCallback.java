package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * This callback shows a default error message onFailure.
 *
 * @author Alexander Geiß on 20.07.2017.
 */

public abstract class DefaultCallback<T> implements Callback<T> {
    private Context context;
    @NonNull
    private String message = "Connectivity error!";

    /**
     * Creates a new DefaultCallback
     *
     * @param context Activity to show Toast
     */
    public DefaultCallback(Context context) {
        this.context = context;
    }

    /**
     * Creates a new DefaultCallback
     *
     * @param context Activity to show Toast
     * @param message Error message
     */
    public DefaultCallback(Context context, @NonNull String message) {
        this.context = context;
        this.message = message;
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        Log.e("HTTP Error:", t.toString());
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
