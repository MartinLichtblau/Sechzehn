package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

/**
 * Created by JohannesTP on 21.06.2017.
 */

public class User {
    public int id;

    public String email;

    public String username;

    public String password;

    @SerializedName("password_confirmation")
    public String passwordConfirmation;

    @SerializedName("real_name")
    public String realName;

    public String city;

    @SerializedName("profile_picture")
    public String profilePicture;

    public double lat;

    public double lng;

    public LatLng getLatLng() {
        return new LatLng(lat, lng);
    }
}
