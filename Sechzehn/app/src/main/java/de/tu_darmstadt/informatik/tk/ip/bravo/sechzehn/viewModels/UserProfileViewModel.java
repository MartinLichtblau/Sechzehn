package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.viewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.ServiceGenerator;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.Services.UserService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by marti on 28.06.2017.
 */

public class UserProfileViewModel extends ViewModel {
    private static MutableLiveData<User> user = new MutableLiveData<User>(); //Needs a new MutableLiveData<User>(), otherwise the Observer initially observes a null obj
    private UserService userService;

    public void initUser(String username){
        if (this.user != null) {
            // ViewModel is created per Fragment so
            // we know the user won't change
            return;
        } else
            fetchUser(username);
    }

    public LiveData<User> getUser(){
        return user;
    }

    private void fetchUser(final String username) {
        userService = ServiceGenerator.createService(UserService.class, "");
        userService.getUser(username).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                    Log.d("onResponse | ", response.toString());
                    Log.i(this.toString(), "getUser.onResponse | " + " REQ username: " + username + " RET username: " + response.body().getUsername());
                    user.setValue(response.body());

            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // the network call was a failure
                Log.e(this.toString(), "getUser.onFailure | ", t.getCause());
            }
        });
    }

}