package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Pair of Marker and MarkerOptions.
 * Created by marti on 28.08.2017.
 */

public class MarkerMarkerOptions {
    public Marker marker;
    public MarkerOptions markerOptions;

    public MarkerMarkerOptions(Marker marker, MarkerOptions markerOptions) {
        this.marker = marker;
        this.markerOptions = markerOptions;
    }


}
