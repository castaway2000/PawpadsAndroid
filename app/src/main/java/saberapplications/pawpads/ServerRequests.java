package saberapplications.pawpads;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

/**
 * Created by blaze on 10/22/2015.
 */
public class ServerRequests {

    ProgressDialog progressDialog;
    public static final int CONNECTION_TIME = 1000 * 15;
    public static final String SERVER_ADDRESS = "http://pawpadstest.comuv.com/";

    public ServerRequests(Context context){
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Processing");
        progressDialog.setMessage("Please wait...");
    }

    public void storeUserDataInBackground(User user, GetUserCallback userCallback){
        progressDialog.show();
        new StoreUserDataAsyncTask(user, userCallback).execute();
    }
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
    //TODO: password hashing
        User user;
        GetUserCallback userCallback;

        public StoreUserDataAsyncTask(User user, GetUserCallback userCallback){
            ServerRequests.StoreUserDataAsyncTask.this.user = user;
            ServerRequests.StoreUserDataAsyncTask.this.userCallback = userCallback;
        }

        @Override
        protected Void doInBackground(Void... params) {
            ArrayList<NameValuePair> dataToSend = new ArrayList<>();
            dataToSend.add(new BasicNameValuePair("name",user.name));
            dataToSend.add(new BasicNameValuePair("age",user.age+""));
            dataToSend.add(new BasicNameValuePair("username",user.username));
            dataToSend.add(new BasicNameValuePair("password", user.password));

            HttpParams httpRequestParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpRequestParams, CONNECTION_TIME);
            HttpConnectionParams.setSoTimeout(httpRequestParams, CONNECTION_TIME);

            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpPost post = new HttpPost(SERVER_ADDRESS + "Register.php");

            try{
                post.setEntity(new UrlEncodedFormEntity(dataToSend));
                client.execute(post);
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
    //TODO: password hashing
    User user;
        GetUserCallback userCallback;

        public FetchUserDataAsyncTask(User usr, GetUserCallback userCB) {
            ServerRequests.FetchUserDataAsyncTask.this.user = usr;
            ServerRequests.FetchUserDataAsyncTask.this.userCallback = userCB;
        }

        @Override
        protected User doInBackground(Void... params) {
            ArrayList<NameValuePair> dataToSend = new ArrayList<>();
            dataToSend.add(new BasicNameValuePair("username",user.username));
            dataToSend.add(new BasicNameValuePair("password", user.password));

            HttpParams httpRequestParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpRequestParams, CONNECTION_TIME);
            HttpConnectionParams.setSoTimeout(httpRequestParams, CONNECTION_TIME);

            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpPost post = new HttpPost(SERVER_ADDRESS + "FetchUserData.php");

            User returnedUser = null;
            try{
                post.setEntity(new UrlEncodedFormEntity(dataToSend));
                HttpResponse httpResponse = client.execute(post);

                HttpEntity entity = httpResponse.getEntity();
                String result = EntityUtils.toString(entity);
                JSONObject jObject = new JSONObject(result);

                if(jObject.length() == 0){
                    returnedUser = null;
                }
                else
                {
                    String name = jObject.getString("name");
                    int age = jObject.getInt("age");
                    returnedUser = new User(name, age, user.username, user.password);
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
    //TODO: photo handling, null photo returns.
    //TODO: lat long handeling.
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
            HttpGet get = new HttpGet(SERVER_ADDRESS + "test.php");

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
                    returnedUser = null;
                }
                else
                {
                    for(int i = 0; i < jArray.length(); i++){
                        jObject = jArray.getJSONObject(i);

                        lUsername.add(i, jObject.getString("username"));

                        if(jObject.getString("profile") != null){
                            lProfile.add(i,jObject.getString("profile"));
                        }else{lProfile.add(i, "this user has not set up a description yet");}

                        if(jObject.getString("pic") != null) {
                            lPic.add(i, jObject.getString("pic"));
                        }
                        else{lPic.add(i,"http://pawpadstest.comuv.com/pictures/btn_star_big_on.png");}

                        if (jObject.getString("distance") != null) {
                            lDistance.add(i, jObject.getString("distance"));
                        }else{lDistance.add(i,Integer.toString(i));}
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
}
