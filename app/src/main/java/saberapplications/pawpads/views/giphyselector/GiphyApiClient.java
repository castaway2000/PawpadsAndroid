package saberapplications.pawpads.views.giphyselector;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Stanislav Volnjanskij on 2/10/17.
 */

public class GiphyApiClient {
    public static final String BASE_URL = "http://api.giphy.com/v1/";

    static GiphyAPI instance;
    GiphyApiClient(){
    }
    public static GiphyAPI getService(){
        if (instance==null){

            OkHttpClient defaultHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(new HeaderInterceptor())
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(defaultHttpClient)
                    .build();
            instance = retrofit.create(GiphyAPI.class);
        }
        return instance;
    }
}
