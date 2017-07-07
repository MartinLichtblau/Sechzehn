package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.viewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.util.Map;
import java.util.Objects;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.UserToken;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.ServiceGenerator;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.Services.UserService;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by marti on 28.06.2017.
 */

public class OwnerViewModel extends ViewModel {
    private String ownername;
    private String token;
    private MutableLiveData<User> owner = new MutableLiveData<User>(); //Needs a new MutableLiveData<User>(), otherwise the Observer initially observes a null obj
    private UserService userService;


    public LiveData<User> getOwner(){
        return owner;
    }

    public void initOwner(String ownername, String token){
        if(ownername != null || ownername != "" || owner.getValue() != null){
            Log.d(this.getClass().toString(), "initOwner | owner can only be set once");
        }
        Log.d(this.getClass().toString(), "initOwner");
        this.ownername = ownername;
        this.token = token;
        userService = ServiceGenerator.createService(UserService.class,token);
       /* User user = new User();
        user.setUsername(ownername);
        this.owner.setValue(user);*/
        userService.getUser(ownername).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.isSuccessful())
                    owner.setValue(response.body());
                Log.d(this.toString(),"code: "+String.valueOf(response.code())+" — msg: "+response.message()+" — body: "+response.body());
                }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // the network call was a failure
                Log.e(this.toString(),"getUser.onFailure | ",t.getCause());
            }
        });
    }

    public UserToken generateUserToken(){
        return new UserToken(token, owner.getValue());
    }

    public LatLng getLatLng(){
        if(owner.getValue().getLat() != null && owner.getValue().getLng() != null)
            return new LatLng(owner.getValue().getLat(),owner.getValue().getLng());
        return null;
    }

    public LiveData<String> changePassword(String oldPassword, String newPassword){
        Log.d(this.getClass().toString(), "changePassword");
        final MutableLiveData<String> msg = new MutableLiveData<String>();
        //Ref.:
        // > https://stackoverflow.com/questions/21398598/how-to-post-raw-whole-json-in-the-body-of-a-retrofit-request
        // > https://stackoverflow.com/questions/12155800/how-to-convert-hashmap-to-json-object-in-java
        Map<String, Object> map = new ArrayMap<>();
        map.put("old_password", oldPassword);
        map.put("password", newPassword);
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),(new JSONObject(map)).toString());
        userService.changePassword(ownername, body).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.isSuccessful()) {
                    owner.setValue(response.body());
                    msg.setValue("Password successfully changed");
                }else{
                    Log.d(this.toString(),"code: "+String.valueOf(response.code())+" — msg: "+response.message()+" — body: "+response.body());
                    msg.setValue("Upps: "+response.message());
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // the network call was a failure
                msg.setValue("Error: "+t.getCause());
            }
        });
        return msg;
    }

    public LiveData<String> resetPassword(){
        Log.d(this.getClass().toString(), "changePassword");
        final MutableLiveData<String> msg = new MutableLiveData<String>();
        Map<String, Object> map = new ArrayMap<>();
        map.put("email", owner.getValue().getEmail());
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),(new JSONObject(map)).toString());
        userService.resetPassword(body).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if(response.isSuccessful()) {
                    msg.setValue(response.message());
                }else{
                    Log.d(this.toString(),"code: "+String.valueOf(response.code())+" — msg: "+response.message()+" — body: "+response.body());
                    msg.setValue("Upps: "+response.message());
                }
            }
            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                // the network call was a failure
                msg.setValue("Error: "+t.getCause());
            }
        });
        return msg;
    }

    public LiveData<String> changeEmail(String password, String email){
        Log.d(this.getClass().toString(), "changeEmail");
        final MutableLiveData<String> msg = new MutableLiveData<String>();
        Map<String, Object> map = new ArrayMap<>();
        map.put("password", password);
        map.put("email", email);
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),(new JSONObject(map)).toString());
        userService.changeEmail(ownername, body).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.isSuccessful()) {
                    owner.setValue(response.body());
                    msg.setValue("New Email is "+owner.getValue().getEmail());
                }else{
                    Log.d(this.toString(),"code: "+String.valueOf(response.code())+" — msg: "+response.message()+" — body: "+response.body());
                    msg.setValue("Upps: "+response.message());
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // the network call was a failure
                msg.setValue("Error: "+t.getCause());
            }
        });
        return msg;
    }

    public LiveData<String> deleteAccount(String password){
        Log.d(this.getClass().toString(), "deleteAccount");
        final MutableLiveData<String> msg = new MutableLiveData<String>();
        Map<String, Object> map = new ArrayMap<>();
        map.put("password", password);
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),(new JSONObject(map)).toString());
        userService.deleteAccount(ownername,body).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if(response.isSuccessful()) {
                    msg.setValue(response.message()); //@TODO useful response is in "[{"field":"password","validation":"password_match","message":"Password does not match"}]"
                }else{
                    Log.d(this.toString(),"code: "+String.valueOf(response.code())+" — msg: "+response.message()+" — body: "+response.body());
                    msg.setValue("Upps: "+response.message());
                }
            }
            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                // the network call was a failure
                msg.setValue("Error: "+t.getCause());
            }
        });
        return msg;
    }
}