package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.viewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Resource;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.GenericBody;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.NetworkUtils;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.ServiceGenerator;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.Services.UserService;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.SzUtils;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by marti on 28.06.2017.
 */

public class OwnerViewModel extends ViewModel {
    private MutableLiveData<User> owner = new MutableLiveData<User>(); //Needs a new MutableLiveData<User>(), otherwise the Observer initially observes a null obj
    private static final UserService userService = ServiceGenerator.createService(UserService.class,SzUtils.getToken());
    private MutableLiveData<String> toastMessage = new MutableLiveData<String>();
    private MutableLiveData<Resource> resource = new MutableLiveData<>();



    public LiveData<User> getOwner(){
        return owner;
    }

    public MutableLiveData<Resource> initOwner(String ownername){
        resource.setValue(Resource.loading(null));
        userService.getUser(ownername).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    owner.setValue(response.body());
                    resource.setValue(Resource.success(response.body()));
                } else
                    resource.setValue(Resource.error(NetworkUtils.parseError(response).getMessage(), null));
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                resource.setValue(Resource.error(t.toString(),null));
            }
        });
        return resource;
    }

    public String getOwnername(){
        return owner.getValue().getUsername();
    }

    public LatLng getLatLng(){
        if(owner.getValue().getLat() != null && owner.getValue().getLng() != null)
            return new LatLng(owner.getValue().getLat(),owner.getValue().getLng());
        return new LatLng(49.872825,8.651193); //TODO Remove after finished Debug
    }

    public LiveData<String> receiveToast(){
        //BottomTabsMain observes this method and makes a toast whenever it changes
        return toastMessage;
    }

    public LiveData<String> makeToast(String message){
        toastMessage.setValue(message);
        return toastMessage;
    }

    //-------------------------------------REST Functions------->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    public LiveData<Boolean> editProfile(User o){
        Log.d(this.getClass().toString(), "editProfile");
        final MutableLiveData<Boolean> close = new MutableLiveData<Boolean>();
        userService.updateUser(getOwnername(), o).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.isSuccessful()) {
                    owner.setValue(response.body());
                    makeToast("Profile updated");
                    close.setValue(true);
                }else{
                    makeToast("Upps: "+ NetworkUtils.parseError(response).getMessage());
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                makeToast("Error: "+t.getCause());
            }
        });
        return close;
    }

    public LiveData<Boolean> changePassword(String oldPassword, String newPassword){
        Log.d(this.getClass().toString(), "changePassword");
        final MutableLiveData<Boolean> close = new MutableLiveData<Boolean>();
        RequestBody body = new GenericBody().put("old_password", oldPassword).put("password", newPassword).generate();
        userService.changePassword(getOwnername(), body).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.isSuccessful()) {
                    owner.setValue(response.body());
                    makeToast("Password successfully changed");
                    close.setValue(true);
                }else{
                   makeToast("Upps: "+ NetworkUtils.parseError(response).getMessage());
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
               makeToast("Error: "+t.getCause());
            }
        });
        return close;
    }

    public LiveData<Boolean> resetPassword(){
        Log.d(this.getClass().toString(), "changePassword");
        final MutableLiveData<Boolean> close = new MutableLiveData<Boolean>();
        RequestBody body = new GenericBody().put("email", owner.getValue().getEmail()).generate();
        userService.resetPassword(body).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if(response.isSuccessful()) {
                    makeToast(response.message());
                    close.setValue(true);
                }else{
                    makeToast("Upps: "+ NetworkUtils.parseError(response).getMessage());
                }
            }
            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                makeToast("Error: "+t.getCause());
            }
        });
        return close;
    }

    public LiveData<Boolean> changeEmail(String password, String email){
        Log.d(this.getClass().toString(), "changeEmail");
        final MutableLiveData<Boolean> close = new MutableLiveData<Boolean>();
        RequestBody body = new GenericBody().put("password", password).put("email", email).generate();
        userService.changeEmail(getOwnername(), body).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.body() != null) { //The body is a User object & ErrorBody is empty
                    owner.setValue(response.body());
                    makeToast("New Email is: "+owner.getValue().getEmail());
                    close.setValue(true);
                }else{
                    makeToast("Upps: "+ NetworkUtils.parseError(response).getMessage());
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                makeToast("Error: "+t.getCause());
            }
        });
        return close;
    }

    public LiveData<Boolean> deleteAccount(String password){
        Log.d(this.getClass().toString(), "deleteAccount");
        final MutableLiveData<Boolean> close = new MutableLiveData<Boolean>();
        RequestBody body = new GenericBody().put("password", password).generate();
        userService.deleteAccount(getOwnername(),body).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if(response.body() != null) {
                    makeToast("We will miss you!");
                    close.setValue(true);
                }else{
                    makeToast("Upps: "+ NetworkUtils.parseError(response).getMessage());
                }
            }
            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                makeToast("Error: "+t.getCause());
            }
        });
        return close;
    }

    public LiveData<Boolean> changePicture(Bitmap bitmap){
        Log.d(this.getClass().toString(), "changePicture");
        final MutableLiveData<Boolean> close = new MutableLiveData<Boolean>();
        //Compress & Convert bitmap so it is absolutely sure under 500kb
        byte[] byteArray = SzUtils.compressWithJpgToByte(bitmap);
        Log.i("changePicture |", "Final Bitmap Size is: "+byteArray.length);

        //Retrofit 2 Multipart > https://medium.com/@adinugroho/upload-image-from-android-app-using-retrofit-2-ae6f922b184c
        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"),byteArray);
        MultipartBody.Part picture = MultipartBody.Part.createFormData("profile_picture",getOwnername()+".profile_picture"+".jpg",reqFile);
        userService.changePicture(getOwnername(), picture).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.body() != null) { //The body is a User object & ErrorBody is empty
                    owner.setValue(response.body());
                    makeToast("Looks Good!");
                    close.setValue(true);
                }else {
                    makeToast("Upps: " + NetworkUtils.parseError(response).getMessage());
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                t.printStackTrace();
            }
        });
        return close;
    }


}