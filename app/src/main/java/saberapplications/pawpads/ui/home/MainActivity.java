package saberapplications.pawpads.ui.home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChat;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBPrivateChat;
import com.quickblox.chat.QBPrivateChatManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBMessageListener;
import com.quickblox.chat.listeners.QBPrivateChatManagerListener;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.messages.QBMessages;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBSubscription;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import saberapplications.pawpads.ui.chat.ChatActivity;
import saberapplications.pawpads.GPS;
import saberapplications.pawpads.Login;
import saberapplications.pawpads.R;
import saberapplications.pawpads.UserList;
import saberapplications.pawpads.UserLocalStore;
import saberapplications.pawpads.Util;
import saberapplications.pawpads.profileEditPage;


public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    String TAG = "MAIN";
    GoogleCloudMessaging gcm;
    SharedPreferences prefs;
    Context context;
    String regid;
    String msg;


    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView listView;
    UserLocalStore userLocalStore;
    private UserListAdapter adapter;

    private QBPrivateChatManagerListener chatListener=new QBPrivateChatManagerListener() {
        @Override
        public void chatCreated(QBPrivateChat qbPrivateChat, final boolean createdLocally) {
            if (!createdLocally) {
                qbPrivateChat.addMessageListener(new QBMessageListener() {
                    @Override
                    public void processMessage(final QBChat qbChat, final QBChatMessage qbChatMessage) {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("New Chat Message")
                                        .setMessage(qbChatMessage.getBody())
                                        .setPositiveButton("Open chat", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                QBRequestGetBuilder builder = new QBRequestGetBuilder();

                                                builder.in("_id", qbChat.getDialogId());

                                                QBChatService.getChatDialogs(QBDialogType.PRIVATE, builder, new QBEntityCallbackImpl<ArrayList<QBDialog>>() {
                                                    @Override
                                                    public void onSuccess(ArrayList<QBDialog> result, Bundle params) {
                                                        if (result.size()==0) return;
                                                        openChat(result.get(0));
                                                    }
                                                });

                                                ///qbChat.getDialogId()
                                                ///qbChat.getDialogId()
                                            }
                                        })
                                        .setNegativeButton("Cancel", null)
                                        .show();
                            }
                        });

                    }

                    @Override
                    public void processError(QBChat qbChat, QBChatException e, QBChatMessage qbChatMessage) {

                    }
                });
            }
        }
    };


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
        try {
            if (QBAuth.getBaseService().getToken()!=null){
                init();
            }else{
                prefs=PreferenceManager.getDefaultSharedPreferences(this);
                QBAuth.createSession(prefs.getString(Util.QB_USER,""),prefs.getString(Util.QB_PASSWORD,""),
                        new QBEntityCallbackImpl<QBSession>(){
                            @Override
                            public void onSuccess(QBSession result, Bundle params) {
                                init();
                            }

                            @Override
                            public void onError(List<String> errors) {
                                startActivity(new Intent(MainActivity.this, Login.class));
                                finish();

                            }
                        });
            }
        } catch (BaseServiceException e) {
            prefs=PreferenceManager.getDefaultSharedPreferences(this);
            QBAuth.createSession(prefs.getString(Util.QB_USER, ""), prefs.getString(Util.QB_PASSWORD, ""),
                    new QBEntityCallbackImpl<QBSession>() {
                        @Override
                        public void onSuccess(QBSession result, Bundle params) {
                            init();
                        }

                        @Override
                        public void onError(List<String> errors) {
                            startActivity(new Intent(MainActivity.this, Login.class));
                            finish();

                        }
                    });
        }




    }
    protected void  init(){
        QBChatService chatService=null;
        if (!QBChatService.isInitialized()) {
            QBChatService.init(context);
        }
        chatService = QBChatService.getInstance();




        prefs=PreferenceManager.getDefaultSharedPreferences(this);
        final QBUser qbUser = new QBUser(prefs.getString(Util.QB_USER,""),prefs.getString(Util.QB_PASSWORD,""));
        qbUser.setId(prefs.getInt(Util.QB_USERID, 0));
        chatService.login(qbUser, new QBEntityCallbackImpl() {
            @Override
            public void onSuccess() {
                try {
                    QBChatService.getInstance().startAutoSendPresence(60);

                    QBChatService.getInstance().getPrivateChatManager().addPrivateChatManagerListener( chatListener);

                } catch (SmackException.NotLoggedInException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(List errors) {
                // errror
            }
        });



        loadUsers();

        if (!isUserRegistered(context)) {


            if (checkPlayServices()) {
                gcm = GoogleCloudMessaging.getInstance(this);
                regid = getRegistrationId(context);

                if (regid.isEmpty()) {
                    registerInBackground();
                }else {
                    sendRegistrationIdToBackend(regid);
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


    }



    public void setListView(UserList userList) {
        //final ListAdapter listAdapter = new CustomAdapter(this, ud.user, ud.upics, ud.descr, ud.geol);
      //  listView.setAdapter(listAdapter);

        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    public void performClickAction(int position) {

        //occupants_ids
        QBRequestGetBuilder builder = new QBRequestGetBuilder();
        final QBUser user = adapter.getItem(position);
        builder.in("occupants_ids", user.getId());

        QBChatService.getChatDialogs(QBDialogType.PRIVATE, builder, new QBEntityCallbackImpl<ArrayList<QBDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBDialog> result, Bundle params) {
                super.onSuccess(result, params);
                if (result.size() == 0) {

                    QBPrivateChatManager chatManager = QBChatService.getInstance().getPrivateChatManager();
                    chatManager.createDialog(user.getId(), new QBEntityCallbackImpl<QBDialog>() {

                        @Override
                        public void onSuccess(QBDialog result, Bundle params) {
                            openChat(result);
                        }

                        @Override
                        public void onError(List<String> errors) {
                            Util.onError(errors, MainActivity.this);
                        }
                    });

                } else {
                    openChat(result.get(0));
                }
            }

            @Override
            public void onError(List<String> errors) {
                Util.onError(errors, MainActivity.this);
            }
        });

    }
    private void openChat(QBDialog dialog){
        Intent intent=new Intent(MainActivity.this,ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_DIALOG, dialog);
        startActivity(intent);
    }

    protected void loadUsers(){
        prefs=PreferenceManager.getDefaultSharedPreferences(this);
        QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
        pagedRequestBuilder.setPage(1);
        pagedRequestBuilder.setPerPage(100);
        QBUsers.getUsers(pagedRequestBuilder, new QBEntityCallbackImpl<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                mSwipeRefreshLayout.setRefreshing(false);
                int currentUserId = prefs.getInt(Util.QB_USERID, 0);
                for (int i = 0; i < users.size(); i++) {

                    if (users.get(i).getId() == currentUserId) {
                        users.remove(i);

                    }
                }

                adapter = new UserListAdapter(context, 0, users);
                listView.setAdapter(adapter);
            }

            @Override
            public void onError(List<String> errors) {
                mSwipeRefreshLayout.setRefreshing(false);
                Util.onError(errors, MainActivity.this);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (QBChatService.isInitialized()) {
            QBChatService.getInstance().getPrivateChatManager().removePrivateChatManagerListener(chatListener);
        }
    }

    @Override
    public void onRefresh() {

        loadUsers();
    }


    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */


    private void registerInBackground() {
        new AsyncTask<Void,Void,String>() {


            @Override
            protected String doInBackground(Void[] params) {


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

                    return regid;

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;


            }

            @Override
            protected void onPostExecute(String s) {
                sendRegistrationIdToBackend(regid);
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



    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the registration ID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }


    //  private RequestQueue mRequestQueue;
    private void sendRegistrationIdToBackend(String registrationID) {
        String deviceId =Build.MANUFACTURER+" "+Build.MODEL;

        QBMessages.subscribeToPushNotificationsTask(registrationID, deviceId, QBEnvironment.DEVELOPMENT, new QBEntityCallbackImpl<ArrayList<QBSubscription>>() {
            @Override
            public void onSuccess(ArrayList<QBSubscription> subscriptions, Bundle args) {
                //Log.d(LOG_TAG, "subscribed");
            }

            @Override
            public void onError(List<String> errors) {
                Util.onError(errors, MainActivity.this);
            }
        });
    }


    /**
     * Handle the result of a request for permissions.
     *
     * Watches for the result of a request for permission to use fine location (GPS) data.
     * If the request was granted, continue processing.
     * If the request was denied, stop; the application needs location data to work and cannot be
     * used without permission to use location data.
     *
     * @param requestCode The ID of the permissions request.
     * @param permissions The permissions that were requested.
     * @param grantResults The grant or denial for each requested permission.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode) {
            case GPS.PermissionRequestId:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // TODO continue processing
                    android.util.Log.i(this.toString(), "ACCESS_FINE_LOCATION was granted");
                }
                else {
                    // TODO stop login
                    android.util.Log.w(this.toString(), "ACCESS_FINE_LOCATION was denied");
                }
                break;
            default:
                break;
        }
    }

}


