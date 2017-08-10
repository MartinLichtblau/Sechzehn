package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents the Friendship between two users.
 * @author Alexander Geiß on 10.08.2017.
 */

public class Friendship {
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


}
