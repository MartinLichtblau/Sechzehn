package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.Repositories;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.UserToken;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.ServiceGenerator;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.Services.UserService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by marti on 28.06.2017.
 */

public class UserRepository {
    private UserService userService;

    public LiveData<User> getUser(String userId) {
        final MutableLiveData<User> data = new MutableLiveData<>();

        userService = ServiceGenerator.createService(UserService.class, "");
        Call<User> call = userService.getUser("1");

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                // error case is left out for brevity
                data.setValue(response.body());
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // the network call was a failure
                // TODO: handle error
            }
        });
        return data;
    }
}
