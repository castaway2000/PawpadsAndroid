package saberapplications.pawpads;

import android.app.ProgressDialog;
import android.content.Context;

import com.google.gson.JsonElement;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Query;


/**
 * Created by blaze on 10/22/2015.
 */
public class ServerRequests{
    ProgressDialog progressDialog;
    public static final int CONNECTION_TIME = 1000 * 15;
    public static final String SERVER_ADDRESS = "http://www.szablya.com/saberapps/pawpads/";
    Double LAT;
    Double LNG;
    String USER;

    interface  API{
        @GET("/test.php")
        public void register(@Query("username") String userName,@Query("password") String password,@Query("email") String email,
                             @Query("lat") double lat,@Query("lng") double lng, Callback<JsonElement> callback);
    }

    public ServerRequests(Context context, Double lat, Double lng, String username){
        this.LAT = lat;
        this.LNG = lng;
        this.USER = username;
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Processing");
        progressDialog.setMessage("Please wait...");
    }

    public ServerRequests(Context context){
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Processing");
        progressDialog.setMessage("Please wait...");
    }


    public void storeUserDataInBackground(User user, final GetUserCallback userCallback){
        progressDialog.show();
        RestAdapter restAdapter= new RestAdapter.Builder()
                .setEndpoint(SERVER_ADDRESS)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();

        API apiClient = restAdapter.create(API.class);

        apiClient.register(user.username, user.password,user.email,LAT, LNG, new Callback<JsonElement>() {
            @Override
            public void success(JsonElement element, Response response) {
                userCallback.done(null);
                progressDialog.dismiss();
            }

            @Override
            public void failure(RetrofitError error) {
                userCallback.done(null);
                progressDialog.dismiss();

            }
        });

    }
}
