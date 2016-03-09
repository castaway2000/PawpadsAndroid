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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
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
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.location.QBLocations;
import com.quickblox.location.model.QBLocation;
import com.quickblox.location.request.QBLocationRequestBuilder;
import com.quickblox.location.request.SortField;
import com.quickblox.location.request.SortOrder;
import com.quickblox.messages.QBMessages;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBSubscription;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import saberapplications.pawpads.About;
import saberapplications.pawpads.GPS;
import saberapplications.pawpads.R;
import saberapplications.pawpads.UserList;
import saberapplications.pawpads.UserLocalStore;
import saberapplications.pawpads.Util;
import saberapplications.pawpads.ui.profile.ProfileEditActivity;
import saberapplications.pawpads.ui.profile.ProfileActivity;
import saberapplications.pawpads.ui.BaseActivity;
import saberapplications.pawpads.ui.chat.ChatActivity;
import saberapplications.pawpads.ui.dialogs.DialogsListActivity;
import saberapplications.pawpads.ui.login.LoginActivity;


public class MainActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

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

    private QBPrivateChatManagerListener chatListener = new QBPrivateChatManagerListener() {
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
                                                        if (result.size() == 0) return;
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
    protected void onStart() {
        BaseActivity.openActivitiesCount++;
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        context = getApplicationContext();
        listView = (ListView) findViewById(R.id.listView);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipelayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        userLocalStore = new UserLocalStore(this);
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String userName = defaultSharedPreferences.getString(Util.USER_NAME, "");
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
        setTitle("PawPads | " + userName);

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
                Intent i = new Intent(MainActivity.this, ProfileEditActivity.class);
                startActivity(i);
                return true;

            case R.id.action_logout:
                userLocalStore = new UserLocalStore(this);
                userLocalStore.clearUserData();
                userLocalStore.setUserLoggedIn(false);
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return true;
            case R.id.action_dialogs_activity:
                startActivity(new Intent(this, DialogsListActivity.class));
                return true;
            case R.id.action_about_devs:
                startActivity(new Intent(this, About.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
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
        final QBLocation qbLocation = adapter.getItem(position);
        builder.in("occupants_ids", qbLocation.getUser().getId());

        QBChatService.getChatDialogs(QBDialogType.PRIVATE, builder, new QBEntityCallbackImpl<ArrayList<QBDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBDialog> result, Bundle params) {
                super.onSuccess(result, params);
                if (result.size() == 0) {

                    QBPrivateChatManager chatManager = QBChatService.getInstance().getPrivateChatManager();
                    chatManager.createDialog(qbLocation.getUser().getId(), new QBEntityCallbackImpl<QBDialog>() {

                        @Override
                        public void onSuccess(QBDialog result, Bundle params) {
                            openChat(result, qbLocation.getUser());
                        }

                        @Override
                        public void onError(List<String> errors) {
                            Util.onError(errors, MainActivity.this);
                        }
                    });

                } else {
                    openChat(result.get(0), qbLocation.getUser());
                }
            }

            @Override
            public void onError(List<String> errors) {
                Util.onError(errors, MainActivity.this);
            }
        });

    }

    private void openChat(QBDialog dialog, QBUser user) {

        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        intent.putExtra(ChatActivity.EXTRA_DIALOG, dialog);
        intent.putExtra(Util.QB_USERID, user.getId());
        startActivity(intent);
    }

    private void openChat(final QBDialog dialog) {
        QBUsers.getUser(dialog.getUserId(), new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                intent.putExtra(ChatActivity.EXTRA_DIALOG, dialog);
                intent.putExtra(Util.QB_USERID, qbUser.getId());
                startActivity(intent);
            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(List<String> list) {

            }
        });

    }


    private QBEntityCallbackImpl<ArrayList<QBUser>> qbUsersCallback = new QBEntityCallbackImpl<ArrayList<QBUser>>() {
        @Override
        public void onSuccess(final ArrayList<QBUser> users, Bundle params) {
            loadAndSetNearUsers();
        }

        @Override
        public void onError(List<String> errors) {
            mSwipeRefreshLayout.setRefreshing(false);
            Util.onError(errors, MainActivity.this);
        }
    };

    @Override
    protected void onStop() {
        if (QBChatService.isInitialized()) {
            QBChatService.getInstance().getPrivateChatManager().removePrivateChatManagerListener(chatListener);
        }
        super.onStop();

    }

    @Override
    public void onQBConnect() throws Exception {

        QBChatService.getInstance().getPrivateChatManager().addPrivateChatManagerListener(chatListener);

        loadAndSetNearUsers();

        if (!isUserRegistered(context)) {


            if (checkPlayServices()) {
                gcm = GoogleCloudMessaging.getInstance(this);
                regid = getRegistrationId(context);

                if (regid.isEmpty()) {
                    registerInBackground();
                } else {
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

    @Override
    public void onRefresh() {

        loadAndSetNearUsers();
    }


    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */


    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {


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
        String deviceId = Build.MANUFACTURER + " " + Build.MODEL;

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
     * <p>
     * Watches for the result of a request for permission to use fine location (GPS) data.
     * If the request was granted, continue processing.
     * If the request was denied, stop; the application needs location data to work and cannot be
     * used without permission to use location data.
     *
     * @param requestCode  The ID of the permissions request.
     * @param permissions  The permissions that were requested.
     * @param grantResults The grant or denial for each requested permission.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case GPS.PermissionRequestId:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // TODO continue processing
                    android.util.Log.i(this.toString(), "ACCESS_FINE_LOCATION was granted");
                } else {
                    // TODO stop login
                    android.util.Log.w(this.toString(), "ACCESS_FINE_LOCATION was denied");
                }
                break;
            default:
                break;
        }
    }

    private void loadAndSetNearUsers() {

        final int currentUserId = prefs.getInt(Util.QB_USERID, 0);
        final ArrayList<QBLocation> nearLocations = new ArrayList<>();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        QBLocationRequestBuilder getLocationsBuilder = new QBLocationRequestBuilder();
        Double latitude = Double.valueOf(prefs.getString(Util.USER_LOCATION_LAT, ""));
        Double longitude = Double.valueOf(prefs.getString(Util.USER_LOCATION_LONG, ""));
        getLocationsBuilder.setRadius(latitude, longitude, 200);
        getLocationsBuilder.setLastOnly();
        getLocationsBuilder.setSort(SortField.DISTANCE, SortOrder.ASCENDING);

        QBLocations.getLocations(getLocationsBuilder, new QBEntityCallbackImpl<ArrayList<QBLocation>>() {
            @Override
            public void onSuccess(ArrayList<QBLocation> locations, Bundle params) {
                for (QBLocation qbLocation : locations) {
                    boolean isContain = false;
                    for (QBLocation location : nearLocations) {
                        if (location.getUserId().equals(qbLocation.getUserId())) {
                            isContain = true;
                            break;
                        }
                    }
                    if (qbLocation.getUser().getId() != currentUserId && !isContain) {
                        nearLocations.add(qbLocation);
                    }
                }
                adapter = new UserListAdapter(context, 0, nearLocations);
                adapter.setLocation(getLastLocation());
                listView.setAdapter(adapter);
                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onError(List<String> errors) {
                mSwipeRefreshLayout.setRefreshing(false);
                Util.onError(errors, MainActivity.this);
            }
        });
    }
}


