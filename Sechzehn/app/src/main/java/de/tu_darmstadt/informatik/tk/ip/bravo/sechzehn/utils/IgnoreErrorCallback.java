package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Callback which ignores failures
 * @author Alexander Geiß on 10.08.2017.
 */

public abstract class IgnoreErrorCallback<T> implements Callback<T>{
    @Override
    public void onFailure(Call<T> call, Throwable t) {

    }
}
