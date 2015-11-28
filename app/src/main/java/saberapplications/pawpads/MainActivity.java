package saberapplications.pawpads;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.squareup.okhttp.OkHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;


public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    String TAG = "MAIN";
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    Context context;
    String regid;
    String msg;
    String USERNAME;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView listView;
    UserLocalStore userLocalStore;
    UserData ud = new UserData(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();
        listView = (ListView) findViewById(R.id.listView);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipelayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        userLocalStore = new UserLocalStore(this);

    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        Util.PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(Util.PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing registration ID is not guaranteed to work with
        // the new app version.
        int registeredVersion = prefs.getInt(Util.PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    private boolean isUserRegistered(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String User_name = prefs.getString(Util.USER_NAME, "");
        if (User_name.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return false;
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_profileID:
                Intent i = new Intent(MainActivity.this, profileEditPage.class);
                startActivity(i);
                return true;

            case R.id.action_logout:
                userLocalStore = new UserLocalStore(this);
                userLocalStore.clearUserData();
                userLocalStore.setUserLoggedIn(false);
                startActivity(new Intent(this, Login.class));
                finish();

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (authenticate()) {
            ud.getUserData();
            String username = userLocalStore.getLoggedInUser().username;
            this.USERNAME = username;

            if (!isUserRegistered(context)) {
                sendRegistrationIdToBackend();

                if (checkPlayServices()) {
                    gcm = GoogleCloudMessaging.getInstance(this);
                    this.regid = getRegistrationId(context);

                    if (regid.isEmpty()) {
                        registerInBackground();
                    }

                } else {
                    Log.i("MAIN", "No valid Google Play Services APK found.");
                }
            }

            listView.setOnItemClickListener(
                    new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            performClickAction(position);
                        }
                    }
            );
        } else {
            startActivity(new Intent(MainActivity.this, Login.class));
            finish();
        }
    }

    private boolean authenticate() {
        return userLocalStore.getUserLoggedIn();
    }


    public void setListView(UserList userList) {
        final ListAdapter listAdapter = new CustomAdapter(this, ud.user, ud.upics, ud.descr, ud.geol, ud.email);
        listView.setAdapter(listAdapter);

        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    public void performClickAction(int position) {
        ud.getUserData();
        final CustomAdapter ca = new CustomAdapter(this, ud.user, ud.upics, ud.descr, ud.geol, ud.email);

        Intent i = new Intent(MainActivity.this, profilepage.class);
        i.putExtra("value", ca.descrip[position]);
        i.putExtra("image", ca.pics[position]);
        i.putExtra("username", ca.user[position]);
        i.putExtra("location", ca.geoloc[position]);
        i.putExtra("email", ca.email[position]);
        startActivity(i);
    }

    @Override
    public void onRefresh() {
        ud.getUserData();
    }


    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */


    private void registerInBackground() {
        new AsyncTask() {


            @Override
            protected String doInBackground(Object[] params) {


                try {

                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(MainActivity.this);
                    }
                    regid = gcm.register(Util.SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;


                    // You should send the registration ID to your server over HTTP,
                    //GoogleCloudMessaging gcm;/ so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    // sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the registration ID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;


            }
        }.execute();

    }


    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Util.PROPERTY_REG_ID, regId);
        editor.putInt(Util.PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    private void storeUserDetails(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        // editor.putString(Util.EMAIL, editText_email.getText().toString());
        // editor.putString(Util.USER_NAME, editText_user_name.getText().toString());
        editor.commit();
    }

    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the registration ID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }


    private RequestQueue mRequestQueue;
    private void sendRegistrationIdToBackend() {
        // Your implementation here.
        new SendGcmToServer().execute();
        new smppLogin().execute();
// Access the RequestQueue through your singleton class.
        // AppController.getInstance().addToRequestQueue(jsObjRequest, "jsonRequest");

    }

    private class SendGcmToServer extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... params) {

            String url = Util.pawpadsURL + "updateGcmUser.php?username=" + USERNAME + "&regid=" + regid;
            Log.i("pavan", "url" + url);

            OkHttpClient client_for_getMyFriends = new OkHttpClient();

            String response = null;
            // String response=Utility.callhttpRequest(url);

            try {
                url = url.replace(" ", "%20");
                response = callOkHttpRequest(new URL(url),
                        client_for_getMyFriends);
                for (String subString : response.split("<script", 2)) {
                    response = subString;
                    break;
                }
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            //Toast.makeText(context,"response "+result,Toast.LENGTH_LONG).show();

            if (result != null) {
                if (result.equals("success")) {

                    storeUserDetails(context);
                    startActivity(new Intent(MainActivity.this, ChatActivity.class));
                    finish();

                } else {

                    Toast.makeText(context, "Try Again" + result, Toast.LENGTH_LONG).show();
                }


            } else {

                Toast.makeText(context, "Check net connection ", Toast.LENGTH_LONG).show();
            }

        }

    }


    // Http request using OkHttpClient
    String callOkHttpRequest(URL url, OkHttpClient tempClient)
            throws IOException {

        HttpURLConnection connection = tempClient.open(url);

        connection.setConnectTimeout(40000);
        InputStream in = null;
        try {
            // Read the response.
            in = connection.getInputStream();
            byte[] response = readFully(in);
            return new String(response, "UTF-8");
        } finally {
            if (in != null)
                in.close();
        }
    }

    byte[] readFully(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int count; (count = in.read(buffer)) != -1; ) {
            out.write(buffer, 0, count);
        }
        return out.toByteArray();
    }



    private String getUserName(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String User_name = prefs.getString(Util.USER_NAME, "");
        Log.d("pavan","username in main "+User_name);
        return User_name;

    }


    private class smppLogin extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... arg0) {

            //TODO: make this work appropriately
            String VerifyUserURL = Util.SERVER+"/plugins/userService/userservice?type=add&secret="+Util.XMPP_SECREAT_KEY+"&"
                    + "username="
                    +ud.user
                    + "&password="+Util.XMPP_PASSWORD+"&name="
                    + ud.user
                    + "&email="
                    + ud.email;
            VerifyUserURL = VerifyUserURL.replace(" ", "%20");
            Log.i("pavan", "BEFORE CONVERTING URL::::  " + VerifyUserURL);

            OkHttpClient client_send_code = new OkHttpClient();
            String response = null;
            try {
                response = callOkHttpRequest(new URL(VerifyUserURL),
                        client_send_code);
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (response != null) {
                return response;
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }

                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("pavan", "server said: " + result);

            if (result != null) {
                storeUserDetails(context);
                Intent chatActivity=new Intent(MainActivity.this,ChatActivity.class);
                chatActivity.putExtra("user_id",ud.user);
                startActivity(chatActivity);
            } else {
                Toast.makeText(MainActivity.this,"error",Toast.LENGTH_LONG).show();
            }

        }

    }



}


