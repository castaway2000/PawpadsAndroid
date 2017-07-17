package saberapplications.pawpads.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import saberapplications.pawpads.R;

/**
 * Created by Stanislav Volnjanskij on 7/10/17.
 */

public class NetworkStateChangedHelper {
    AppCompatActivity activity;
    private static boolean dialogIsShown=false;
    private AlertDialog dialog;

    public NetworkStateChangedHelper(AppCompatActivity activity) {
        this.activity = activity;
    }

    BroadcastReceiver networkStateChanged=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkNeworkConnection();
        }
    };

    public void register(){
        activity.registerReceiver(networkStateChanged,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }
    public void unRegister(){
        activity.unregisterReceiver(networkStateChanged);
        if (dialogIsShown) dialogIsShown=false;
    }
    public boolean isConnected(){
        ConnectivityManager cm =
                (ConnectivityManager)activity.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork!=null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }
    public synchronized void checkNeworkConnection(){
        if (!isConnected() && !dialogIsShown){
            dialogIsShown=true;
            dialog = new AlertDialog.Builder(activity)
                    .setMessage(R.string.interner_not_connecteed)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            activity.finish();
                        }
                    })
                    .show();

        }else if(!isConnected() && dialogIsShown && dialog==null){
            activity.finish();
        }
    }

}
