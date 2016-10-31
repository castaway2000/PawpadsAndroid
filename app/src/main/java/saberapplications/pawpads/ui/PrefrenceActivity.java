package saberapplications.pawpads.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;

import saberapplications.pawpads.C;
import saberapplications.pawpads.R;
import saberapplications.pawpads.Util;
import saberapplications.pawpads.databinding.ActivitySettingsBinding;
import saberapplications.pawpads.databinding.BindableBoolean;
import saberapplications.pawpads.databinding.BindableString;
import saberapplications.pawpads.ui.login.LoginActivity;

/**
 * Created by blaze on 3/24/2016.
 */
public class PrefrenceActivity extends BaseActivity{
    private SharedPreferences preferences;
    Button removeProfile, savebtn;
    EditText dist;
    CheckBox tbMetricBox, tbImBox, tbPushBox;
    RadioGroup rbGroup;
    RadioButton rbHigh, rbMedium, rbLow, rbMI, rbKM;

    Boolean alert, push, gAlert, gPush;

    ActivitySettingsBinding binding;

    public BindableString range=new BindableString();
    public BindableString unit=new BindableString();
    public BindableString accuracy=new BindableString();
    public BindableBoolean pushes=new BindableBoolean();
    public BindableBoolean popups=new BindableBoolean();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= DataBindingUtil.setContentView(this,R.layout.activity_settings);
        binding.setActivity(this);
        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(null);
        TextView  textView= (TextView) findViewById(R.id.toolbar_title);
        textView.setText(R.string.settings);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        preferences = PreferenceManager.getDefaultSharedPreferences(PrefrenceActivity.this);
        dist = (EditText) findViewById(R.id.etRange);
        tbPushBox = (CheckBox) findViewById(R.id.ckPushNotifications);
        tbImBox = (CheckBox) findViewById(R.id.ckImNotification);
        rbLow = (RadioButton) findViewById(R.id.rbLow);
        rbMedium = (RadioButton) findViewById(R.id.rbMedium);
        rbHigh = (RadioButton) findViewById(R.id.rbHigh);
        rbMI = (RadioButton) findViewById(R.id.rbMI);
        rbKM = (RadioButton) findViewById(R.id.rbKM);

        accuracy.set( preferences.getString("accuracy", "medium"));

        unit.set(preferences.getString(C.MEASURE_UNIT, "KM"));

        range.set(String.valueOf(preferences.getInt("range", 60)));

        range.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                SharedPreferences.Editor editor=preferences.edit();
                editor.putInt(C.RANGE,Integer.parseInt(range.get()));
                editor.apply();
            }
        });
        pushes.set(preferences.getBoolean(C.PUSH, true));

        pushes.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                SharedPreferences.Editor editor=preferences.edit();
                editor.putBoolean(C.PUSH,pushes.get());
                editor.apply();
            }
        });

        popups.set( preferences.getBoolean(C.ALERT, true));

        popups.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                SharedPreferences.Editor editor=preferences.edit();
                editor.putBoolean(C.ALERT,popups.get());
                editor.apply();
            }
        });

        binding.units.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.rbKM:
                        unit.set("KM");
                        break;
                    case R.id.rbMI:
                        unit.set("MI");
                        break;
                }
                SharedPreferences.Editor editor=preferences.edit();
                editor.putString(C.MEASURE_UNIT,unit.get());
                editor.apply();
            }
        });

        binding.accuracy.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.rbLow:
                        accuracy.set(C.ACCURACY_LOW);
                        break;
                    case R.id.rbMedium:
                        accuracy.set(C.ACCURACY_MEDIUM);
                        break;
                    case R.id.rbHigh:
                        accuracy.set(C.ACCURACY_HIGH);
                        break;
                }
                SharedPreferences.Editor editor=preferences.edit();
                editor.putString(C.ACCURACY,accuracy.get());
                editor.apply();
            }
        });


        TextView appVersion = (TextView) findViewById(R.id.app_version);
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            appVersion.setText(getString(R.string.version) + String.format("%s(%d)", pInfo.versionName, pInfo.versionCode));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }


    public void saveSettings(int accuracy, int range, String unit, Boolean alert, Boolean push) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(PrefrenceActivity.this).edit();
        editor.putInt("accuracy", accuracy);

        if (unit.equals("MI")) {
            int rangeM = (int) Math.floor(range * 1.60934);
            editor.putInt("range_km", rangeM);
            //TODO: make this show what user input was not its modified value
        } else {
            editor.putInt("range_km", range);
        }
        editor.putInt("range", range);
        editor.putInt("dsp_range", range);
        editor.putString("unit", unit);
        editor.putBoolean("alert", alert);
        editor.putBoolean("push", push);
        editor.apply();

        Util.ACCURACY = accuracy;
        Util.PUSH_NOTIFICIATIONS = push;
        Util.IM_ALERT = alert;
        Util.UNIT_OF_MEASURE = unit;
        Toast.makeText(getApplicationContext(), "settings saved", Toast.LENGTH_SHORT).show();
    }

    public void deleteUserProfile() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(PrefrenceActivity.this);
        alertDialog.setTitle("Remove profile");
        alertDialog.setMessage("All your data will be deleted. Are you sure?");
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                QBUsers.deleteUser(preferences.getInt(C.QB_USERID, -1), new QBEntityCallback() {
                    @Override
                    public void onSuccess(Object o, Bundle bundle) {
                        Toast.makeText(PrefrenceActivity.this, "Remove profile successfully", Toast.LENGTH_LONG).show();
                        Intent myIntent1 = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(myIntent1);
                        finish();
                        LocalBroadcastManager.getInstance(PrefrenceActivity.this).sendBroadcast(new Intent(C.CLOSE_ALL_APP_ACTIVITIES));
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Util.onError(e, PrefrenceActivity.this);
                    }

                });

            }
        });
        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
