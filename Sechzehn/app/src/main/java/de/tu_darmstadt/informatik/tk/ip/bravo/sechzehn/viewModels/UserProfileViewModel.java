package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.viewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Resource;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.ServiceGenerator;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services.UserService;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.GenericBody;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.NetworkUtils;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.SzUtils;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by marti on 28.06.2017.
 */

public class UserProfileViewModel extends ViewModel {
    private MutableLiveData<User> user = new MutableLiveData<User>(); //Needs a new MutableLiveData<User>(), otherwise the Observer initially observes a null obj
    private UserService userService;
    public MutableLiveData<Resource> resource = new MutableLiveData<>();



    public void initUser(final String username){

        if(user.getValue() != null){
            // ViewModel is created per Fragment so
            // we know the userId won't change
            Log.d(this.toString(),"a senderUser is already initialized");
            return;
        }

        userService = ServiceGenerator.createService(UserService.class, SzUtils.getToken());
        userService.getUser(username).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.isSuccessful()){
                    user.setValue(response.body());
                }else{
                    Log.d(this.toString(),"code: "+String.valueOf(response.code())+" — msg: "+response.message()+" — body: "+response.body());
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(this.toString(),"getUser.onFailure | ",t.getCause());
            }
        });
    }

    public void refreshUser(){
        initUser(getUsername());
    }

    public MutableLiveData<User> getUser(){
        return user;
    }

    public LiveData<Resource> requestFriendship(){
        resource.setValue(Resource.loading(null));
        userService.requestFriendship(getUsername()).enqueue(new Callback<Object>(){
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful())
                    resource.setValue(Resource.success(response.body(),"Friend Request send"));
                else
                    resource.setValue(Resource.error(NetworkUtils.parseError(response).getMessage(), null));
            }
            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                resource.setValue(Resource.error(t.getCause().toString(),null));
            }
        });
        return resource;
    }

    public LiveData<Resource> answerFriendship(final String answer){
        resource.setValue(Resource.loading(null));
        RequestBody body = new GenericBody().put("status", answer).generate();
        userService.answerFriendship(getUsername(), body).enqueue(new Callback<Object>(){
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful())
                    resource.setValue(Resource.success(response.body(),"Friend Request "+answer));
                else
                    resource.setValue(Resource.error(NetworkUtils.parseError(response).getMessage(), null));
            }
            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                resource.setValue(Resource.error(t.getCause().toString(),null));
            }
        });
        return resource;
    }

    public LiveData<Resource> deleteFriendship(){
        resource.setValue(Resource.loading(null));
        userService.deleteFriendship(getUsername()).enqueue(new Callback<Object>(){
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful())
                    resource.setValue(Resource.success(response.body(),getUsername()+" is no longer your friend"));
                else
                    resource.setValue(Resource.error(NetworkUtils.parseError(response).getMessage(), null));
            }
            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                resource.setValue(Resource.error(t.getCause().toString(),null));
            }
        });
        return resource;
    }

    public LatLng getLatLng(){
        if(user.getValue().getLat() != null && user.getValue().getLng() != null)
            return new LatLng(user.getValue().getLat(),user.getValue().getLng());
        return null;
    }

    public String getUsername(){
        return user.getValue().getUsername();
    }
}