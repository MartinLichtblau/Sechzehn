package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.viewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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
    private MutableLiveData<User> user = new MutableLiveData<User>(); //Needs a new MutableLiveData<User>(), otherwise the Observer initially observes a null obj
    private UserService userService;
    private MarkerOptions userMarker;


    public void initUser(final String username){
        if(user.getValue() != null){
            // ViewModel is created per Fragment so
            // we know the userId won't change
            Log.d(this.toString(),"a user is already initialized");
            return;
        }
        userService = ServiceGenerator.createService(UserService.class, "");
        userService.getUser(username).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                Log.i(this.toString(),"getUser.onResponse | "  + " REQ username: " + username +  " RET username: " + response.body().getUsername());
                user.setValue(response.body());
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // the network call was a failure
                Log.e(this.toString(),"getUser.onFailure | ",t.getCause());
            }
        });
    }

    public LiveData<User> getUser(){
        return user;
    }

    public MarkerOptions getMarkerOptions(){
       if(userMarker == null ){
           userMarker = new MarkerOptions()
                   .position(new LatLng(user.getValue().getLat(),user.getValue().getLng()))
                   .title(user.getValue().getUsername());
       }
       return userMarker;
    }
}