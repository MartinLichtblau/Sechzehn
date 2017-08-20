package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents the Friendship between two users.
 *
 * @author Alexander Geiß on 10.08.2017.
 */

public class Friendship {
    public Friendship() {
    }

    public Friendship(Status status) {
        this.status = status;
    }

    /**
     * Represents the status of a friendship
     */
    //Ref > http://blog.jensdriller.com/simple-deserialization-of-java-enums-using-google-gson-annotations/
    public enum Status {
        @SerializedName("NONE")NONE,
        @SerializedName("SELF")SELF,
        @SerializedName("CONFIRMED")CONFIRMED,
        @SerializedName("RELATED_CONFIRMED")RELATED_CONFIRMED,
        @SerializedName("RELATING_CONFIRMED")RELATING_CONFIRMED,
        @SerializedName("DECLINED")DECLINED,
        UNKNOWN
    }

    /**
     *
     */
    @SerializedName("relating_user")
    public String relatingUser;
    /**
     * When somebody asks the authenticated User, the status is RELATED_CONFIRMED.
     * When the authenticated User has asked the related User, the status is RELATING_CONFIRMED.
     */
    public Status status;
    /**
     *
     */
    @SerializedName("related_user")
    public User relatedUser;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Friendship that = (Friendship) o;

        if (relatingUser != null ? !relatingUser.equals(that.relatingUser) : that.relatingUser != null)
            return false;
        if (status != that.status) return false;
        return relatedUser != null ? relatedUser.equals(that.relatedUser) : that.relatedUser == null;

    }

    @Override
    public int hashCode() {
        int result = relatingUser != null ? relatingUser.hashCode() : 0;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (relatedUser != null ? relatedUser.hashCode() : 0);
        return result;
    }
}
