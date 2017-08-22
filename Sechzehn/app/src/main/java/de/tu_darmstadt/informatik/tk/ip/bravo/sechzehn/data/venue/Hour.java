package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.venue;

import com.google.gson.annotations.SerializedName;

/**
 * @author Alexander Geiß on 21.08.2017.
 */

public class Hour {
    public Hour() {
    }

    public Hour(Day day, String start, String end) {
        this.day = day;
        this.start = start;
        this.end = end;
    }

    public Day day;
    public String start;
    public String end;


    /**
     *
     */

    public enum Day {

        @SerializedName("Mon")MON(1, "Mon"),
        @SerializedName("Tue")TUE(2, "Tue"),
        @SerializedName("Wed")WED(3, "Wed"),
        @SerializedName("Thu")THU(4, "Thu"),
        @SerializedName("Fri")FRI(5, "Fri"),
        @SerializedName("Sat")SAT(6, "Sat"),
        @SerializedName("Sun")SUN(7, "Sun"),
        UNKNOWN(0, "");
        private final int id;
        private final String name;

        Day(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getDayNumber() {
            return id;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
