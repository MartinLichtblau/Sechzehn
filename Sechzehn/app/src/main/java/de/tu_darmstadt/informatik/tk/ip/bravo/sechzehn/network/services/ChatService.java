package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.LastChat;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Message;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Pagination;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.ServiceGenerator;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * @author Alexander Geiß on 03.08.2017.
 */

public interface ChatService {
    @GET("messages")
    Call<Pagination<LastChat>> getLastChats(
            @Nullable @Query("page") Integer page,
            @Nullable @Query("per_page") Integer perPage
    );

    @GET("messages/{username}")
    Call<Pagination<Message>> getMessages(
            @NonNull @Path("username") String username,
            @Nullable @Query("page") Integer page,
            @Nullable @Query("per_page") Integer perPage
    );

    public static ChatService ChatService = ServiceGenerator.createService(ChatService.class);
}
