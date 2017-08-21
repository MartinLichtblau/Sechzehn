package de.tu_darmstadt.informatik.tk. ip.bravo.sechzehn.network.services;


import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Pagination;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Venue;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.ServiceGenerator;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by marti on 21.08.2017.
 */

public interface VenueService {

    @GET("venues")
    Call<Pagination<Venue>> getVenues(
            @Query("page") Integer page,
            @Query("per_page") Integer perPage,
            @Query("lat") Double lat,
            @Query("lng") Double lng,
            @Query("radius") Double radius,
            @Query("section") String section,
            @Query("query") String query,
            @Query("price") Integer price,
            @Query("time") String time);

   public VenueService VenueService = ServiceGenerator.createService(VenueService.class);
}
