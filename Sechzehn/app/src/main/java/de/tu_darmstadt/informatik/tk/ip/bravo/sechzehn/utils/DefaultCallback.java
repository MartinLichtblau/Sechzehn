package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils;

import android.content.Context;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Alexander Geiß on 20.07.2017.
 */

public abstract class DefaultCallback<T> implements Callback<T> {
    private Context context;

    public DefaultCallback(Context context) {
        this.context = context;
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        Toast.makeText(context, "Connectivity error!", Toast.LENGTH_SHORT).show();
    }
}
