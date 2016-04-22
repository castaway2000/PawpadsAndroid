package saberapplications.pawpads;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;


/**
 * Created by blaze on 9/8/2015.
 */

public class GPS extends Activity {
    Context mContext;

    public final static int PermissionRequestId = 100000;

    public GPS(Context context) {
        this.mContext = context;

        // Check for permission to use location data.
        if(ContextCompat.checkSelfPermission(this.mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(
                    (Activity)this.mContext, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // TODO justify needing permission to use location data.
                android.util.Log.w(this.mContext.toString(), "ActivityCompat determined that a rationale should be provided for requesting ACCESS_FINE_LOCATION");
            }
            else {
                // Ask for permission to use location data, with no special information.
                ActivityCompat.requestPermissions(
                        (Activity)this.mContext,
                        new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                        GPS.PermissionRequestId);
            }
        }
    }

    public void checkGPSEnabled(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if(!manager.isProviderEnabled( LocationManager.GPS_PROVIDER )) {
            buildAlertMessageNoGps();
        }
        else{finish();}
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

}
