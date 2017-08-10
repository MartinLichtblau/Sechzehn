
package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class User {


    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("username")
    @Expose
    private String username;

    @SerializedName("real_name")
    @Expose
    private String realName;
    @SerializedName("city")
    @Expose
    private String city;
    @SerializedName("profile_picture")
    @Expose
    private String profilePicture;
    @SerializedName("date_of_birth")
    @Expose
    private String dateOfBirth;
    @SerializedName("lat")
    @Expose
    private Double lat;
    @SerializedName("lng")
    @Expose
    private Double lng;
    @Expose
    private String password;
    @Expose
    private String email;
    @SerializedName("incognito")
    @Expose
    private boolean incognito;
    @SerializedName("confirmed")
    @Expose
    private boolean confirmed;

    @SerializedName("friendship_status")
    @Expose
    private Friendship.Status friendshipStatus;

    public boolean isIncognito() {
        return incognito;
    }

    public void setIncognito(boolean incognito) {
        this.incognito = incognito;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealName() {
        return realName;
    }

    public String getOptionalRealName() {
        return realName != null ? realName : username;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Friendship.Status getFriendshipStatus() {
        return friendshipStatus;
    }

    public void setFriendshipStatus(Friendship.Status friendshipStatus) {
        this.friendshipStatus = friendshipStatus;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", realName='" + realName + '\'' +
                ", city='" + city + '\'' +
                ", profilePicture='" + profilePicture + '\'' +
                ", dateOfBirth='" + dateOfBirth + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", incognito=" + incognito +
                ", confirmed=" + confirmed +
                ", friendshipStatus=" + friendshipStatus +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (incognito != user.incognito) return false;
        if (confirmed != user.confirmed) return false;
        if (id != null ? !id.equals(user.id) : user.id != null) return false;
        if (username != null ? !username.equals(user.username) : user.username != null)
            return false;
        if (realName != null ? !realName.equals(user.realName) : user.realName != null)
            return false;
        if (city != null ? !city.equals(user.city) : user.city != null) return false;
        if (profilePicture != null ? !profilePicture.equals(user.profilePicture) : user.profilePicture != null)
            return false;
        if (dateOfBirth != null ? !dateOfBirth.equals(user.dateOfBirth) : user.dateOfBirth != null)
            return false;
        if (lat != null ? !lat.equals(user.lat) : user.lat != null) return false;
        if (lng != null ? !lng.equals(user.lng) : user.lng != null) return false;
        if (password != null ? !password.equals(user.password) : user.password != null)
            return false;
        return email != null ? email.equals(user.email) : user.email == null;

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}