package saberapplications.pawpads.service;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.location.QBLocations;
import com.quickblox.location.model.QBLocation;
import com.quickblox.location.request.QBLocationRequestBuilder;
import com.quickblox.location.request.SortField;
import com.quickblox.location.request.SortOrder;

import java.text.NumberFormat;
import java.util.ArrayList;

import saberapplications.pawpads.C;
import saberapplications.pawpads.PawPadsApplication;
import saberapplications.pawpads.Util;

/**
 * This service sends user location updates when app is active
 */

public class UserLocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private static final String USERID = "userid";
    public static final String LOCATION_CHANGED = "LOCATION_CHANGED";
    public static final String LOCATION = "location";
    private GoogleApiClient mGoogleApiClient;
    private static Location lastLocation;
    private QBLocation qbLocation;
    private int userId;
    private static boolean mIsRunning;

    public UserLocationService() {

    }

    public static void startService(int userId) {
        if (!checkPermissions()) return;
        Intent intent = new Intent(PawPadsApplication.getInstance(), UserLocationService.class);
        intent.putExtra(USERID, userId);
        PawPadsApplication.getInstance().startService(intent);

    }

    public static void stop() {
        Intent intent = new Intent(PawPadsApplication.getInstance(), UserLocationService.class);
        PawPadsApplication.getInstance().stopService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mIsRunning = true;
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null && userId == 0) {
            stopSelf();
        }
        if (intent == null) {
            stopSelf();
            return START_STICKY;
        }
        userId = intent.getIntExtra(USERID, 0);
        if (userId == 0) {
            stopSelf();
        }
        return START_STICKY;


    }

    @Override
    public void onDestroy() {
        mIsRunning = false;
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationRequest locationRequest = new LocationRequest();
        SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(this);
        switch (preferences.getString(C.ACCURACY,C.ACCURACY_MEDIUM)){
            case C.ACCURACY_LOW:
                locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
                break;
            case C.ACCURACY_MEDIUM:
                locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                break;
            case C.ACCURACY_HIGH:
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                break;
        }

        locationRequest.setInterval(120000);
        locationRequest.setFastestInterval(60000);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, locationRequest, this);
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        updateLocationAsync(lastLocation);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {


    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        updateLocationAsync(location);
    }

    private void updateLocationAsync(final Location location) {
        Intent intent = new Intent(LOCATION_CHANGED);
        intent.putExtra("location", location);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                updateUserLocation(location);
                return null;
            }
        };
        task.execute();
    }

    private void updateUserLocation(Location location) {
        if (!mGoogleApiClient.isConnected()) return;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (location == null) {
            location = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
        }

        if (location == null) return;
        if (qbLocation == null) {
            initUserLocation(lastLocation);
        }
        if (qbLocation == null) {
            stopSelf();
            return;
        }

        qbLocation.setLatitude(accuracySettings(location.getLatitude()));
        qbLocation.setLongitude(accuracySettings(location.getLongitude()));

        try {
            QBLocations.deleteObsoleteLocations(1);
            qbLocation = QBLocations.updateLocation(qbLocation);
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();
            editor.putString(Util.USER_LOCATION_LAT, String.valueOf(qbLocation.getLatitude()));
            editor.putString(Util.USER_LOCATION_LONG, String.valueOf(qbLocation.getLongitude()));
            editor.putInt(C.QB_USERID, userId);
            editor.apply();

        } catch (QBResponseException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

    }

    protected void initUserLocation(Location location) {
        try {
            QBLocationRequestBuilder getLocationsBuilder = new QBLocationRequestBuilder();
            getLocationsBuilder.setSort(SortField.CREATED_AT, SortOrder.DESCENDING);
            getLocationsBuilder.setUserId(userId);
            Bundle bundle = new Bundle();
            ArrayList<QBLocation> locations = QBLocations.getLocations(getLocationsBuilder, bundle);
            if (locations.size() > 0) {
                qbLocation = locations.get(0);
            } else {
                qbLocation = new QBLocation();
                qbLocation.setUserId(userId);


                qbLocation.setLatitude(accuracySettings(location.getLatitude()));
                qbLocation.setLongitude(accuracySettings(location.getLongitude()));

                qbLocation = QBLocations.createLocation(qbLocation);
            }
        } catch (QBResponseException e) {
            if (e.getMessage().equals("Entity you are looking for was not found.")) {
                qbLocation = new QBLocation();
                qbLocation.setUserId(userId);

                qbLocation.setLatitude(accuracySettings(location.getLatitude()));
                qbLocation.setLongitude(accuracySettings(location.getLongitude()));

                try {
                    qbLocation = QBLocations.createLocation(qbLocation);
                } catch (QBResponseException e1) {
                    e1.printStackTrace();
                    Crashlytics.logException(e1);
                }
            } else {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
        }
    }

    public static Location getLastLocation() {
        return lastLocation;
    }


    public Double accuracySettings(Double location) {
        Double loc;
        NumberFormat formatter;
        if (Util.ACCURACY==null){
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            Util.ACCURACY=prefs.getString(C.ACCURACY,C.ACCURACY_MEDIUM);
        }
        if (Util.ACCURACY.equals(C.ACCURACY_HIGH)) {
            loc = (double)Math.round(location * 1000) / 1000;
            return loc;
        } else if (Util.ACCURACY.equals(C.ACCURACY_LOW)) {
            loc = (double)Math.round(location * 10) / 10;
            return loc;
        } else {
            return location;
        }
    }

    public static boolean isRunning() {
        return mIsRunning;
    }

    private static boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(PawPadsApplication.getInstance(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(PawPadsApplication.getInstance(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }

        }

        return true;
    }

}
