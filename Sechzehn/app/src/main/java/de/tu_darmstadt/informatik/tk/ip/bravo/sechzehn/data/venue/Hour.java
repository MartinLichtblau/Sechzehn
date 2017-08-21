package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.venue;

import com.google.gson.annotations.SerializedName;

/**
 * @author Alexander Geiß on 21.08.2017.
 */

public class Hour {
    public Day day;
    public String start;
    public String end;


    /**
     *
     */

    public enum Day {
        @SerializedName("Sun")SUN,
        @SerializedName("Mon")MON,
        @SerializedName("Tue")TUE,
        @SerializedName("Wed")WED,
        @SerializedName("Thu")THU,
        @SerializedName("Fri")FRI,
        @SerializedName("Sat")SAT,
        UNKNOWN
    }
}
