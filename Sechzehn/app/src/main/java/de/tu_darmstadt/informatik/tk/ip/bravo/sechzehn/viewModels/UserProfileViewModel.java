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
    private LiveData<User> user;
    private UserService userService;

    public void init(String userId) {
        if (this.user != null) {
            // ViewModel is created per Fragment so
            // we know the userId won't change
            return;
        }
        try {
            user = fetch(userId);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public LiveData<User> getUser() {
        return user;
    }


    private LiveData<User> fetch(String userId) throws InterruptedException {
        final MutableLiveData<User> user = new MutableLiveData<>();
        userService = ServiceGenerator.createService(UserService.class, "");
        userService.getUser(userId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                user.setValue(response.body());
                Log.i(this.toString(),"onResponse: " + user.toString());
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // the network call was a failure
                Log.e(this.toString(),"onFailure",t.getCause());
            }
        });

        Thread.sleep(2000);
        return user;
    }


}