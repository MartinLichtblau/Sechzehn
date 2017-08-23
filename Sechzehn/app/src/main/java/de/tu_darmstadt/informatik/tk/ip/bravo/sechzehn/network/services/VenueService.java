package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services;

import android.support.annotation.IntRange;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Pagination;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Venue;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.ServiceGenerator;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

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
     * @param per_page the number of elements per page. Default: 10.
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
    Call<Pagination<Venue>> getVenues(Integer page, Integer per_page, Double lat, Double lng, Double radius, Venue.Section section, String query, @IntRange(from = 1, to = 5) Integer price, String time);

    /**
     * Gets detail info about a venue.
     *
     * @param id the id of the venue.
     * @return Call which gets the corresponding venue.
     */
    @GET("venues/{id}")
    Call<Venue> getVenue(@Path("id") String id);

    /**
     * An instance of the {@link VenueService}.
     */
    VenueService VenueService = ServiceGenerator.createService(VenueService.class);
}