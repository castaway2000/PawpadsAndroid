package saberapplications.pawpads;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;


import com.google.gson.JsonElement;

import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;

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

    /*
    public void fetchUserDataInBackground(User user, GetUserCallback userCallback){
        progressDialog.show();
        new FetchUserDataAsyncTask(user, userCallback).execute();

    }
    public void fetchListDataInBackground(UserList user, GetUserListCallback userCallback){
        progressDialog.show();
        new FetchListDataAsyncTask(user, userCallback).execute();

    }


//REGISTRATION ASYNC TASK
    public class StoreUserDataAsyncTask extends AsyncTask<Void, Void, Void>{
        User user;
        GetUserCallback userCallback;

        public StoreUserDataAsyncTask(User user, GetUserCallback userCallback){
            ServerRequests.StoreUserDataAsyncTask.this.user = user;
            ServerRequests.StoreUserDataAsyncTask.this.userCallback = userCallback;
        }

        @Override
        protected Void doInBackground(Void... params) {



            HttpParams httpRequestParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpRequestParams, CONNECTION_TIME);
            HttpConnectionParams.setSoTimeout(httpRequestParams, CONNECTION_TIME);


            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpGet get = new HttpGet(SERVER_ADDRESS + "Register.php?username="+user.username+
                    "&password="+user.password+"&lat="+LAT+"&lng="+LNG+"&email="+user.email);

            try{
                client.execute(get);
            }catch(Exception e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid){
            progressDialog.dismiss();
            userCallback.done(null);
            super.onPostExecute(aVoid);
        }
    }


//LOGIN ASYNC TASK
    public class FetchUserDataAsyncTask extends AsyncTask<Void, Void, User> {
    User user;
        GetUserCallback userCallback;

        public FetchUserDataAsyncTask(User usr, GetUserCallback userCB) {
            ServerRequests.FetchUserDataAsyncTask.this.user = usr;
            ServerRequests.FetchUserDataAsyncTask.this.userCallback = userCB;
        }

        @Override
        protected User doInBackground(Void... params) {

            HttpParams httpRequestParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpRequestParams, CONNECTION_TIME);
            HttpConnectionParams.setSoTimeout(httpRequestParams, CONNECTION_TIME);

            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpGet get = new HttpGet(SERVER_ADDRESS + "FetchUserData.php?username="+user.username+"&password="+user.password);

            User returnedUser = null;
            try{
                HttpResponse httpResponse = client.execute(get);

                HttpEntity entity = httpResponse.getEntity();
                String result = EntityUtils.toString(entity);
                JSONArray jArray = new JSONArray(result);

                for(int i=0; i < jArray.length(); i++){
                    if("true".equals(jArray.get(i))){
                        returnedUser = new User(user.username, user.password);
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            return returnedUser;
        }

        @Override
        protected void onPostExecute(User returnedUser){
            progressDialog.dismiss();
            userCallback.done(returnedUser);
            super.onPostExecute(returnedUser);
        }

    }


//FETCH ALL DATA FOR LISTS
    public class FetchListDataAsyncTask extends AsyncTask<Void, Void, UserList> {
        UserList user;
        GetUserListCallback userCallback;
        public String[] aUsername = {};
        public String[] aProfile = {};
        public String[] aPic = {};
        public String[] aDistance = {};



    public FetchListDataAsyncTask(UserList user, GetUserListCallback userCallback) {
            FetchListDataAsyncTask.this.user = user;
            FetchListDataAsyncTask.this.userCallback = userCallback;
        }

        @Override
        protected UserList doInBackground(Void... params) {
            HttpParams httpRequestParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpRequestParams, CONNECTION_TIME);
            HttpConnectionParams.setSoTimeout(httpRequestParams, CONNECTION_TIME);

            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpGet get = new HttpGet(SERVER_ADDRESS + "test.php?lat="+LAT+"&lng="+LNG+"&username="+USER);

            UserList returnedUser = null;
            try{

                HttpResponse httpResponse = client.execute(get);
                HttpEntity entity = httpResponse.getEntity();

                String result = EntityUtils.toString(entity);
                JSONArray jArray =  new JSONArray(result);
                JSONObject jObject = new JSONObject();


                ArrayList<String> lUsername = new ArrayList<>();
                ArrayList<String> lProfile = new ArrayList<>();
                ArrayList<String> lPic = new ArrayList<>();
                ArrayList<String> lDistance = new ArrayList<>();

                if(jArray.length() == 0){
                    returnedUser = new UserList(aUsername, aProfile, aPic, aDistance);;
                }
                else
                {
                    for(int i = 0; i < jArray.length(); i++){
                        jObject = jArray.getJSONObject(i);

                        String username = jObject.getString("username");
                        if(username.equals(USER)) {
                            continue;
                        }

                        lUsername.add(username);

                        if(jObject.getString("profile") != null) {
                            lProfile.add(jObject.getString("profile"));
                        }
                        else {
                            lProfile.add("this user has not set up a description yet");
                        }

                        if(jObject.isNull("image_url")) {
                            lPic.add(SERVER_ADDRESS+"pictures/btn_star_big_on.png");
                        }
                        else {
                            lPic.add(jObject.getString("image_url"));
                        }

                        if (jObject.getString("distance") != null) {
                            lDistance.add(jObject.getString("distance"));
                        }
                        else {
                            lDistance.add(Integer.toString(i));
                        }
                }
                    aUsername = lUsername.toArray(new String[lUsername.size()]);
                    aProfile = lProfile.toArray(new String[lProfile.size()]);
                    aPic = lPic.toArray(new String[lPic.size()]);
                    aDistance = lDistance.toArray(new String[lDistance.size()]);

                    returnedUser = new UserList(aUsername, aProfile, aPic, aDistance);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            return returnedUser;
        }

        @Override
        protected void onPostExecute(UserList returnedUser){
            progressDialog.dismiss();
            userCallback.done(returnedUser);
            super.onPostExecute(returnedUser);
        }
    }
*/

}
