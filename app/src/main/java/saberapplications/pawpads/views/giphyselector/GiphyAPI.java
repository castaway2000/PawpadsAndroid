package saberapplications.pawpads.views.giphyselector;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Stanislav Volnjanskij on 2/10/17.
 */

public interface GiphyAPI {
    @GET("gifs/search")
    Call<JsonObject> getGifs(@Query("q") String query,@Query("limit") int limit,@Query("offset") int offset);
}
