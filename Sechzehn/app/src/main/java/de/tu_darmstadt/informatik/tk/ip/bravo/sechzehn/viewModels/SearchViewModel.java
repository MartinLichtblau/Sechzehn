package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.viewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.location.Location;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public MutableLiveData<HashMap<Marker, MarkerOptions>> userMarkers = new MutableLiveData<>();
    /*public MutableLiveData<HashMap<Venue,Marker>> venueMap = new MutableLiveData<>();*/

    public MutableLiveData<HashMap<User,Marker>> getUserMap(){
        return userMap;
    }

    /*public MutableLiveData<HashMap<Venue,Marker>> getVenueMap(){
        return venueMap;
    }*/

    public MutableLiveData<Resource> searchXUsersNearby(Integer numberUsers, Double lat, Double lng, Double radius){
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
}
