package saberapplications.pawpads;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

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
    public static final String SERVER_ADDRESS = "http://pawpads.byethost8.com/";

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



    public class StoreUserDataAsyncTask extends AsyncTask<Void, Void, Void>{
        User user;
        GetUserCallback userCallback;

        public StoreUserDataAsyncTask(User user, GetUserCallback userCallback){
            this.user = user;
            this.userCallback = userCallback;
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



    public class FetchUserDataAsyncTask extends AsyncTask<Void, Void, User> {
        User user;
        GetUserCallback userCallback;

        public FetchUserDataAsyncTask(User user, GetUserCallback userCallback) {
            this.user = user;
            this.userCallback = userCallback;
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




    //FETCH ALL DATA

    public class FetchListDataAsyncTask extends AsyncTask<Void, Void, UserList> {
        UserList user;
        GetUserListCallback userCallback;
        public String[] aAge = {};
        public String[] aUsername = {};
        public String[] aName = {};

        public FetchListDataAsyncTask(UserList user, GetUserListCallback userCallback) {
            this.user = user;
            this.userCallback = userCallback;
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
//                HttpResponse httpResponse = client.execute(get);
//                HttpEntity entity = httpResponse.getEntity();
//
//                String result = EntityUtils.toString(entity);
//                JSONObject jObject = new JSONObject(result);
//                JSONArray jsonArray = new JSONArray(jObject);


                ArrayList<String> lName = new ArrayList<>();
                ArrayList<String> lAge = new ArrayList<>();
                ArrayList<String> lUsername = new ArrayList<>();

                lName.add("blart0");
                lName.add("blart1");
                lName.add("blart2");
                lName.add("blart3");
                lName.add("blart4");

                lAge.add("0");
                lAge.add("1");
                lAge.add("2");
                lAge.add("3");
                lAge.add("4");

                lUsername.add("wewt0");
                lUsername.add("wewt1");
                lUsername.add("wewt2");
                lUsername.add("wewt3");
                lUsername.add("wewt4");

//                if(jObject.length() == 0){
//                    returnedUser = null;
//                }
//                else
//                {
//                    for(int i = 0; i < jObject.length(); i++){
//
//                         //= jsonArray.getJSONObject(i);
//
//                        String name = jObject.getString("name");
//                        lName.add(i,name);
//
//                        int age = jObject.getInt("age");
//                        lAge.add(i, Integer.toString(age));
//
//                        String username = jObject.getString("username");
//                        lUsername.add(i, username);
//                }

                    aAge = lAge.toArray(new String[lAge.size()]);
                    aName = lName.toArray(new String[lName.size()]);
                    aUsername = lUsername.toArray(new String[lUsername.size()]);

                    returnedUser = new UserList(aName, aAge, aUsername);
//                }
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
