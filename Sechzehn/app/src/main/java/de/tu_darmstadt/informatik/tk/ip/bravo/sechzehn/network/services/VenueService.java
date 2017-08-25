package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Pagination;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Venue;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.venue.CheckIn;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.ServiceGenerator;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Provides interaction with the Sechzehn API related to Venues.
 *
 * @author Alexander Geiß on 20.08.2017.
 */

public interface VenueService {
    /**
     * Get a paginated list of Venues for optional search parameters. The default sorting order is
     * by rating descending.
     *
     * @param page     the page number. Default: 1.
     * @param perPage the number of elements per page. Default: 10.
     * @param lat      the latitude of the point to search around. Can only be used together with
     *                 lng.
     * @param lng      the longitude of the point to search around. Can only be used together with
     *                 lat.
     * @param radius   the search radius around the point in km. Can only be used together with lat
     *                 and lng. Default: 10.
     * @param section  the section for quick searches.
     * @param query    the search parameter for the Venue name or the Category.
     * @param price    the price ranking.
     * @param time
     * @return Call which gets a paginated venue list.
     */
    @GET("venues")
    Call<Pagination<Venue>> getVenues(
            @Query("page") Integer page,
            @Query("per_page") Integer perPage,
            @Query("lat") Double lat,
            @Query("lng") Double lng,
            @Query("radius") Double radius,
            @Query("section") Venue.Section section,
            @Query("query")  String query,
            @Query("price") Integer price,
            @Query("time") String time);
    /**
     * Gets detail info about a venue.
     *
     * @param id the id of the venue.
     * @return Call which gets the corresponding venue.
     */
    @GET("venues/{id}")
    Call<Venue> getVenue(@Path("id") String id);

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
     * An instance of the {@link VenueService}.
     */
    VenueService VenueService = ServiceGenerator.createService(VenueService.class);
}
