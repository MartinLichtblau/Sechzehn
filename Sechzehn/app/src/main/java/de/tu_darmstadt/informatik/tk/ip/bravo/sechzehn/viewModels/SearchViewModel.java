package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.viewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.ServiceGenerator;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.Services.UserService;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.SzUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by marti on 19.07.2017.
 */

public class SearchViewModel extends ViewModel {
    private final String TAG = "SearchViewModel";
    private UserService userService;

    public void initialize(){
        userService = ServiceGenerator.createService(UserService.class,SzUtils.getToken());
        //venuesService
    }

    public LiveData<List<User>> getFriends(@Nullable Integer page, @Nullable Integer perPage){
        Log.d(TAG, "getFriends");
        final MutableLiveData<List<User>> friendList = new MutableLiveData<List<User>>();
        userService.getFriends(SzUtils.getOwnername(), page, perPage).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if(response.body() != null) { //The body is a List of Users & ErrorBody is empty
                    Log.d(TAG, "getFriends: "+response.body());
                    friendList.setValue(response.body());
                }else{
                    //Log.d(TAG, "getFriends: "+NetworkUtils.parseError(response).getMessage());
                }
            }
            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {

            }
        });
        return friendList;
    }


}
