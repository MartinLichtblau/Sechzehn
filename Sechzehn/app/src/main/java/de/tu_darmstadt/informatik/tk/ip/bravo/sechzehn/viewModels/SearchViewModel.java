package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.viewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.location.Location;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Pagination;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Resource;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.ServiceGenerator;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.Services.UserService;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.NetworkUtils;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.SzUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by marti on 19.07.2017.
 */

public class SearchViewModel extends ViewModel {
    private final String TAG = "SearchViewModel";
    private static final UserService userService = ServiceGenerator.createService(UserService.class,SzUtils.getToken());

    public MutableLiveData<Resource> getXUsersNearby(Integer numberUsers, Double lat, Double lng, Double radius){
        return getUsers(null, numberUsers, lat, lng,radius,null,null);
    }

    public MutableLiveData<Resource> getUsers(Integer page, Integer perPage, Double lat, Double lng, Double radius, Boolean isFriend, String searchedUsername){
        final MutableLiveData<Resource> resource = new MutableLiveData<>();
        resource.setValue(Resource.loading(null));

        userService.getUsers(page, perPage, lat, lng, radius, isFriend, searchedUsername)
                .enqueue(new Callback<Pagination<User>>(){
            @Override
            public void onResponse(Call<Pagination<User>> call, Response<Pagination<User>> response) {
                if (response.isSuccessful())
                    resource.setValue(Resource.success(response.body()));
                else
                    resource.setValue(Resource.error(NetworkUtils.parseError(response).getMessage(), null));
            }
            @Override
            public void onFailure(Call<Pagination<User>> call, Throwable t) {
                resource.setValue(Resource.error(t.getCause().toString(),null));
            }
        });
        return resource;
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
