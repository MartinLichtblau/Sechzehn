package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services;

import android.support.annotation.NonNull;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.venue.CheckIn;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.ServiceGenerator;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Provides interaction with the Sechzehn API related to CheckIns.
 *
 * @author Alexander Geiß on 21.08.2017.
 */

public interface CheckInService {


    /**
     * Check-in into Venue.
     *
     * @param venueId The id of the venue.
     * @param checkIn A {@link CheckIn} object with rating set.
     * @return Call which results in a {@link CheckIn} object.
     */
    @POST("venues/{venue_id}/check-ins")
    Call<CheckIn> checkIn(
            @NonNull @Path("venue_id") String venueId,
            @NonNull @Body CheckIn checkIn
    );

    /**
     * An instance of the {@link CheckInService}.
     */
    CheckInService CheckInService = ServiceGenerator.createService(CheckInService.class);
}
